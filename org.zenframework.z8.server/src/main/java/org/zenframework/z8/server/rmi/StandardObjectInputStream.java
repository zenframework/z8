package org.zenframework.z8.server.rmi;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.Externalizable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.InvalidObjectException;
import java.io.NotActiveException;
import java.io.ObjectInput;
import java.io.ObjectInputValidation;
import java.io.ObjectStreamConstants;
import java.io.StreamCorruptedException;
import java.io.UTFDataFormatException;
import java.io.WriteAbortedException;
import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Arrays;
import java.util.HashMap;

import org.zenframework.z8.server.rmi.StandardObjectOutputStream.SerialCallbackContext;

public class StandardObjectInputStream extends InputStream implements ObjectInput, ObjectStreamConstants {
	private static final int NULL_HANDLE = -1;
	private static final Object unsharedMarker = new Object();
	private static final HashMap<String, Class<?>> primClasses = new HashMap<String, Class<?>>(8, 1.0F);
	static {
		primClasses.put("boolean", boolean.class);
		primClasses.put("byte", byte.class);
		primClasses.put("char", char.class);
		primClasses.put("short", short.class);
		primClasses.put("int", int.class);
		primClasses.put("long", long.class);
		primClasses.put("float", float.class);
		primClasses.put("double", double.class);
		primClasses.put("void", void.class);
	}

	private final BlockDataInputStream bin;
	private final ValidationList vlist;
	private int depth;
	private boolean closed;

	private final HandleTable handles;
	private int passHandle = NULL_HANDLE;
	private boolean defaultDataEnd = false;

	private byte[] primVals;

	private final boolean enableOverride;
	private boolean enableResolve;

	private SerialCallbackContext curContext;

	public StandardObjectInputStream(InputStream in) throws IOException {
		bin = new BlockDataInputStream(in);
		handles = new HandleTable(10);
		vlist = new ValidationList();
		enableOverride = false;
		readStreamHeader();
		bin.setBlockDataMode(true);
	}

	protected StandardObjectInputStream() throws IOException, SecurityException {
		bin = null;
		handles = null;
		vlist = null;
		enableOverride = true;
	}

	public final Object readObject() throws IOException, ClassNotFoundException {
		if(enableOverride) {
			return readObjectOverride();
		}

		int outerHandle = passHandle;
		try {
			Object obj = readObject0(false);
			handles.markDependency(outerHandle, passHandle);
			ClassNotFoundException ex = handles.lookupException(passHandle);
			if(ex != null) {
				throw ex;
			}
			if(depth == 0) {
				vlist.doCallbacks();
			}
			return obj;
		} finally {
			passHandle = outerHandle;
			if(closed && depth == 0) {
				clear();
			}
		}
	}

	protected Object readObjectOverride() throws IOException, ClassNotFoundException {
		return null;
	}

	public Object readUnshared() throws IOException, ClassNotFoundException {
		int outerHandle = passHandle;
		try {
			Object obj = readObject0(true);
			handles.markDependency(outerHandle, passHandle);
			ClassNotFoundException ex = handles.lookupException(passHandle);
			if(ex != null) {
				throw ex;
			}
			if(depth == 0) {
				vlist.doCallbacks();
			}
			return obj;
		} finally {
			passHandle = outerHandle;
			if(closed && depth == 0) {
				clear();
			}
		}
	}

	public void defaultReadObject() throws IOException, ClassNotFoundException {
		SerialCallbackContext ctx = curContext;
		if(ctx == null) {
			throw new NotActiveException("not in call to readObject");
		}
		Object curObj = ctx.getObj();
		StandardObjectStreamClass curDesc = ctx.getDesc();
		bin.setBlockDataMode(false);
		defaultReadFields(curObj, curDesc);
		bin.setBlockDataMode(true);
		if(!curDesc.hasWriteObjectData()) {
			defaultDataEnd = true;
		}
		ClassNotFoundException ex = handles.lookupException(passHandle);
		if(ex != null) {
			throw ex;
		}
	}

	public StandardObjectInputStream.GetField readFields() throws IOException, ClassNotFoundException {
		SerialCallbackContext ctx = curContext;
		if(ctx == null) {
			throw new NotActiveException("not in call to readObject");
		}
		StandardObjectStreamClass curDesc = ctx.getDesc();
		bin.setBlockDataMode(false);
		GetFieldImpl getField = new GetFieldImpl(curDesc);
		getField.readFields();
		bin.setBlockDataMode(true);
		if(!curDesc.hasWriteObjectData()) {
			defaultDataEnd = true;
		}

		return getField;
	}

	public void registerValidation(ObjectInputValidation obj, int prio) throws NotActiveException, InvalidObjectException {
		if(depth == 0) {
			throw new NotActiveException("stream inactive");
		}
		vlist.register(obj, prio);
	}

	protected Class<?> resolveClass(StandardObjectStreamClass desc) throws IOException, ClassNotFoundException {
		String name = desc.getName();
		try {
			return Class.forName(name, false, latestUserDefinedLoader());
		} catch(ClassNotFoundException ex) {
			Class<?> cl = primClasses.get(name);
			if(cl != null) {
				return cl;
			} else {
				throw ex;
			}
		}
	}

	protected Class<?> resolveProxyClass(String[] interfaces) throws IOException, ClassNotFoundException {
		ClassLoader latestLoader = latestUserDefinedLoader();
		ClassLoader nonPublicLoader = null;
		boolean hasNonPublicInterface = false;

		Class<?>[] classObjs = new Class<?>[interfaces.length];
		for(int i = 0; i < interfaces.length; i++) {
			Class<?> cl = Class.forName(interfaces[i], false, latestLoader);
			if((cl.getModifiers() & Modifier.PUBLIC) == 0) {
				if(hasNonPublicInterface) {
					if(nonPublicLoader != cl.getClassLoader()) {
						throw new IllegalAccessError("conflicting non-public interface class loaders");
					}
				} else {
					nonPublicLoader = cl.getClassLoader();
					hasNonPublicInterface = true;
				}
			}
			classObjs[i] = cl;
		}
		try {
			return Proxy.getProxyClass(hasNonPublicInterface ? nonPublicLoader : latestLoader, classObjs);
		} catch(IllegalArgumentException e) {
			throw new ClassNotFoundException(null, e);
		}
	}

	protected Object resolveObject(Object obj) throws IOException {
		return obj;
	}

	protected boolean enableResolveObject(boolean enable) throws SecurityException {
		if(enable == enableResolve) {
			return enable;
		}
		if(enable) {
			SecurityManager sm = System.getSecurityManager();
			if(sm != null) {
				sm.checkPermission(SUBSTITUTION_PERMISSION);
			}
		}
		enableResolve = enable;
		return !enableResolve;
	}

	protected void readStreamHeader() throws IOException, StreamCorruptedException {
		short s0 = bin.readShort();
		short s1 = bin.readShort();
		if(s0 != STREAM_MAGIC || s1 != STREAM_VERSION) {
			throw new StreamCorruptedException(String.format("invalid stream header: %04X%04X", s0, s1));
		}
	}

