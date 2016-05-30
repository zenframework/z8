package org.zenframework.z8.server.rmi;

import java.io.Externalizable;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamConstants;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import sun.misc.Unsafe;
import sun.reflect.ReflectionFactory;

//@SuppressWarnings("restriction")
public class StandardObjectStreamClass implements Serializable {
	public static final StandardObjectStreamField[] NO_FIELDS = new StandardObjectStreamField[0];

	private static final long serialVersionUID = -6120832682080437368L;

	private static final ReflectionFactory reflFactory = AccessController.doPrivileged(new ReflectionFactory.GetReflectionFactoryAction());

	private static class Caches {
		static final ConcurrentMap<WeakClassKey, Reference<?>> localDescs = new ConcurrentHashMap<WeakClassKey, Reference<?>>();

		static final ConcurrentMap<FieldReflectorKey, Reference<?>> reflectors = new ConcurrentHashMap<FieldReflectorKey, Reference<?>>();

		private static final ReferenceQueue<Class<?>> localDescsQueue = new ReferenceQueue<Class<?>>();

		private static final ReferenceQueue<Class<?>> reflectorsQueue = new ReferenceQueue<Class<?>>();
	}

	private Class<?> cl;
	private String name;
	private volatile Long suid;
	private boolean isProxy;
	private boolean isEnum;
	private boolean serializable;
	private boolean externalizable;
	private boolean hasWriteObjectData;
	private boolean hasBlockExternalData = true;

	private static class ExceptionInfo {
		private final String className;
		private final String message;

		ExceptionInfo(String cn, String msg) {
			className = cn;
			message = msg;
		}

		InvalidClassException newInvalidClassException() {
			return new InvalidClassException(className, message);
		}
	}

	private ClassNotFoundException resolveEx;
	private ExceptionInfo deserializeEx;
	private ExceptionInfo serializeEx;
	private ExceptionInfo defaultSerializeEx;

	private StandardObjectStreamField[] fields;
	private int primDataSize;
	private int numObjFields;
	private FieldReflector fieldRefl;
	private volatile ClassDataSlot[] dataLayout;

	private Constructor<?> cons;
	private Method writeObjectMethod;
	private Method readObjectMethod;
	private Method readObjectNoDataMethod;
	private Method writeReplaceMethod;
	private Method readResolveMethod;

	private StandardObjectStreamClass localDesc;
	private StandardObjectStreamClass superDesc;

	public static StandardObjectStreamClass lookup(Class<?> cl) {
		return lookup(cl, false);
	}

	public static StandardObjectStreamClass lookupAny(Class<?> cl) {
		return lookup(cl, true);
	}

	public String getName() {
		return name;
	}

	public long getSerialVersionUID() {
		return suid.longValue();
	}

	public Class<?> forClass() {
		return cl;
	}

	public StandardObjectStreamField[] getFields() {
		return getFields(true);
	}

	public StandardObjectStreamField getField(String name) {
		return getField(name, null);
	}

	public String toString() {
		return name + ": static final long serialVersionUID = " + getSerialVersionUID() + "L;";
	}

	static StandardObjectStreamClass lookup(Class<?> cl, boolean all) {
		if(!(all || Serializable.class.isAssignableFrom(cl))) {
			return null;
		}
		processQueue(Caches.localDescsQueue, Caches.localDescs);
		WeakClassKey key = new WeakClassKey(cl, Caches.localDescsQueue);
		Reference<?> ref = Caches.localDescs.get(key);
		Object entry = null;
		if(ref != null) {
			entry = ref.get();
		}
		EntryFuture future = null;
		if(entry == null) {
			EntryFuture newEntry = new EntryFuture();
			Reference<?> newRef = new SoftReference<EntryFuture>(newEntry);
			do {
				if(ref != null) {
					Caches.localDescs.remove(key, ref);
				}
				ref = Caches.localDescs.putIfAbsent(key, newRef);
				if(ref != null) {
					entry = ref.get();
				}
			} while(ref != null && entry == null);
			if(entry == null) {
				future = newEntry;
			}
		}

		if(entry instanceof StandardObjectStreamClass) { // check common case
															// first
			return (StandardObjectStreamClass)entry;
		}
		if(entry instanceof EntryFuture) {
			future = (EntryFuture)entry;
			if(future.getOwner() == Thread.currentThread()) {
				entry = null;
			} else {
				entry = future.get();
			}
		}
		if(entry == null) {
			try {
				entry = new StandardObjectStreamClass(cl);
			} catch(Throwable th) {
				entry = th;
			}
			if(future.set(entry)) {
				Caches.localDescs.put(key, new SoftReference<Object>(entry));
			} else {
				// nested lookup call already set future
				entry = future.get();
			}
		}

		if(entry instanceof StandardObjectStreamClass) {
			return (StandardObjectStreamClass)entry;
		} else if(entry instanceof RuntimeException) {
			throw (RuntimeException)entry;
		} else if(entry instanceof Error) {
			throw (Error)entry;
		} else {
			throw new InternalError("unexpected entry: " + entry);
		}
	}

	private static class EntryFuture {

		private static final Object unset = new Object();
		private final Thread owner = Thread.currentThread();
		private Object entry = unset;

		synchronized boolean set(Object entry) {
			if(this.entry != unset) {
				return false;
			}
			this.entry = entry;
			notifyAll();
			return true;
		}