	protected StandardObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException {
		StandardObjectStreamClass desc = new StandardObjectStreamClass();
		desc.readNonProxy(this);
		return desc;
	}

	public int read() throws IOException {
		return bin.read();
	}

	public int read(byte[] buf, int off, int len) throws IOException {
		if(buf == null) {
			throw new NullPointerException();
		}
		int endoff = off + len;
		if(off < 0 || len < 0 || endoff > buf.length || endoff < 0) {
			throw new IndexOutOfBoundsException();
		}
		return bin.read(buf, off, len, false);
	}

	public int available() throws IOException {
		return bin.available();
	}

	public void close() throws IOException {
		closed = true;
		if(depth == 0) {
			clear();
		}
		bin.close();
	}

	public boolean readBoolean() throws IOException {
		return bin.readBoolean();
	}

	public byte readByte() throws IOException {
		return bin.readByte();
	}

	public int readUnsignedByte() throws IOException {
		return bin.readUnsignedByte();
	}

	public char readChar() throws IOException {
		return bin.readChar();
	}

	public short readShort() throws IOException {
		return bin.readShort();
	}

	public int readUnsignedShort() throws IOException {
		return bin.readUnsignedShort();
	}

	public int readInt() throws IOException {
		return bin.readInt();
	}

	public long readLong() throws IOException {
		return bin.readLong();
	}

	public float readFloat() throws IOException {
		return bin.readFloat();
	}

	public double readDouble() throws IOException {
		return bin.readDouble();
	}

	public void readFully(byte[] buf) throws IOException {
		bin.readFully(buf, 0, buf.length, false);
	}

	public void readFully(byte[] buf, int off, int len) throws IOException {
		int endoff = off + len;
		if(off < 0 || len < 0 || endoff > buf.length || endoff < 0) {
			throw new IndexOutOfBoundsException();
		}
		bin.readFully(buf, off, len, false);
	}

	public int skipBytes(int len) throws IOException {
		return bin.skipBytes(len);
	}

	@Deprecated
	public String readLine() throws IOException {
		return bin.readLine();
	}

	public String readUTF() throws IOException {
		return bin.readUTF();
	}

	public static abstract class GetField {

		public abstract StandardObjectStreamClass getObjectStreamClass();

		public abstract boolean defaulted(String name) throws IOException;

		public abstract boolean get(String name, boolean val) throws IOException;

		public abstract byte get(String name, byte val) throws IOException;

		public abstract char get(String name, char val) throws IOException;

		public abstract short get(String name, short val) throws IOException;

		public abstract int get(String name, int val) throws IOException;

		public abstract long get(String name, long val) throws IOException;

		public abstract float get(String name, float val) throws IOException;

		public abstract double get(String name, double val) throws IOException;

		public abstract Object get(String name, Object val) throws IOException;
	}

	private void clear() {
		handles.clear();
		vlist.clear();
	}

	private Object readObject0(boolean unshared) throws IOException {
		boolean oldMode = bin.getBlockDataMode();
		if(oldMode) {
			int remain = bin.currentBlockRemaining();
			if(remain > 0) {
				throw new RuntimeException("OptionalDataException " + remain);
			} else if(defaultDataEnd) {
				/*
				 * Fix for 4360508: stream is currently at the end of a field
				 * value block written via default serialization; since there is
				 * no terminating TC_ENDBLOCKDATA tag, simulate
				 * end-of-custom-data behavior explicitly.
				 */
				throw new RuntimeException("OptionalDataException true");
			}
			bin.setBlockDataMode(false);
		}

		byte tc;
		while((tc = bin.peekByte()) == TC_RESET) {
			bin.readByte();
			handleReset();
		}

		depth++;
		try {
			switch(tc) {
			case TC_NULL:
				return readNull();

			case TC_REFERENCE:
				return readHandle(unshared);

			case TC_CLASS:
				return readClass(unshared);

			case TC_CLASSDESC:
			case TC_PROXYCLASSDESC:
				return readClassDesc(unshared);

			case TC_STRING:
			case TC_LONGSTRING:
				return checkResolve(readString(unshared));

			case TC_ARRAY:
				return checkResolve(readArray(unshared));

			case TC_ENUM:
				return checkResolve(readEnum(unshared));

			case TC_OBJECT:
				return checkResolve(readOrdinaryObject(unshared));

			case TC_EXCEPTION:
				IOException ex = readFatalException();
				throw new WriteAbortedException("writing aborted", ex);

			case TC_BLOCKDATA:
			case TC_BLOCKDATALONG:
				if(oldMode) {
					bin.setBlockDataMode(true);
					bin.peek(); // force header read
					throw new RuntimeException("OptionalDataException " + bin.currentBlockRemaining());
				} else {
					throw new StreamCorruptedException("unexpected block data");
				}

			case TC_ENDBLOCKDATA:
				if(oldMode) {
					throw new RuntimeException("OptionalDataException true");
				} else {
					throw new StreamCorruptedException("unexpected end of block data");
				}

			default:
				throw new StreamCorruptedException(String.format("invalid type code: %02X", tc));
			}
		} finally {
			depth--;
			bin.setBlockDataMode(oldMode);
		}
	}

	private Object checkResolve(Object obj) throws IOException {
		if(!enableResolve || handles.lookupException(passHandle) != null) {
			return obj;
		}
		Object rep = resolveObject(obj);
		if(rep != obj) {
			handles.setObject(passHandle, rep);
		}
		return rep;
	}

	String readTypeString() throws IOException {
		int oldHandle = passHandle;
		try {
			byte tc = bin.peekByte();
			switch(tc) {
			case TC_NULL:
				return (String)readNull();

			case TC_REFERENCE:
				return (String)readHandle(false);

			case TC_STRING:
			case TC_LONGSTRING:
				return readString(false);

			default:
				throw new StreamCorruptedException(String.format("invalid type code: %02X", tc));
			}
		} finally {
			passHandle = oldHandle;
		}
	}

	private Object readNull() throws IOException {
		if(bin.readByte() != TC_NULL) {
			throw new InternalError();
		}
		passHandle = NULL_HANDLE;
		return null;
	}

	private Object readHandle(boolean unshared) throws IOException {
		if(bin.readByte() != TC_REFERENCE) {
			throw new InternalError();
		}
		passHandle = bin.readInt() - baseWireHandle;
		if(passHandle < 0 || passHandle >= handles.size()) {
			throw new StreamCorruptedException(String.format("invalid handle value: %08X", passHandle + baseWireHandle));
		}
		if(unshared) {
			throw new InvalidObjectException("cannot read back reference as unshared");
		}

		Object obj = handles.lookupObject(passHandle);
		if(obj == unsharedMarker) {
			throw new InvalidObjectException("cannot read back reference to unshared object");
		}
		return obj;
	}

	private Class<?> readClass(boolean unshared) throws IOException {
		if(bin.readByte() != TC_CLASS) {
			throw new InternalError();
		}
		StandardObjectStreamClass desc = readClassDesc(false);
		Class<?> cl = desc.forClass();
		passHandle = handles.assign(unshared ? unsharedMarker : cl);

		ClassNotFoundException resolveEx = desc.getResolveException();
		if(resolveEx != null) {
			handles.markException(passHandle, resolveEx);
		}

		handles.finish(passHandle);
		return cl;
	}