		synchronized Object get() {
			boolean interrupted = false;
			while(entry == unset) {
				try {
					wait();
				} catch(InterruptedException ex) {
					interrupted = true;
				}
			}
			if(interrupted) {
				AccessController.doPrivileged(new PrivilegedAction<Void>() {
					public Void run() {
						Thread.currentThread().interrupt();
						return null;
					}
				});
			}
			return entry;
		}

		Thread getOwner() {
			return owner;
		}
	}

	private StandardObjectStreamClass(final Class<?> cl) {
		this.cl = cl;
		name = cl.getName();
		isProxy = Proxy.isProxyClass(cl);
		isEnum = Enum.class.isAssignableFrom(cl);
		serializable = Serializable.class.isAssignableFrom(cl);
		externalizable = Externalizable.class.isAssignableFrom(cl);

		Class<?> superCl = cl.getSuperclass();
		superDesc = (superCl != null) ? lookup(superCl, false) : null;
		localDesc = this;

		if(serializable) {
			AccessController.doPrivileged(new PrivilegedAction<Void>() {
				public Void run() {
					if(isEnum) {
						suid = Long.valueOf(0);
						fields = NO_FIELDS;
						return null;
					}
					if(cl.isArray()) {
						fields = NO_FIELDS;
						return null;
					}

					suid = getDeclaredSUID(cl);
					try {
						fields = getSerialFields(cl);
						computeFieldOffsets();
					} catch(InvalidClassException e) {
						serializeEx = deserializeEx = new ExceptionInfo(e.classname, e.getMessage());
						fields = NO_FIELDS;
					}

					if(externalizable) {
						cons = getExternalizableConstructor(cl);
					} else {
						cons = getSerializableConstructor(cl);
						writeObjectMethod = getPrivateMethod(cl, "writeObject", new Class<?>[] { ObjectOutputStream.class }, Void.TYPE);
						readObjectMethod = getPrivateMethod(cl, "readObject", new Class<?>[] { ObjectInputStream.class }, Void.TYPE);
						readObjectNoDataMethod = getPrivateMethod(cl, "readObjectNoData", null, Void.TYPE);
						hasWriteObjectData = (writeObjectMethod != null);
					}
					writeReplaceMethod = getInheritableMethod(cl, "writeReplace", null, Object.class);
					readResolveMethod = getInheritableMethod(cl, "readResolve", null, Object.class);
					return null;
				}
			});
		} else {
			suid = Long.valueOf(0);
			fields = NO_FIELDS;
		}

		try {
			fieldRefl = getReflector(fields, this);
		} catch(InvalidClassException ex) {
			// field mismatches impossible when matching local fields vs. self
			throw new InternalError(ex);
		}

		if(deserializeEx == null) {
			if(isEnum) {
				deserializeEx = new ExceptionInfo(name, "enum type");
			} else if(cons == null) {
				deserializeEx = new ExceptionInfo(name, "no valid constructor");
			}
		}
		for(int i = 0; i < fields.length; i++) {
			if(fields[i].getField() == null) {
				defaultSerializeEx = new ExceptionInfo(name, "unmatched serializable field(s) declared");
			}
		}
	}

	StandardObjectStreamClass() {
	}

	void initProxy(Class<?> cl, ClassNotFoundException resolveEx, StandardObjectStreamClass superDesc) throws InvalidClassException {
		this.cl = cl;
		this.resolveEx = resolveEx;
		this.superDesc = superDesc;
		isProxy = true;
		serializable = true;
		suid = Long.valueOf(0);
		fields = NO_FIELDS;

		if(cl != null) {
			localDesc = lookup(cl, true);
			if(!localDesc.isProxy) {
				throw new InvalidClassException("cannot bind proxy descriptor to a non-proxy class");
			}
			name = localDesc.name;
			externalizable = localDesc.externalizable;
			cons = localDesc.cons;
			writeReplaceMethod = localDesc.writeReplaceMethod;
			readResolveMethod = localDesc.readResolveMethod;
			deserializeEx = localDesc.deserializeEx;
		}
		fieldRefl = getReflector(fields, localDesc);
	}

	void initNonProxy(StandardObjectStreamClass model, Class<?> cl, ClassNotFoundException resolveEx, StandardObjectStreamClass superDesc) throws InvalidClassException {
		this.cl = cl;
		this.resolveEx = resolveEx;
		this.superDesc = superDesc;
		name = model.name;
		suid = Long.valueOf(model.getSerialVersionUID());
		isProxy = false;
		isEnum = model.isEnum;
		serializable = model.serializable;
		externalizable = model.externalizable;
		hasBlockExternalData = model.hasBlockExternalData;
		hasWriteObjectData = model.hasWriteObjectData;
		fields = model.fields;
		primDataSize = model.primDataSize;
		numObjFields = model.numObjFields;

		if(cl != null) {
			localDesc = lookup(cl, true);
			if(localDesc.isProxy) {
				throw new InvalidClassException("cannot bind non-proxy descriptor to a proxy class");
			}
			if(isEnum != localDesc.isEnum) {
				throw new InvalidClassException(isEnum ? "cannot bind enum descriptor to a non-enum class" : "cannot bind non-enum descriptor to an enum class");
			}

			if(serializable == localDesc.serializable && !cl.isArray() && suid.longValue() != localDesc.getSerialVersionUID()) {
				throw new InvalidClassException(localDesc.name, "local class incompatible: " + "stream classdesc serialVersionUID = " + suid + ", local class serialVersionUID = " + localDesc.getSerialVersionUID());
			}

			if(!classNamesEqual(name, localDesc.name)) {
				throw new InvalidClassException(localDesc.name, "local class name incompatible with stream class " + "name \"" + name + "\"");
			}

			if(!isEnum) {
				if((serializable == localDesc.serializable) && (externalizable != localDesc.externalizable)) {
					throw new InvalidClassException(localDesc.name, "Serializable incompatible with Externalizable");
				}

				if((serializable != localDesc.serializable) || (externalizable != localDesc.externalizable) || !(serializable || externalizable)) {
					deserializeEx = new ExceptionInfo(localDesc.name, "class invalid for deserialization");
				}
			}

			cons = localDesc.cons;
			writeObjectMethod = localDesc.writeObjectMethod;
			readObjectMethod = localDesc.readObjectMethod;
			readObjectNoDataMethod = localDesc.readObjectNoDataMethod;
			writeReplaceMethod = localDesc.writeReplaceMethod;
			readResolveMethod = localDesc.readResolveMethod;
			if(deserializeEx == null) {
				deserializeEx = localDesc.deserializeEx;
			}
		}
		fieldRefl = getReflector(fields, localDesc);
		// reassign to matched fields so as to reflect local unshared settings
		fields = fieldRefl.getFields();
	}

	void readNonProxy(StandardObjectInputStream in) throws IOException, ClassNotFoundException {
		name = in.readUTF();
		suid = Long.valueOf(in.readLong());
		isProxy = false;

		byte flags = in.readByte();
		hasWriteObjectData = ((flags & ObjectStreamConstants.SC_WRITE_METHOD) != 0);
		hasBlockExternalData = ((flags & ObjectStreamConstants.SC_BLOCK_DATA) != 0);
		externalizable = ((flags & ObjectStreamConstants.SC_EXTERNALIZABLE) != 0);
		boolean sflag = ((flags & ObjectStreamConstants.SC_SERIALIZABLE) != 0);
		if(externalizable && sflag) {
			throw new InvalidClassException(name, "serializable and externalizable flags conflict");
		}
		serializable = externalizable || sflag;
		isEnum = ((flags & ObjectStreamConstants.SC_ENUM) != 0);
		if(isEnum && suid.longValue() != 0L) {
			throw new InvalidClassException(name, "enum descriptor has non-zero serialVersionUID: " + suid);
		}

		int numFields = in.readShort();
		if(isEnum && numFields != 0) {
			throw new InvalidClassException(name, "enum descriptor has non-zero field count: " + numFields);
		}
		fields = (numFields > 0) ? new StandardObjectStreamField[numFields] : NO_FIELDS;
		for(int i = 0; i < numFields; i++) {
			char tcode = (char)in.readByte();
			String fname = in.readUTF();
			String signature = ((tcode == 'L') || (tcode == '[')) ? in.readTypeString() : new String(new char[] { tcode });
			try {
				fields[i] = new StandardObjectStreamField(fname, signature, false);
			} catch(RuntimeException e) {
				throw (IOException)new InvalidClassException(name, "invalid descriptor for field " + fname).initCause(e);
			}
		}
		computeFieldOffsets();
	}

	void writeNonProxy(StandardObjectOutputStream out) throws IOException {
		out.writeUTF(name);
		out.writeLong(getSerialVersionUID());

		byte flags = 0;
		if(externalizable) {
			flags |= ObjectStreamConstants.SC_EXTERNALIZABLE;
			int protocol = out.getProtocolVersion();
			if(protocol != ObjectStreamConstants.PROTOCOL_VERSION_1) {
				flags |= ObjectStreamConstants.SC_BLOCK_DATA;
			}
		} else if(serializable) {
			flags |= ObjectStreamConstants.SC_SERIALIZABLE;
		}
		if(hasWriteObjectData) {
			flags |= ObjectStreamConstants.SC_WRITE_METHOD;
		}
		if(isEnum) {
			flags |= ObjectStreamConstants.SC_ENUM;
		}
		out.writeByte(flags);

		out.writeShort(fields.length);
		for(int i = 0; i < fields.length; i++) {
			StandardObjectStreamField f = fields[i];
			out.writeByte(f.getTypeCode());
			out.writeUTF(f.getName());
			if(!f.isPrimitive()) {
				out.writeTypeString(f.getTypeString());
			}
		}
	}

	ClassNotFoundException getResolveException() {
		return resolveEx;
	}

	void checkDeserialize() throws InvalidClassException {
		if(deserializeEx != null) {
			throw deserializeEx.newInvalidClassException();
		}
	}

	void checkSerialize() throws InvalidClassException {
		if(serializeEx != null) {
			throw serializeEx.newInvalidClassException();
		}
	}

	void checkDefaultSerialize() throws InvalidClassException {
		if(defaultSerializeEx != null) {
			throw defaultSerializeEx.newInvalidClassException();
		}
	}

	StandardObjectStreamClass getSuperDesc() {
		return superDesc;
	}

	StandardObjectStreamClass getLocalDesc() {
		return localDesc;
	}

	StandardObjectStreamField[] getFields(boolean copy) {
		return copy ? fields.clone() : fields;
	}