	private StandardObjectStreamClass readClassDesc(boolean unshared) throws IOException {
		byte tc = bin.peekByte();
		switch(tc) {
		case TC_NULL:
			return (StandardObjectStreamClass)readNull();

		case TC_REFERENCE:
			return (StandardObjectStreamClass)readHandle(unshared);

		case TC_PROXYCLASSDESC:
			return readProxyDesc(unshared);

		case TC_CLASSDESC:
			return readNonProxyDesc(unshared);

		default:
			throw new StreamCorruptedException(String.format("invalid type code: %02X", tc));
		}
	}

	private StandardObjectStreamClass readProxyDesc(boolean unshared) throws IOException {
		if(bin.readByte() != TC_PROXYCLASSDESC) {
			throw new InternalError();
		}

		StandardObjectStreamClass desc = new StandardObjectStreamClass();
		int descHandle = handles.assign(unshared ? unsharedMarker : desc);
		passHandle = NULL_HANDLE;

		int numIfaces = bin.readInt();
		String[] ifaces = new String[numIfaces];
		for(int i = 0; i < numIfaces; i++) {
			ifaces[i] = bin.readUTF();
		}

		Class<?> cl = null;
		ClassNotFoundException resolveEx = null;
		bin.setBlockDataMode(true);
		try {
			if((cl = resolveProxyClass(ifaces)) == null) {
				resolveEx = new ClassNotFoundException("null class");
			} else if(!Proxy.isProxyClass(cl)) {
				throw new InvalidClassException("Not a proxy");
			}
		} catch(ClassNotFoundException ex) {
			resolveEx = ex;
		}
		skipCustomData();

		desc.initProxy(cl, resolveEx, readClassDesc(false));

		handles.finish(descHandle);
		passHandle = descHandle;
		return desc;
	}

	private StandardObjectStreamClass readNonProxyDesc(boolean unshared) throws IOException {
		if(bin.readByte() != TC_CLASSDESC) {
			throw new InternalError();
		}

		StandardObjectStreamClass desc = new StandardObjectStreamClass();
		int descHandle = handles.assign(unshared ? unsharedMarker : desc);
		passHandle = NULL_HANDLE;

		StandardObjectStreamClass readDesc = null;
		try {
			readDesc = readClassDescriptor();
		} catch(ClassNotFoundException ex) {
			throw (IOException)new InvalidClassException("failed to read class descriptor").initCause(ex);
		}

		Class<?> cl = null;
		ClassNotFoundException resolveEx = null;
		bin.setBlockDataMode(true);
		try {
			if((cl = resolveClass(readDesc)) == null) {
				resolveEx = new ClassNotFoundException("null class");
			}
		} catch(ClassNotFoundException ex) {
			resolveEx = ex;
		}
		skipCustomData();

		desc.initNonProxy(readDesc, cl, resolveEx, readClassDesc(false));

		handles.finish(descHandle);
		passHandle = descHandle;
		return desc;
	}

	private String readString(boolean unshared) throws IOException {
		String str;
		byte tc = bin.readByte();
		switch(tc) {
		case TC_STRING:
			str = bin.readUTF();
			break;

		case TC_LONGSTRING:
			str = bin.readLongUTF();
			break;

		default:
			throw new StreamCorruptedException(String.format("invalid type code: %02X", tc));
		}
		passHandle = handles.assign(unshared ? unsharedMarker : str);
		handles.finish(passHandle);
		return str;
	}

	private Object readArray(boolean unshared) throws IOException {
		if(bin.readByte() != TC_ARRAY) {
			throw new InternalError();
		}

		StandardObjectStreamClass desc = readClassDesc(false);
		int len = bin.readInt();

		Object array = null;
		Class<?> cl, ccl = null;
		if((cl = desc.forClass()) != null) {
			ccl = cl.getComponentType();
			array = Array.newInstance(ccl, len);
		}

		int arrayHandle = handles.assign(unshared ? unsharedMarker : array);
		ClassNotFoundException resolveEx = desc.getResolveException();
		if(resolveEx != null) {
			handles.markException(arrayHandle, resolveEx);
		}

		if(ccl == null) {
			for(int i = 0; i < len; i++) {
				readObject0(false);
			}
		} else if(ccl.isPrimitive()) {
			if(ccl == Integer.TYPE) {
				bin.readInts((int[])array, 0, len);
			} else if(ccl == Byte.TYPE) {
				bin.readFully((byte[])array, 0, len, true);
			} else if(ccl == Long.TYPE) {
				bin.readLongs((long[])array, 0, len);
			} else if(ccl == Float.TYPE) {
				bin.readFloats((float[])array, 0, len);
			} else if(ccl == Double.TYPE) {
				bin.readDoubles((double[])array, 0, len);
			} else if(ccl == Short.TYPE) {
				bin.readShorts((short[])array, 0, len);
			} else if(ccl == Character.TYPE) {
				bin.readChars((char[])array, 0, len);
			} else if(ccl == Boolean.TYPE) {
				bin.readBooleans((boolean[])array, 0, len);
			} else {
				throw new InternalError();
			}
		} else {
			Object[] oa = (Object[])array;
			for(int i = 0; i < len; i++) {
				oa[i] = readObject0(false);
				handles.markDependency(arrayHandle, passHandle);
			}
		}

		handles.finish(arrayHandle);
		passHandle = arrayHandle;
		return array;
	}

	private Enum<?> readEnum(boolean unshared) throws IOException {
		if(bin.readByte() != TC_ENUM) {
			throw new InternalError();
		}

		StandardObjectStreamClass desc = readClassDesc(false);
		if(!desc.isEnum()) {
			throw new InvalidClassException("non-enum class: " + desc);
		}

		int enumHandle = handles.assign(unshared ? unsharedMarker : null);
		ClassNotFoundException resolveEx = desc.getResolveException();
		if(resolveEx != null) {
			handles.markException(enumHandle, resolveEx);
		}

		String name = readString(false);
		Enum<?> result = null;
		Class<?> cl = desc.forClass();
		if(cl != null) {
			try {
				@SuppressWarnings({ "unchecked", "rawtypes" })
				Enum<?> en = Enum.valueOf((Class)cl, name);
				result = en;
			} catch(IllegalArgumentException ex) {
				throw (IOException)new InvalidObjectException("enum constant " + name + " does not exist in " + cl).initCause(ex);
			}
			if(!unshared) {
				handles.setObject(enumHandle, result);
			}
		}

		handles.finish(enumHandle);
		passHandle = enumHandle;
		return result;
	}