	StandardObjectStreamField getField(String name, Class<?> type) {
		for(int i = 0; i < fields.length; i++) {
			StandardObjectStreamField f = fields[i];
			if(f.getName().equals(name)) {
				if(type == null || (type == Object.class && !f.isPrimitive())) {
					return f;
				}
				Class<?> ftype = f.getType();
				if(ftype != null && type.isAssignableFrom(ftype)) {
					return f;
				}
			}
		}
		return null;
	}

	boolean isProxy() {
		return isProxy;
	}

	boolean isEnum() {
		return isEnum;
	}

	boolean isExternalizable() {
		return externalizable;
	}

	boolean isSerializable() {
		return serializable;
	}

	boolean hasBlockExternalData() {
		return hasBlockExternalData;
	}

	boolean hasWriteObjectData() {
		return hasWriteObjectData;
	}

	boolean isInstantiable() {
		return (cons != null);
	}

	boolean hasWriteObjectMethod() {
		return (writeObjectMethod != null);
	}

	boolean hasReadObjectMethod() {
		return (readObjectMethod != null);
	}

	boolean hasReadObjectNoDataMethod() {
		return (readObjectNoDataMethod != null);
	}

	boolean hasWriteReplaceMethod() {
		return (writeReplaceMethod != null);
	}

	boolean hasReadResolveMethod() {
		return (readResolveMethod != null);
	}

	Object newInstance() throws InstantiationException, InvocationTargetException, UnsupportedOperationException {
		if(cons != null) {
			try {
				return cons.newInstance();
			} catch(IllegalAccessException ex) {
				throw new InternalError(ex);
			}
		} else {
			throw new UnsupportedOperationException();
		}
	}

	void invokeWriteObject(Object obj, StandardObjectOutputStream out) throws IOException, UnsupportedOperationException {
		if(writeObjectMethod != null) {
			try {
				writeObjectMethod.invoke(obj, new Object[] { out });
			} catch(InvocationTargetException ex) {
				Throwable th = ex.getTargetException();
				if(th instanceof IOException) {
					throw (IOException)th;
				} else {
					throwMiscException(th);
				}
			} catch(IllegalAccessException ex) {
				// should not occur, as access checks have been suppressed
				throw new InternalError(ex);
			}
		} else {
			throw new UnsupportedOperationException();
		}
	}

	void invokeReadObject(Object obj, StandardObjectInputStream in) throws ClassNotFoundException, IOException, UnsupportedOperationException {
		if(readObjectMethod != null) {
			try {
				readObjectMethod.invoke(obj, new Object[] { in });
			} catch(InvocationTargetException ex) {
				Throwable th = ex.getTargetException();
				if(th instanceof ClassNotFoundException) {
					throw (ClassNotFoundException)th;
				} else if(th instanceof IOException) {
					throw (IOException)th;
				} else {
					throwMiscException(th);
				}
			} catch(IllegalAccessException ex) {
				// should not occur, as access checks have been suppressed
				throw new InternalError(ex);
			}
		} else {
			throw new UnsupportedOperationException();
		}
	}

	void invokeReadObjectNoData(Object obj) throws IOException, UnsupportedOperationException {
		if(readObjectNoDataMethod != null) {
			try {
				readObjectNoDataMethod.invoke(obj, (Object[])null);
			} catch(InvocationTargetException ex) {
				Throwable th = ex.getTargetException();
				if(th instanceof ObjectStreamException) {
					throw (ObjectStreamException)th;
				} else {
					throwMiscException(th);
				}
			} catch(IllegalAccessException ex) {
				// should not occur, as access checks have been suppressed
				throw new InternalError(ex);
			}
		} else {
			throw new UnsupportedOperationException();
		}
	}

	Object invokeWriteReplace(Object obj) throws IOException, UnsupportedOperationException {
		if(writeReplaceMethod != null) {
			try {
				return writeReplaceMethod.invoke(obj, (Object[])null);
			} catch(InvocationTargetException ex) {
				Throwable th = ex.getTargetException();
				if(th instanceof ObjectStreamException) {
					throw (ObjectStreamException)th;
				} else {
					throwMiscException(th);
					throw new InternalError(th); // never reached
				}
			} catch(IllegalAccessException ex) {
				throw new InternalError(ex);
			}
		} else {
			throw new UnsupportedOperationException();
		}
	}

	Object invokeReadResolve(Object obj) throws IOException, UnsupportedOperationException {
		if(readResolveMethod != null) {
			try {
				return readResolveMethod.invoke(obj, (Object[])null);
			} catch(InvocationTargetException ex) {
				Throwable th = ex.getTargetException();
				if(th instanceof ObjectStreamException) {
					throw (ObjectStreamException)th;
				} else {
					throwMiscException(th);
					throw new InternalError(th); // never reached
				}
			} catch(IllegalAccessException ex) {
				// should not occur, as access checks have been suppressed
				throw new InternalError(ex);
			}
		} else {
			throw new UnsupportedOperationException();
		}
	}

	static class ClassDataSlot {
		final StandardObjectStreamClass desc;
		final boolean hasData;

		ClassDataSlot(StandardObjectStreamClass desc, boolean hasData) {
			this.desc = desc;
			this.hasData = hasData;
		}
	}

	ClassDataSlot[] getClassDataLayout() throws InvalidClassException {
		if(dataLayout == null) {
			dataLayout = getClassDataLayout0();
		}
		return dataLayout;
	}

	private ClassDataSlot[] getClassDataLayout0() throws InvalidClassException {
		ArrayList<ClassDataSlot> slots = new ArrayList<ClassDataSlot>();
		Class<?> start = cl, end = cl;

		while(end != null && Serializable.class.isAssignableFrom(end)) {
			end = end.getSuperclass();
		}

		HashSet<String> oscNames = new HashSet<String>(3);

		for(StandardObjectStreamClass d = this; d != null; d = d.superDesc) {
			if(oscNames.contains(d.name)) {
				throw new InvalidClassException("Circular reference.");
			} else {
				oscNames.add(d.name);
			}

			String searchName = (d.cl != null) ? d.cl.getName() : d.name;
			Class<?> match = null;
			for(Class<?> c = start; c != end; c = c.getSuperclass()) {
				if(searchName.equals(c.getName())) {
					match = c;
					break;
				}
			}

			if(match != null) {
				for(Class<?> c = start; c != match; c = c.getSuperclass()) {
					slots.add(new ClassDataSlot(StandardObjectStreamClass.lookup(c, true), false));
				}
				start = match.getSuperclass();
			}

			slots.add(new ClassDataSlot(d.getVariantFor(match), true));
		}

		for(Class<?> c = start; c != end; c = c.getSuperclass()) {
			slots.add(new ClassDataSlot(StandardObjectStreamClass.lookup(c, true), false));
		}

		Collections.reverse(slots);
		return slots.toArray(new ClassDataSlot[slots.size()]);
	}

	int getPrimDataSize() {
		return primDataSize;
	}

	int getNumObjFields() {
		return numObjFields;
	}

	void getPrimFieldValues(Object obj, byte[] buf) {
		fieldRefl.getPrimFieldValues(obj, buf);
	}

	void setPrimFieldValues(Object obj, byte[] buf) {
		fieldRefl.setPrimFieldValues(obj, buf);
	}

	void getObjFieldValues(Object obj, Object[] vals) {
		fieldRefl.getObjFieldValues(obj, vals);
	}

	void setObjFieldValues(Object obj, Object[] vals) {
		fieldRefl.setObjFieldValues(obj, vals);
	}

	private void computeFieldOffsets() throws InvalidClassException {
		primDataSize = 0;
		numObjFields = 0;
		int firstObjIndex = -1;

		for(int i = 0; i < fields.length; i++) {
			StandardObjectStreamField f = fields[i];
			switch(f.getTypeCode()) {
			case 'Z':
			case 'B':
				f.setOffset(primDataSize++);
				break;

			case 'C':
			case 'S':
				f.setOffset(primDataSize);
				primDataSize += 2;
				break;

			case 'I':
			case 'F':
				f.setOffset(primDataSize);
				primDataSize += 4;
				break;

			case 'J':
			case 'D':
				f.setOffset(primDataSize);
				primDataSize += 8;
				break;

			case '[':
			case 'L':
				f.setOffset(numObjFields++);
				if(firstObjIndex == -1) {
					firstObjIndex = i;
				}
				break;

			default:
				throw new InternalError();
			}
		}
		if(firstObjIndex != -1 && firstObjIndex + numObjFields != fields.length) {
			throw new InvalidClassException(name, "illegal field order");
		}
	}

	private StandardObjectStreamClass getVariantFor(Class<?> cl) throws InvalidClassException {
		if(this.cl == cl) {
			return this;
		}
		StandardObjectStreamClass desc = new StandardObjectStreamClass();
		if(isProxy) {
			desc.initProxy(cl, null, superDesc);
		} else {
			desc.initNonProxy(this, cl, null, superDesc);
		}
		return desc;
	}

	private static Constructor<?> getExternalizableConstructor(Class<?> cl) {
		try {
			Constructor<?> cons = cl.getDeclaredConstructor((Class<?>[])null);
			cons.setAccessible(true);
			return ((cons.getModifiers() & Modifier.PUBLIC) != 0) ? cons : null;
		} catch(NoSuchMethodException ex) {
			return null;
		}
	}

	private static Constructor<?> getSerializableConstructor(Class<?> cl) {
		Class<?> initCl = cl;
		while(Serializable.class.isAssignableFrom(initCl)) {
			if((initCl = initCl.getSuperclass()) == null) {
				return null;
			}
		}
		try {
			Constructor<?> cons = initCl.getDeclaredConstructor((Class<?>[])null);
			int mods = cons.getModifiers();
			if((mods & Modifier.PRIVATE) != 0 || ((mods & (Modifier.PUBLIC | Modifier.PROTECTED)) == 0 && !packageEquals(cl, initCl))) {
				return null;
			}
			cons = reflFactory.newConstructorForSerialization(cl, cons);
			cons.setAccessible(true);
			return cons;
		} catch(NoSuchMethodException ex) {
			return null;
		}
	}

	private static Method getInheritableMethod(Class<?> cl, String name, Class<?>[] argTypes, Class<?> returnType) {
		Method meth = null;
		Class<?> defCl = cl;
		while(defCl != null) {
			try {
				meth = defCl.getDeclaredMethod(name, argTypes);
				break;
			} catch(NoSuchMethodException ex) {
				defCl = defCl.getSuperclass();
			}
		}

		if((meth == null) || (meth.getReturnType() != returnType)) {
			return null;
		}
		meth.setAccessible(true);
		int mods = meth.getModifiers();
		if((mods & (Modifier.STATIC | Modifier.ABSTRACT)) != 0) {
			return null;
		} else if((mods & (Modifier.PUBLIC | Modifier.PROTECTED)) != 0) {
			return meth;
		} else if((mods & Modifier.PRIVATE) != 0) {
			return (cl == defCl) ? meth : null;
		} else {
			return packageEquals(cl, defCl) ? meth : null;
		}
	}