	private Object readOrdinaryObject(boolean unshared) throws IOException {
		if(bin.readByte() != TC_OBJECT) {
			throw new InternalError();
		}

		StandardObjectStreamClass desc = readClassDesc(false);
		desc.checkDeserialize();

		Class<?> cl = desc.forClass();
		if(cl == String.class || cl == Class.class || cl == StandardObjectStreamClass.class) {
			throw new InvalidClassException("invalid class descriptor");
		}

		Object obj;
		try {
			obj = desc.isInstantiable() ? desc.newInstance() : null;
		} catch(Exception ex) {
			throw (IOException)new InvalidClassException(desc.forClass().getName(), "unable to create instance").initCause(ex);
		}

		passHandle = handles.assign(unshared ? unsharedMarker : obj);
		ClassNotFoundException resolveEx = desc.getResolveException();
		if(resolveEx != null) {
			handles.markException(passHandle, resolveEx);
		}

		if(desc.isExternalizable()) {
			readExternalData((Externalizable)obj, desc);
		} else {
			readSerialData(obj, desc);
		}

		handles.finish(passHandle);

		if(obj != null && handles.lookupException(passHandle) == null && desc.hasReadResolveMethod()) {
			Object rep = desc.invokeReadResolve(obj);
			if(unshared && rep.getClass().isArray()) {
				rep = cloneArray(rep);
			}
			if(rep != obj) {
				handles.setObject(passHandle, obj = rep);
			}
		}

		return obj;
	}

	private void readExternalData(Externalizable obj, StandardObjectStreamClass desc) throws IOException {
		SerialCallbackContext oldContext = curContext;
		if(oldContext != null)
			oldContext.check();
		curContext = null;
		try {
			boolean blocked = desc.hasBlockExternalData();
			if(blocked) {
				bin.setBlockDataMode(true);
			}
			if(obj != null) {
				try {
					obj.readExternal(this);
				} catch(ClassNotFoundException ex) {
					handles.markException(passHandle, ex);
				}
			}
			if(blocked) {
				skipCustomData();
			}
		} finally {
			if(oldContext != null)
				oldContext.check();
			curContext = oldContext;
		}
	}

	private void readSerialData(Object obj, StandardObjectStreamClass desc) throws IOException {
		StandardObjectStreamClass.ClassDataSlot[] slots = desc.getClassDataLayout();
		for(int i = 0; i < slots.length; i++) {
			StandardObjectStreamClass slotDesc = slots[i].desc;

			if(slots[i].hasData) {
				if(obj == null || handles.lookupException(passHandle) != null) {
					defaultReadFields(null, slotDesc); // skip field values
				} else if(slotDesc.hasReadObjectMethod()) {
					SerialCallbackContext oldContext = curContext;
					if(oldContext != null)
						oldContext.check();
					try {
						curContext = new SerialCallbackContext(obj, slotDesc);

						bin.setBlockDataMode(true);
						slotDesc.invokeReadObject(obj, this);
					} catch(ClassNotFoundException ex) {
						handles.markException(passHandle, ex);
					} finally {
						curContext.setUsed();
						if(oldContext != null)
							oldContext.check();
						curContext = oldContext;
					}

					defaultDataEnd = false;
				} else {
					defaultReadFields(obj, slotDesc);
				}

				if(slotDesc.hasWriteObjectData()) {
					skipCustomData();
				} else {
					bin.setBlockDataMode(false);
				}
			} else {
				if(obj != null && slotDesc.hasReadObjectNoDataMethod() && handles.lookupException(passHandle) == null) {
					slotDesc.invokeReadObjectNoData(obj);
				}
			}
		}
	}

	private void skipCustomData() throws IOException {
		int oldHandle = passHandle;
		for(;;) {
			if(bin.getBlockDataMode()) {
				bin.skipBlockData();
				bin.setBlockDataMode(false);
			}
			switch(bin.peekByte()) {
			case TC_BLOCKDATA:
			case TC_BLOCKDATALONG:
				bin.setBlockDataMode(true);
				break;

			case TC_ENDBLOCKDATA:
				bin.readByte();
				passHandle = oldHandle;
				return;

			default:
				readObject0(false);
				break;
			}
		}
	}

	private void defaultReadFields(Object obj, StandardObjectStreamClass desc) throws IOException {
		Class<?> cl = desc.forClass();
		if(cl != null && obj != null && !cl.isInstance(obj)) {
			throw new ClassCastException();
		}

		int primDataSize = desc.getPrimDataSize();
		if(primVals == null || primVals.length < primDataSize) {
			primVals = new byte[primDataSize];
		}
		bin.readFully(primVals, 0, primDataSize, false);
		if(obj != null) {
			desc.setPrimFieldValues(obj, primVals);
		}

		int objHandle = passHandle;
		StandardObjectStreamField[] fields = desc.getFields(false);
		Object[] objVals = new Object[desc.getNumObjFields()];
		int numPrimFields = fields.length - objVals.length;
		for(int i = 0; i < objVals.length; i++) {
			StandardObjectStreamField f = fields[numPrimFields + i];
			objVals[i] = readObject0(f.isUnshared());
			if(f.getField() != null) {
				handles.markDependency(objHandle, passHandle);
			}
		}
		if(obj != null) {
			desc.setObjFieldValues(obj, objVals);
		}
		passHandle = objHandle;
	}

	private IOException readFatalException() throws IOException {
		if(bin.readByte() != TC_EXCEPTION) {
			throw new InternalError();
		}
		clear();
		return (IOException)readObject0(false);
	}

	private void handleReset() throws StreamCorruptedException {
		if(depth > 0) {
			throw new StreamCorruptedException("unexpected reset; recursion depth: " + depth);
		}
		clear();
	}

//	private static native void bytesToFloats(byte[] src, int srcpos, float[] dst, int dstpos, int nfloats);
//	private static native void bytesToDoubles(byte[] src, int srcpos, double[] dst, int dstpos, int ndoubles);

	@SuppressWarnings("restriction")
	private static ClassLoader latestUserDefinedLoader() {
		return sun.misc.VM.latestUserDefinedLoader();
	}

	private class GetFieldImpl extends GetField {
		private final StandardObjectStreamClass desc;
		private final byte[] primVals;
		private final Object[] objVals;
		private final int[] objHandles;

		GetFieldImpl(StandardObjectStreamClass desc) {
			this.desc = desc;
			primVals = new byte[desc.getPrimDataSize()];
			objVals = new Object[desc.getNumObjFields()];
			objHandles = new int[objVals.length];
		}

		public StandardObjectStreamClass getObjectStreamClass() {
			return desc;
		}

		public boolean defaulted(String name) throws IOException {
			return (getFieldOffset(name, null) < 0);
		}

		public boolean get(String name, boolean val) throws IOException {
			int off = getFieldOffset(name, Boolean.TYPE);
			return (off >= 0) ? Bits.getBoolean(primVals, off) : val;
		}

		public byte get(String name, byte val) throws IOException {
			int off = getFieldOffset(name, Byte.TYPE);
			return (off >= 0) ? primVals[off] : val;
		}

		public char get(String name, char val) throws IOException {
			int off = getFieldOffset(name, Character.TYPE);
			return (off >= 0) ? Bits.getChar(primVals, off) : val;
		}

		public short get(String name, short val) throws IOException {
			int off = getFieldOffset(name, Short.TYPE);
			return (off >= 0) ? Bits.getShort(primVals, off) : val;
		}