	private static Method getPrivateMethod(Class<?> cl, String name, Class<?>[] argTypes, Class<?> returnType) {
		try {
			Method meth = cl.getDeclaredMethod(name, argTypes);
			meth.setAccessible(true);
			int mods = meth.getModifiers();
			return ((meth.getReturnType() == returnType) && ((mods & Modifier.STATIC) == 0) && ((mods & Modifier.PRIVATE) != 0)) ? meth : null;
		} catch(NoSuchMethodException ex) {
			return null;
		}
	}

	private static boolean packageEquals(Class<?> cl1, Class<?> cl2) {
		return (cl1.getClassLoader() == cl2.getClassLoader() && getPackageName(cl1).equals(getPackageName(cl2)));
	}

	private static String getPackageName(Class<?> cl) {
		String s = cl.getName();
		int i = s.lastIndexOf('[');
		if(i >= 0) {
			s = s.substring(i + 2);
		}
		i = s.lastIndexOf('.');
		return (i >= 0) ? s.substring(0, i) : "";
	}

	private static boolean classNamesEqual(String name1, String name2) {
		name1 = name1.substring(name1.lastIndexOf('.') + 1);
		name2 = name2.substring(name2.lastIndexOf('.') + 1);
		return name1.equals(name2);
	}

	private static void throwMiscException(Throwable th) throws IOException {
		if(th instanceof RuntimeException) {
			throw (RuntimeException)th;
		} else if(th instanceof Error) {
			throw (Error)th;
		} else {
			IOException ex = new IOException("unexpected exception type");
			ex.initCause(th);
			throw ex;
		}
	}

	private static StandardObjectStreamField[] getSerialFields(Class<?> cl) throws InvalidClassException {
		StandardObjectStreamField[] fields;
		if(Serializable.class.isAssignableFrom(cl) && !Externalizable.class.isAssignableFrom(cl) && !Proxy.isProxyClass(cl) && !cl.isInterface()) {
			if((fields = getDeclaredSerialFields(cl)) == null) {
				fields = getDefaultSerialFields(cl);
			}
			Arrays.sort(fields);
		} else {
			fields = NO_FIELDS;
		}
		return fields;
	}

	private static StandardObjectStreamField[] getDeclaredSerialFields(Class<?> cl) throws InvalidClassException {
		StandardObjectStreamField[] serialPersistentFields = null;
		try {
			Field f = cl.getDeclaredField("serialPersistentFields");
			int mask = Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL;
			if((f.getModifiers() & mask) == mask) {
				f.setAccessible(true);
				serialPersistentFields = (StandardObjectStreamField[])f.get(null);
			}
		} catch(Exception ex) {
		}
		if(serialPersistentFields == null) {
			return null;
		} else if(serialPersistentFields.length == 0) {
			return NO_FIELDS;
		}

		StandardObjectStreamField[] boundFields = new StandardObjectStreamField[serialPersistentFields.length];
		Set<String> fieldNames = new HashSet<String>(serialPersistentFields.length);

		for(int i = 0; i < serialPersistentFields.length; i++) {
			StandardObjectStreamField spf = serialPersistentFields[i];

			String fname = spf.getName();
			if(fieldNames.contains(fname)) {
				throw new InvalidClassException("multiple serializable fields named " + fname);
			}
			fieldNames.add(fname);

			try {
				Field f = cl.getDeclaredField(fname);
				if((f.getType() == spf.getType()) && ((f.getModifiers() & Modifier.STATIC) == 0)) {
					boundFields[i] = new StandardObjectStreamField(f, spf.isUnshared(), true);
				}
			} catch(NoSuchFieldException ex) {
			}
			if(boundFields[i] == null) {
				boundFields[i] = new StandardObjectStreamField(fname, spf.getType(), spf.isUnshared());
			}
		}
		return boundFields;
	}

	private static StandardObjectStreamField[] getDefaultSerialFields(Class<?> cl) {
		Field[] clFields = cl.getDeclaredFields();
		ArrayList<StandardObjectStreamField> list = new ArrayList<StandardObjectStreamField>();
		int mask = Modifier.STATIC | Modifier.TRANSIENT;

		for(int i = 0; i < clFields.length; i++) {
			if((clFields[i].getModifiers() & mask) == 0) {
				list.add(new StandardObjectStreamField(clFields[i], false, true));
			}
		}
		int size = list.size();
		return (size == 0) ? NO_FIELDS : list.toArray(new StandardObjectStreamField[size]);
	}

	private static Long getDeclaredSUID(Class<?> cl) {
		try {
			Field f = cl.getDeclaredField("serialVersionUID");
			int mask = Modifier.STATIC | Modifier.FINAL;
			if((f.getModifiers() & mask) == mask) {
				f.setAccessible(true);
				return Long.valueOf(f.getLong(null));
			}
		} catch(Exception ex) {
		}
		return null;
	}

	private static class FieldReflector {
		private static final Unsafe unsafe; 
		
		static {
			try {
				Constructor<Unsafe> unsafeConstructor = Unsafe.class.getDeclaredConstructor();
				unsafeConstructor.setAccessible(true);
				unsafe = unsafeConstructor.newInstance();
			} catch(Throwable e) {
				throw new RuntimeException(e);
			}
		}

		
		
		private final StandardObjectStreamField[] fields;
		private final int numPrimFields;
		private final long[] readKeys;
		private final long[] writeKeys;
		private final int[] offsets;
		private final char[] typeCodes;
		private final Class<?>[] types;

		FieldReflector(StandardObjectStreamField[] fields) {
			this.fields = fields;
			int nfields = fields.length;
			readKeys = new long[nfields];
			writeKeys = new long[nfields];
			offsets = new int[nfields];
			typeCodes = new char[nfields];
			ArrayList<Class<?>> typeList = new ArrayList<Class<?>>();
			Set<Long> usedKeys = new HashSet<Long>();

			for(int i = 0; i < nfields; i++) {
				StandardObjectStreamField f = fields[i];
				Field rf = f.getField();
				long key = (rf != null) ? unsafe.objectFieldOffset(rf) : Unsafe.INVALID_FIELD_OFFSET;
				readKeys[i] = key;
				writeKeys[i] = usedKeys.add(key) ? key : Unsafe.INVALID_FIELD_OFFSET;
				offsets[i] = f.getOffset();
				typeCodes[i] = f.getTypeCode();
				if(!f.isPrimitive())
					typeList.add((rf != null) ? rf.getType() : null);
			}

			types = typeList.toArray(new Class<?>[typeList.size()]);
			numPrimFields = nfields - types.length;
		}

		StandardObjectStreamField[] getFields() {
			return fields;
		}

		void getPrimFieldValues(Object obj, byte[] buf) {
			if(obj == null) {
				throw new NullPointerException();
			}
			for(int i = 0; i < numPrimFields; i++) {
				long key = readKeys[i];
				int off = offsets[i];
				switch(typeCodes[i]) {
				case 'Z':
					Bits.putBoolean(buf, off, unsafe.getBoolean(obj, key));
					break;

				case 'B':
					buf[off] = unsafe.getByte(obj, key);
					break;

				case 'C':
					Bits.putChar(buf, off, unsafe.getChar(obj, key));
					break;

				case 'S':
					Bits.putShort(buf, off, unsafe.getShort(obj, key));
					break;

				case 'I':
					Bits.putInt(buf, off, unsafe.getInt(obj, key));
					break;

				case 'F':
					Bits.putFloat(buf, off, unsafe.getFloat(obj, key));
					break;

				case 'J':
					Bits.putLong(buf, off, unsafe.getLong(obj, key));
					break;

				case 'D':
					Bits.putDouble(buf, off, unsafe.getDouble(obj, key));
					break;

				default:
					throw new InternalError();
				}
			}
		}

		void setPrimFieldValues(Object obj, byte[] buf) {
			if(obj == null) {
				throw new NullPointerException();
			}
			for(int i = 0; i < numPrimFields; i++) {
				long key = writeKeys[i];
				if(key == Unsafe.INVALID_FIELD_OFFSET) {
					continue; // discard value
				}
				int off = offsets[i];
				switch(typeCodes[i]) {
				case 'Z':
					unsafe.putBoolean(obj, key, Bits.getBoolean(buf, off));
					break;

				case 'B':
					unsafe.putByte(obj, key, buf[off]);
					break;

				case 'C':
					unsafe.putChar(obj, key, Bits.getChar(buf, off));
					break;

				case 'S':
					unsafe.putShort(obj, key, Bits.getShort(buf, off));
					break;

				case 'I':
					unsafe.putInt(obj, key, Bits.getInt(buf, off));
					break;

				case 'F':
					unsafe.putFloat(obj, key, Bits.getFloat(buf, off));
					break;

				case 'J':
					unsafe.putLong(obj, key, Bits.getLong(buf, off));
					break;

				case 'D':
					unsafe.putDouble(obj, key, Bits.getDouble(buf, off));
					break;

				default:
					throw new InternalError();
				}
			}
		}

		void getObjFieldValues(Object obj, Object[] vals) {
			if(obj == null) {
				throw new NullPointerException();
			}
			for(int i = numPrimFields; i < fields.length; i++) {
				switch(typeCodes[i]) {
				case 'L':
				case '[':
					vals[offsets[i]] = unsafe.getObject(obj, readKeys[i]);
					break;

				default:
					throw new InternalError();
				}
			}
		}

		void setObjFieldValues(Object obj, Object[] vals) {
			if(obj == null) {
				throw new NullPointerException();
			}
			for(int i = numPrimFields; i < fields.length; i++) {
				long key = writeKeys[i];
				if(key == Unsafe.INVALID_FIELD_OFFSET) {
					continue; // discard value
				}
				switch(typeCodes[i]) {
				case 'L':
				case '[':
					Object val = vals[offsets[i]];
					if(val != null && !types[i - numPrimFields].isInstance(val)) {
						Field f = fields[i].getField();
						throw new ClassCastException("cannot assign instance of " + val.getClass().getName() + " to field " + f.getDeclaringClass().getName() + "." + f.getName() + " of type " + f.getType().getName() + " in instance of " + obj.getClass().getName());
					}
					unsafe.putObject(obj, key, val);
					break;

				default:
					throw new InternalError();
				}
			}
		}
	}