		public int get(String name, int val) throws IOException {
			int off = getFieldOffset(name, Integer.TYPE);
			return (off >= 0) ? Bits.getInt(primVals, off) : val;
		}

		public float get(String name, float val) throws IOException {
			int off = getFieldOffset(name, Float.TYPE);
			return (off >= 0) ? Bits.getFloat(primVals, off) : val;
		}

		public long get(String name, long val) throws IOException {
			int off = getFieldOffset(name, Long.TYPE);
			return (off >= 0) ? Bits.getLong(primVals, off) : val;
		}

		public double get(String name, double val) throws IOException {
			int off = getFieldOffset(name, Double.TYPE);
			return (off >= 0) ? Bits.getDouble(primVals, off) : val;
		}

		public Object get(String name, Object val) throws IOException {
			int off = getFieldOffset(name, Object.class);
			if(off >= 0) {
				int objHandle = objHandles[off];
				handles.markDependency(passHandle, objHandle);
				return (handles.lookupException(objHandle) == null) ? objVals[off] : null;
			} else {
				return val;
			}
		}

		void readFields() throws IOException {
			bin.readFully(primVals, 0, primVals.length, false);

			int oldHandle = passHandle;
			StandardObjectStreamField[] fields = desc.getFields(false);
			int numPrimFields = fields.length - objVals.length;
			for(int i = 0; i < objVals.length; i++) {
				objVals[i] = readObject0(fields[numPrimFields + i].isUnshared());
				objHandles[i] = passHandle;
			}
			passHandle = oldHandle;
		}

		private int getFieldOffset(String name, Class<?> type) {
			StandardObjectStreamField field = desc.getField(name, type);
			if(field != null) {
				return field.getOffset();
			} else if(desc.getLocalDesc().getField(name, type) != null) {
				return -1;
			} else {
				throw new IllegalArgumentException("no such field " + name + " with type " + type);
			}
		}
	}

	private static class ValidationList {

		private static class Callback {
			final ObjectInputValidation obj;
			final int priority;
			Callback next;
			final AccessControlContext acc;

			Callback(ObjectInputValidation obj, int priority, Callback next, AccessControlContext acc) {
				this.obj = obj;
				this.priority = priority;
				this.next = next;
				this.acc = acc;
			}
		}

		private Callback list;

		ValidationList() {
		}

		void register(ObjectInputValidation obj, int priority) throws InvalidObjectException {
			if(obj == null) {
				throw new InvalidObjectException("null callback");
			}

			Callback prev = null, cur = list;
			while(cur != null && priority < cur.priority) {
				prev = cur;
				cur = cur.next;
			}
			AccessControlContext acc = AccessController.getContext();
			if(prev != null) {
				prev.next = new Callback(obj, priority, cur, acc);
			} else {
				list = new Callback(obj, priority, list, acc);
			}
		}

		void doCallbacks() throws InvalidObjectException {
			try {
				while(list != null) {
					AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {
						public Void run() throws InvalidObjectException {
							list.obj.validateObject();
							return null;
						}
					}, list.acc);
					list = list.next;
				}
			} catch(PrivilegedActionException ex) {
				list = null;
				throw (InvalidObjectException)ex.getException();
			}
		}

		public void clear() {
			list = null;
		}
	}

	private static class PeekInputStream extends InputStream {
		private final InputStream in;
		private int peekb = -1;

		PeekInputStream(InputStream in) {
			this.in = in;
		}

		int peek() throws IOException {
			return (peekb >= 0) ? peekb : (peekb = in.read());
		}

		public int read() throws IOException {
			if(peekb >= 0) {
				int v = peekb;
				peekb = -1;
				return v;
			} else {
				return in.read();
			}
		}

		public int read(byte[] b, int off, int len) throws IOException {
			if(len == 0) {
				return 0;
			} else if(peekb < 0) {
				return in.read(b, off, len);
			} else {
				b[off++] = (byte)peekb;
				len--;
				peekb = -1;
				int n = in.read(b, off, len);
				return (n >= 0) ? (n + 1) : 1;
			}
		}

		void readFully(byte[] b, int off, int len) throws IOException {
			int n = 0;
			while(n < len) {
				int count = read(b, off + n, len - n);
				if(count < 0) {
					throw new EOFException();
				}
				n += count;
			}
		}

		public long skip(long n) throws IOException {
			if(n <= 0) {
				return 0;
			}
			int skipped = 0;
			if(peekb >= 0) {
				peekb = -1;
				skipped++;
				n--;
			}
			return skipped + skip(n);
		}

		public int available() throws IOException {
			return in.available() + ((peekb >= 0) ? 1 : 0);
		}

		public void close() throws IOException {
			in.close();
		}
	}

	private class BlockDataInputStream extends InputStream implements DataInput {
		private static final int MAX_BLOCK_SIZE = 1024;
		private static final int MAX_HEADER_SIZE = 5;
		private static final int CHAR_BUF_SIZE = 256;
		private static final int HEADER_BLOCKED = -2;

		private final byte[] buf = new byte[MAX_BLOCK_SIZE];
		private final byte[] hbuf = new byte[MAX_HEADER_SIZE];
		private final char[] cbuf = new char[CHAR_BUF_SIZE];

		private boolean blkmode = false;

		private int pos = 0;
		private int end = -1;
		private int unread = 0;

		private final PeekInputStream in;
		private final DataInputStream din;

		BlockDataInputStream(InputStream in) {
			this.in = new PeekInputStream(in);
			din = new DataInputStream(this);
		}

		boolean setBlockDataMode(boolean newmode) throws IOException {
			if(blkmode == newmode) {
				return blkmode;
			}
			if(newmode) {
				pos = 0;
				end = 0;
				unread = 0;
			} else if(pos < end) {
				throw new IllegalStateException("unread block data");
			}
			blkmode = newmode;
			return !blkmode;
		}

		boolean getBlockDataMode() {
			return blkmode;
		}

		void skipBlockData() throws IOException {
			if(!blkmode) {
				throw new IllegalStateException("not in block data mode");
			}
			while(end >= 0) {
				refill();
			}
		}

		private int readBlockHeader(boolean canBlock) throws IOException {
			if(defaultDataEnd) {
				return -1;
			}
			try {
				for(;;) {
					int avail = canBlock ? Integer.MAX_VALUE : in.available();
					if(avail == 0) {
						return HEADER_BLOCKED;
					}

					int tc = in.peek();
					switch(tc) {
					case TC_BLOCKDATA:
						if(avail < 2) {
							return HEADER_BLOCKED;
						}
						in.readFully(hbuf, 0, 2);
						return hbuf[1] & 0xFF;

					case TC_BLOCKDATALONG:
						if(avail < 5) {
							return HEADER_BLOCKED;
						}
						in.readFully(hbuf, 0, 5);
						int len = Bits.getInt(hbuf, 1);
						if(len < 0) {
							throw new StreamCorruptedException("illegal block data header length: " + len);
						}
						return len;
					case TC_RESET:
						in.read();
						handleReset();
						break;

					default:
						if(tc >= 0 && (tc < TC_BASE || tc > TC_MAX)) {
							throw new StreamCorruptedException(String.format("invalid type code: %02X", tc));
						}
						return -1;
					}
				}
			} catch(EOFException ex) {
				throw new StreamCorruptedException("unexpected EOF while reading block data header");
			}
		}

		private void refill() throws IOException {
			try {
				do {
					pos = 0;
					if(unread > 0) {
						int n = in.read(buf, 0, Math.min(unread, MAX_BLOCK_SIZE));
						if(n >= 0) {
							end = n;
							unread -= n;
						} else {
							throw new StreamCorruptedException("unexpected EOF in middle of data block");
						}
					} else {
						int n = readBlockHeader(true);
						if(n >= 0) {
							end = 0;
							unread = n;
						} else {
							end = -1;
							unread = 0;
						}
					}
				} while(pos == end);
			} catch(IOException ex) {
				pos = 0;
				end = -1;
				unread = 0;
				throw ex;
			}
		}

		int currentBlockRemaining() {
			if(blkmode) {
				return (end >= 0) ? (end - pos) + unread : 0;
			} else {
				throw new IllegalStateException();
			}
		}

		int peek() throws IOException {
			if(blkmode) {
				if(pos == end) {
					refill();
				}
				return (end >= 0) ? (buf[pos] & 0xFF) : -1;
			} else {
				return in.peek();
			}
		}

		byte peekByte() throws IOException {
			int val = peek();
			if(val < 0) {
				throw new EOFException();
			}
			return (byte)val;
		}

		public int read() throws IOException {
			if(blkmode) {
				if(pos == end) {
					refill();
				}
				return (end >= 0) ? (buf[pos++] & 0xFF) : -1;
			} else {
				return in.read();
			}
		}

		public int read(byte[] b, int off, int len) throws IOException {
			return read(b, off, len, false);
		}

		public long skip(long len) throws IOException {
			long remain = len;
			while(remain > 0) {
				if(blkmode) {
					if(pos == end) {
						refill();
					}
					if(end < 0) {
						break;
					}
					int nread = (int)Math.min(remain, end - pos);
					remain -= nread;
					pos += nread;
				} else {
					int nread = (int)Math.min(remain, MAX_BLOCK_SIZE);
					if((nread = in.read(buf, 0, nread)) < 0) {
						break;
					}
					remain -= nread;
				}
			}
			return len - remain;
		}

		public int available() throws IOException {
			if(blkmode) {
				if((pos == end) && (unread == 0)) {
					int n;
					while((n = readBlockHeader(false)) == 0)
						;
					switch(n) {
					case HEADER_BLOCKED:
						break;

					case -1:
						pos = 0;
						end = -1;
						break;

					default:
						pos = 0;
						end = 0;
						unread = n;
						break;
					}
				}
				int unreadAvail = (unread > 0) ? Math.min(in.available(), unread) : 0;
				return (end >= 0) ? (end - pos) + unreadAvail : 0;
			} else {
				return in.available();
			}
		}

		public void close() throws IOException {
			if(blkmode) {
				pos = 0;
				end = -1;
				unread = 0;
			}
			in.close();
		}

		int read(byte[] b, int off, int len, boolean copy) throws IOException {
			if(len == 0) {
				return 0;
			} else if(blkmode) {
				if(pos == end) {
					refill();
				}
				if(end < 0) {
					return -1;
				}
				int nread = Math.min(len, end - pos);
				System.arraycopy(buf, pos, b, off, nread);
				pos += nread;
				return nread;
			} else if(copy) {
				int nread = in.read(buf, 0, Math.min(len, MAX_BLOCK_SIZE));
				if(nread > 0) {
					System.arraycopy(buf, 0, b, off, nread);
				}
				return nread;
			} else {
				return in.read(b, off, len);
			}
		}

		public void readFully(byte[] b) throws IOException {
			readFully(b, 0, b.length, false);
		}

		public void readFully(byte[] b, int off, int len) throws IOException {
			readFully(b, off, len, false);
		}

		public void readFully(byte[] b, int off, int len, boolean copy) throws IOException {
			while(len > 0) {
				int n = read(b, off, len, copy);
				if(n < 0) {
					throw new EOFException();
				}
				off += n;
				len -= n;
			}
		}

		public int skipBytes(int n) throws IOException {
			return din.skipBytes(n);
		}

		public boolean readBoolean() throws IOException {
			int v = read();
			if(v < 0) {
				throw new EOFException();
			}
			return (v != 0);
		}

		public byte readByte() throws IOException {
			int v = read();
			if(v < 0) {
				throw new EOFException();
			}
			return (byte)v;
		}

		public int readUnsignedByte() throws IOException {
			int v = read();
			if(v < 0) {
				throw new EOFException();
			}
			return v;
		}

		public char readChar() throws IOException {
			if(!blkmode) {
				pos = 0;
				in.readFully(buf, 0, 2);
			} else if(end - pos < 2) {
				return din.readChar();
			}
			char v = Bits.getChar(buf, pos);
			pos += 2;
			return v;
		}

		public short readShort() throws IOException {
			if(!blkmode) {
				pos = 0;
				in.readFully(buf, 0, 2);
			} else if(end - pos < 2) {
				return din.readShort();
			}
			short v = Bits.getShort(buf, pos);
			pos += 2;
			return v;
		}

		public int readUnsignedShort() throws IOException {
			if(!blkmode) {
				pos = 0;
				in.readFully(buf, 0, 2);
			} else if(end - pos < 2) {
				return din.readUnsignedShort();
			}
			int v = Bits.getShort(buf, pos) & 0xFFFF;
			pos += 2;
			return v;
		}

		public int readInt() throws IOException {
			if(!blkmode) {
				pos = 0;
				in.readFully(buf, 0, 4);
			} else if(end - pos < 4) {
				return din.readInt();
			}
			int v = Bits.getInt(buf, pos);
			pos += 4;
			return v;
		}

		public float readFloat() throws IOException {
			if(!blkmode) {
				pos = 0;
				in.readFully(buf, 0, 4);
			} else if(end - pos < 4) {
				return din.readFloat();
			}
			float v = Bits.getFloat(buf, pos);
			pos += 4;
			return v;
		}

		public long readLong() throws IOException {
			if(!blkmode) {
				pos = 0;
				in.readFully(buf, 0, 8);
			} else if(end - pos < 8) {
				return din.readLong();
			}
			long v = Bits.getLong(buf, pos);
			pos += 8;
			return v;
		}

		public double readDouble() throws IOException {
			if(!blkmode) {
				pos = 0;
				in.readFully(buf, 0, 8);
			} else if(end - pos < 8) {
				return din.readDouble();
			}
			double v = Bits.getDouble(buf, pos);
			pos += 8;
			return v;
		}

		public String readUTF() throws IOException {
			return readUTFBody(readUnsignedShort());
		}

		@SuppressWarnings("deprecation")
		public String readLine() throws IOException {
			return din.readLine(); // deprecated, not worth optimizing
		}

		void readBooleans(boolean[] v, int off, int len) throws IOException {
			int stop, endoff = off + len;
			while(off < endoff) {
				if(!blkmode) {
					int span = Math.min(endoff - off, MAX_BLOCK_SIZE);
					in.readFully(buf, 0, span);
					stop = off + span;
					pos = 0;
				} else if(end - pos < 1) {
					v[off++] = din.readBoolean();
					continue;
				} else {
					stop = Math.min(endoff, off + end - pos);
				}

				while(off < stop) {
					v[off++] = Bits.getBoolean(buf, pos++);
				}
			}
		}

		void readChars(char[] v, int off, int len) throws IOException {
			int stop, endoff = off + len;
			while(off < endoff) {
				if(!blkmode) {
					int span = Math.min(endoff - off, MAX_BLOCK_SIZE >> 1);
					in.readFully(buf, 0, span << 1);
					stop = off + span;
					pos = 0;
				} else if(end - pos < 2) {
					v[off++] = din.readChar();
					continue;
				} else {
					stop = Math.min(endoff, off + ((end - pos) >> 1));
				}

				while(off < stop) {
					v[off++] = Bits.getChar(buf, pos);
					pos += 2;
				}
			}
		}

		void readShorts(short[] v, int off, int len) throws IOException {
			int stop, endoff = off + len;
			while(off < endoff) {
				if(!blkmode) {
					int span = Math.min(endoff - off, MAX_BLOCK_SIZE >> 1);
					in.readFully(buf, 0, span << 1);
					stop = off + span;
					pos = 0;
				} else if(end - pos < 2) {
					v[off++] = din.readShort();
					continue;
				} else {
					stop = Math.min(endoff, off + ((end - pos) >> 1));
				}

				while(off < stop) {
					v[off++] = Bits.getShort(buf, pos);
					pos += 2;
				}
			}
		}

		void readInts(int[] v, int off, int len) throws IOException {
			int stop, endoff = off + len;
			while(off < endoff) {
				if(!blkmode) {
					int span = Math.min(endoff - off, MAX_BLOCK_SIZE >> 2);
					in.readFully(buf, 0, span << 2);
					stop = off + span;
					pos = 0;
				} else if(end - pos < 4) {
					v[off++] = din.readInt();
					continue;
				} else {
					stop = Math.min(endoff, off + ((end - pos) >> 2));
				}

				while(off < stop) {
					v[off++] = Bits.getInt(buf, pos);
					pos += 4;
				}
			}
		}

		void readFloats(float[] v, int off, int len) throws IOException {
			throw new RuntimeException("native bytesToFloats");
/*
			int span, endoff = off + len;
			while(off < endoff) {
				if(!blkmode) {
					span = Math.min(endoff - off, MAX_BLOCK_SIZE >> 2);
					in.readFully(buf, 0, span << 2);
					pos = 0;
				} else if(end - pos < 4) {
					v[off++] = din.readFloat();
					continue;
				} else {
					span = Math.min(endoff - off, ((end - pos) >> 2));
				}

				bytesToFloats(buf, pos, v, off, span);
				off += span;
				pos += span << 2;
			}
*/
		}

		void readLongs(long[] v, int off, int len) throws IOException {
			int stop, endoff = off + len;
			while(off < endoff) {
				if(!blkmode) {
					int span = Math.min(endoff - off, MAX_BLOCK_SIZE >> 3);
					in.readFully(buf, 0, span << 3);
					stop = off + span;
					pos = 0;
				} else if(end - pos < 8) {
					v[off++] = din.readLong();
					continue;
				} else {
					stop = Math.min(endoff, off + ((end - pos) >> 3));
				}

				while(off < stop) {
					v[off++] = Bits.getLong(buf, pos);
					pos += 8;
				}
			}
		}

		void readDoubles(double[] v, int off, int len) throws IOException {
			throw new RuntimeException("native bytesToDoubles");
/*
			int span, endoff = off + len;
			while(off < endoff) {
				if(!blkmode) {
					span = Math.min(endoff - off, MAX_BLOCK_SIZE >> 3);
					in.readFully(buf, 0, span << 3);
					pos = 0;
				} else if(end - pos < 8) {
					v[off++] = din.readDouble();
					continue;
				} else {
					span = Math.min(endoff - off, ((end - pos) >> 3));
				}

				bytesToDoubles(buf, pos, v, off, span);
				off += span;
				pos += span << 3;
			}
*/
		}

		String readLongUTF() throws IOException {
			return readUTFBody(readLong());
		}

		private String readUTFBody(long utflen) throws IOException {
			StringBuilder sbuf = new StringBuilder();
			if(!blkmode) {
				end = pos = 0;
			}

			while(utflen > 0) {
				int avail = end - pos;
				if(avail >= 3 || (long)avail == utflen) {
					utflen -= readUTFSpan(sbuf, utflen);
				} else {
					if(blkmode) {
						// near block boundary, read one byte at a time
						utflen -= readUTFChar(sbuf, utflen);
					} else {
						// shift and refill buffer manually
						if(avail > 0) {
							System.arraycopy(buf, pos, buf, 0, avail);
						}
						pos = 0;
						end = (int)Math.min(MAX_BLOCK_SIZE, utflen);
						in.readFully(buf, avail, end - avail);
					}
				}
			}

			return sbuf.toString();
		}

		private long readUTFSpan(StringBuilder sbuf, long utflen) throws IOException {
			int cpos = 0;
			int start = pos;
			int avail = Math.min(end - pos, CHAR_BUF_SIZE);
			// stop short of last char unless all of utf bytes in buffer
			int stop = pos + ((utflen > avail) ? avail - 2 : (int)utflen);
			boolean outOfBounds = false;

			try {
				while(pos < stop) {
					int b1, b2, b3;
					b1 = buf[pos++] & 0xFF;
					switch(b1 >> 4) {
					case 0:
					case 1:
					case 2:
					case 3:
					case 4:
					case 5:
					case 6:
					case 7: // 1 byte format: 0xxxxxxx
						cbuf[cpos++] = (char)b1;
						break;

					case 12:
					case 13: // 2 byte format: 110xxxxx 10xxxxxx
						b2 = buf[pos++];
						if((b2 & 0xC0) != 0x80) {
							throw new UTFDataFormatException();
						}
						cbuf[cpos++] = (char)(((b1 & 0x1F) << 6) | ((b2 & 0x3F) << 0));
						break;

					case 14: // 3 byte format: 1110xxxx 10xxxxxx 10xxxxxx
						b3 = buf[pos + 1];
						b2 = buf[pos + 0];
						pos += 2;
						if((b2 & 0xC0) != 0x80 || (b3 & 0xC0) != 0x80) {
							throw new UTFDataFormatException();
						}
						cbuf[cpos++] = (char)(((b1 & 0x0F) << 12) | ((b2 & 0x3F) << 6) | ((b3 & 0x3F) << 0));
						break;

					default: // 10xx xxxx, 1111 xxxx
						throw new UTFDataFormatException();
					}
				}
			} catch(ArrayIndexOutOfBoundsException ex) {
				outOfBounds = true;
			} finally {
				if(outOfBounds || (pos - start) > utflen) {
					pos = start + (int)utflen;
					throw new UTFDataFormatException();
				}
			}

			sbuf.append(cbuf, 0, cpos);
			return pos - start;
		}

		private int readUTFChar(StringBuilder sbuf, long utflen) throws IOException {
			int b1, b2, b3;
			b1 = readByte() & 0xFF;
			switch(b1 >> 4) {
			case 0:
			case 1:
			case 2:
			case 3:
			case 4:
			case 5:
			case 6:
			case 7: // 1 byte format: 0xxxxxxx
				sbuf.append((char)b1);
				return 1;

			case 12:
			case 13: // 2 byte format: 110xxxxx 10xxxxxx
				if(utflen < 2) {
					throw new UTFDataFormatException();
				}
				b2 = readByte();
				if((b2 & 0xC0) != 0x80) {
					throw new UTFDataFormatException();
				}
				sbuf.append((char)(((b1 & 0x1F) << 6) | ((b2 & 0x3F) << 0)));
				return 2;

			case 14: // 3 byte format: 1110xxxx 10xxxxxx 10xxxxxx
				if(utflen < 3) {
					if(utflen == 2) {
						readByte(); // consume remaining byte
					}
					throw new UTFDataFormatException();
				}
				b2 = readByte();
				b3 = readByte();
				if((b2 & 0xC0) != 0x80 || (b3 & 0xC0) != 0x80) {
					throw new UTFDataFormatException();
				}
				sbuf.append((char)(((b1 & 0x0F) << 12) | ((b2 & 0x3F) << 6) | ((b3 & 0x3F) << 0)));
				return 3;

			default: // 10xx xxxx, 1111 xxxx
				throw new UTFDataFormatException();
			}
		}
	}

	private static class HandleTable {
		private static final byte STATUS_OK = 1;
		private static final byte STATUS_UNKNOWN = 2;
		private static final byte STATUS_EXCEPTION = 3;

		byte[] status;
		Object[] entries;
		HandleList[] deps;
		int lowDep = -1;
		int size = 0;

		HandleTable(int initialCapacity) {
			status = new byte[initialCapacity];
			entries = new Object[initialCapacity];
			deps = new HandleList[initialCapacity];
		}

		int assign(Object obj) {
			if(size >= entries.length) {
				grow();
			}
			status[size] = STATUS_UNKNOWN;
			entries[size] = obj;
			return size++;
		}

		void markDependency(int dependent, int target) {
			if(dependent == NULL_HANDLE || target == NULL_HANDLE) {
				return;
			}
			switch(status[dependent]) {

			case STATUS_UNKNOWN:
				switch(status[target]) {
				case STATUS_OK:
					// ignore dependencies on objs with no exception
					break;

				case STATUS_EXCEPTION:
					// eagerly propagate exception
					markException(dependent, (ClassNotFoundException)entries[target]);
					break;

				case STATUS_UNKNOWN:
					// add to dependency list of target
					if(deps[target] == null) {
						deps[target] = new HandleList();
					}
					deps[target].add(dependent);

					// remember lowest unresolved target seen
					if(lowDep < 0 || lowDep > target) {
						lowDep = target;
					}
					break;

				default:
					throw new InternalError();
				}
				break;

			case STATUS_EXCEPTION:
				break;

			default:
				throw new InternalError();
			}
		}

		void markException(int handle, ClassNotFoundException ex) {
			switch(status[handle]) {
			case STATUS_UNKNOWN:
				status[handle] = STATUS_EXCEPTION;
				entries[handle] = ex;

				// propagate exception to dependents
				HandleList dlist = deps[handle];
				if(dlist != null) {
					int ndeps = dlist.size();
					for(int i = 0; i < ndeps; i++) {
						markException(dlist.get(i), ex);
					}
					deps[handle] = null;
				}
				break;

			case STATUS_EXCEPTION:
				break;

			default:
				throw new InternalError();
			}
		}

		void finish(int handle) {
			int end;
			if(lowDep < 0) {
				end = handle + 1;
			} else if(lowDep >= handle) {
				end = size;
				lowDep = -1;
			} else {
				return;
			}

			for(int i = handle; i < end; i++) {
				switch(status[i]) {
				case STATUS_UNKNOWN:
					status[i] = STATUS_OK;
					deps[i] = null;
					break;

				case STATUS_OK:
				case STATUS_EXCEPTION:
					break;

				default:
					throw new InternalError();
				}
			}
		}

		void setObject(int handle, Object obj) {
			switch(status[handle]) {
			case STATUS_UNKNOWN:
			case STATUS_OK:
				entries[handle] = obj;
				break;

			case STATUS_EXCEPTION:
				break;

			default:
				throw new InternalError();
			}
		}

		Object lookupObject(int handle) {
			return (handle != NULL_HANDLE && status[handle] != STATUS_EXCEPTION) ? entries[handle] : null;
		}

		ClassNotFoundException lookupException(int handle) {
			return (handle != NULL_HANDLE && status[handle] == STATUS_EXCEPTION) ? (ClassNotFoundException)entries[handle] : null;
		}

		void clear() {
			Arrays.fill(status, 0, size, (byte)0);
			Arrays.fill(entries, 0, size, null);
			Arrays.fill(deps, 0, size, null);
			lowDep = -1;
			size = 0;
		}

		int size() {
			return size;
		}

		private void grow() {
			int newCapacity = (entries.length << 1) + 1;

			byte[] newStatus = new byte[newCapacity];
			Object[] newEntries = new Object[newCapacity];
			HandleList[] newDeps = new HandleList[newCapacity];

			System.arraycopy(status, 0, newStatus, 0, size);
			System.arraycopy(entries, 0, newEntries, 0, size);
			System.arraycopy(deps, 0, newDeps, 0, size);

			status = newStatus;
			entries = newEntries;
			deps = newDeps;
		}

		private static class HandleList {
			private int[] list = new int[4];
			private int size = 0;

			public HandleList() {
			}

			public void add(int handle) {
				if(size >= list.length) {
					int[] newList = new int[list.length << 1];
					System.arraycopy(list, 0, newList, 0, list.length);
					list = newList;
				}
				list[size++] = handle;
			}

			public int get(int index) {
				if(index >= size) {
					throw new ArrayIndexOutOfBoundsException();
				}
				return list[index];
			}

			public int size() {
				return size;
			}
		}
	}

	private static Object cloneArray(Object array) {
		if(array instanceof Object[]) {
			return ((Object[])array).clone();
		} else if(array instanceof boolean[]) {
			return ((boolean[])array).clone();
		} else if(array instanceof byte[]) {
			return ((byte[])array).clone();
		} else if(array instanceof char[]) {
			return ((char[])array).clone();
		} else if(array instanceof double[]) {
			return ((double[])array).clone();
		} else if(array instanceof float[]) {
			return ((float[])array).clone();
		} else if(array instanceof int[]) {
			return ((int[])array).clone();
		} else if(array instanceof long[]) {
			return ((long[])array).clone();
		} else if(array instanceof short[]) {
			return ((short[])array).clone();
		} else {
			throw new AssertionError();
		}
	}

}