	private static FieldReflector getReflector(StandardObjectStreamField[] fields, StandardObjectStreamClass localDesc) throws InvalidClassException {
		Class<?> cl = (localDesc != null && fields.length > 0) ? localDesc.cl : null;
		processQueue(Caches.reflectorsQueue, Caches.reflectors);
		FieldReflectorKey key = new FieldReflectorKey(cl, fields, Caches.reflectorsQueue);
		Reference<?> ref = Caches.reflectors.get(key);
		Object entry = null;
		if(ref != null) {
			entry = ref.get();
		}
		EntryFuture future = null;
		if(entry == null) {
			EntryFuture newEntry = new EntryFuture();
			Reference<?> newRef = new SoftReference<EntryFuture>(newEntry);
			do {
				if(ref != null) {
					Caches.reflectors.remove(key, ref);
				}
				ref = Caches.reflectors.putIfAbsent(key, newRef);
				if(ref != null) {
					entry = ref.get();
				}
			} while(ref != null && entry == null);
			if(entry == null) {
				future = newEntry;
			}
		}

		if(entry instanceof FieldReflector) {
			return (FieldReflector)entry;
		} else if(entry instanceof EntryFuture) {
			entry = ((EntryFuture)entry).get();
		} else if(entry == null) {
			try {
				entry = new FieldReflector(matchFields(fields, localDesc));
			} catch(Throwable th) {
				entry = th;
			}
			future.set(entry);
			Caches.reflectors.put(key, new SoftReference<Object>(entry));
		}

		if(entry instanceof FieldReflector) {
			return (FieldReflector)entry;
		} else if(entry instanceof InvalidClassException) {
			throw (InvalidClassException)entry;
		} else if(entry instanceof RuntimeException) {
			throw (RuntimeException)entry;
		} else if(entry instanceof Error) {
			throw (Error)entry;
		} else {
			throw new InternalError("unexpected entry: " + entry);
		}
	}

	private static class FieldReflectorKey extends WeakReference<Class<?>> {

		private final String sigs;
		private final int hash;
		private final boolean nullClass;

		FieldReflectorKey(Class<?> cl, StandardObjectStreamField[] fields, ReferenceQueue<Class<?>> queue) {
			super(cl, queue);
			nullClass = (cl == null);
			StringBuilder sbuf = new StringBuilder();
			for(int i = 0; i < fields.length; i++) {
				StandardObjectStreamField f = fields[i];
				sbuf.append(f.getName()).append(f.getSignature());
			}
			sigs = sbuf.toString();
			hash = System.identityHashCode(cl) + sigs.hashCode();
		}

		public int hashCode() {
			return hash;
		}

		public boolean equals(Object obj) {
			if(obj == this) {
				return true;
			}

			if(obj instanceof FieldReflectorKey) {
				FieldReflectorKey other = (FieldReflectorKey)obj;
				Class<?> referent;
				return (nullClass ? other.nullClass : ((referent = get()) != null) && (referent == other.get())) && sigs.equals(other.sigs);
			} else {
				return false;
			}
		}
	}

	private static StandardObjectStreamField[] matchFields(StandardObjectStreamField[] fields, StandardObjectStreamClass localDesc) throws InvalidClassException {
		StandardObjectStreamField[] localFields = (localDesc != null) ? localDesc.fields : NO_FIELDS;

		StandardObjectStreamField[] matches = new StandardObjectStreamField[fields.length];
		for(int i = 0; i < fields.length; i++) {
			StandardObjectStreamField f = fields[i], m = null;
			for(int j = 0; j < localFields.length; j++) {
				StandardObjectStreamField lf = localFields[j];
				if(f.getName().equals(lf.getName())) {
					if((f.isPrimitive() || lf.isPrimitive()) && f.getTypeCode() != lf.getTypeCode()) {
						throw new InvalidClassException(localDesc.name, "incompatible types for field " + f.getName());
					}
					if(lf.getField() != null) {
						m = new StandardObjectStreamField(lf.getField(), lf.isUnshared(), false);
					} else {
						m = new StandardObjectStreamField(lf.getName(), lf.getSignature(), lf.isUnshared());
					}
				}
			}
			if(m == null) {
				m = new StandardObjectStreamField(f.getName(), f.getSignature(), false);
			}
			m.setOffset(f.getOffset());
			matches[i] = m;
		}
		return matches;
	}

	static void processQueue(ReferenceQueue<Class<?>> queue, ConcurrentMap<? extends WeakReference<Class<?>>, ?> map) {
		Reference<? extends Class<?>> ref;
		while((ref = queue.poll()) != null) {
			map.remove(ref);
		}
	}

	static class WeakClassKey extends WeakReference<Class<?>> {
		private final int hash;

		WeakClassKey(Class<?> cl, ReferenceQueue<Class<?>> refQueue) {
			super(cl, refQueue);
			hash = System.identityHashCode(cl);
		}

		public int hashCode() {
			return hash;
		}

		public boolean equals(Object obj) {
			if(obj == this) {
				return true;
			}

			if(obj instanceof WeakClassKey) {
				Object referent = get();
				return (referent != null) && (referent == ((WeakClassKey)obj).get());
			} else {
				return false;
			}
		}
	}
}
