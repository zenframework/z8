package org.zenframework.z8.server.engine;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.rmi.dgc.Lease;
import java.rmi.dgc.VMID;
import java.rmi.server.ObjID;
import java.rmi.server.RemoteObjectInvocationHandler;
import java.rmi.server.UID;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zenframework.z8.rmi.ObjectIO;
import org.zenframework.z8.server.runtime.CLASS;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.datespan;
import org.zenframework.z8.server.types.datetime;
import org.zenframework.z8.server.types.decimal;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.utils.ErrorUtils;
import org.zenframework.z8.server.utils.IOUtils;

import sun.rmi.server.UnicastRef;
import sun.rmi.transport.LiveRef;

@SuppressWarnings("restriction")
public class RmiIO extends ObjectIO {

	private static final Log LOG = LogFactory.getLog(RmiIO.class);

	static private Map<String, Constructor<?>> constructors = Collections
			.synchronizedMap(new HashMap<String, Constructor<?>>());
	static private Map<String, Class<?>> classes = Collections.synchronizedMap(new HashMap<String, Class<?>>());

	static private Constructor<?> getConstructor(String name, Class<?>[] parameters) {
		try {
			Constructor<?> constructor = constructors.get(name);

			if (constructor != null)
				return constructor;

			constructor = getClass(name).getConstructor(parameters);
			constructors.put(name, constructor);

			return constructor;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	static private Class<?> getClass(String name) {
		try {
			Class<?> cls = classes.get(name);

			if (cls != null)
				return cls;

			cls = Class.forName(name);
			classes.put(name, cls);
			return cls;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	static private Object newObject(String name, Class<?>[] arguments, Object[] parameters) {
		try {
			return getConstructor(name, arguments).newInstance(parameters);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	static public void writeBool(ObjectOutputStream out, bool value) throws IOException {
		out.writeBoolean(value.get());
	}

	static public void writeDate(ObjectOutputStream out, date value) throws IOException {
		out.writeLong(value.getTicks());
	}

	static public void writeDatespan(ObjectOutputStream out, datespan value) throws IOException {
		out.writeLong(value.get());
	}

	static public void writeDatetime(ObjectOutputStream out, datetime value) throws IOException {
		out.writeLong(value.getTicks());
	}

	static public void writeDecimal(ObjectOutputStream out, decimal value) throws IOException {
		BigDecimal decimal = value.get();
		writeBytes(out, decimal.unscaledValue().toByteArray());
		out.writeInt(decimal.scale());
	}

	static public void writeBytes(ObjectOutputStream out, byte[] value) throws IOException {
		out.writeInt(value != null ? value.length : -1);
		if (value != null)
			out.write(value);
	}

	static public void writeGuid(ObjectOutputStream out, guid value) throws IOException {
		writeUUID(out, value.get());
	}

	static public void writeUUID(ObjectOutputStream out, UUID value) throws IOException {
		out.writeLong(value.getLeastSignificantBits());
		out.writeLong(value.getMostSignificantBits());
	}

	static public void writeInteger(ObjectOutputStream out, integer value) throws IOException {
		out.writeLong(value.get());
	}

	static public void writeString(ObjectOutputStream out, string value) throws IOException {
		writeString(out, value.get());
	}

	static public void writeString(ObjectOutputStream out, String value) throws IOException {
		writeBytes(out, value != null ? value.getBytes(IOUtils.DefaultCharset) : null);
	}

	static public void writePrimary(ObjectOutputStream out, primary value) throws IOException {
		if (value instanceof bool) {
			out.writeInt(primary.Bool);
			writeBool(out, (bool) value);
		} else if (value instanceof date) {
			out.writeInt(primary.Date);
			writeDate(out, (date) value);
		} else if (value instanceof datespan) {
			out.writeInt(primary.Datespan);
			writeDatespan(out, (datespan) value);
		} else if (value instanceof datetime) {
			out.writeInt(primary.Datetime);
			writeDatetime(out, (datetime) value);
		} else if (value instanceof decimal) {
			out.writeInt(primary.Decimal);
			writeDecimal(out, (decimal) value);
		} else if (value instanceof guid) {
			out.writeInt(primary.Guid);
			writeGuid(out, (guid) value);
		} else if (value instanceof integer) {
			out.writeInt(primary.Integer);
			writeInteger(out, (integer) value);
		} else if (value instanceof string) {
			out.writeInt(primary.String);
			writeString(out, (string) value);
		} else
			throw new RuntimeException("Unknown primary type");
	}

	@Override
	protected void writeObject(ObjectOutputStream out, Object replacement, Object value) throws IOException {
		if (value instanceof String) {
			out.writeInt(RmiSerializable.String);
			writeString(out, (String) value);
		} else if (value instanceof primary) {
			out.writeInt(RmiSerializable.Primary);
			writePrimary(out, (primary) value);
		} else if (value instanceof Object[]) {
			out.writeInt(RmiSerializable.Array);
			writeArray(out, (Object[]) value);
		} else if (value instanceof Collection) {
			out.writeInt(RmiSerializable.Collection);
			writeCollection(out, (Collection<?>) value);
		} else if (value instanceof Map) {
			out.writeInt(RmiSerializable.Map);
			writeMap(out, (Map<?, ?>) value);
		} else if (replacement instanceof Proxy) {
			out.writeInt(RmiSerializable.Proxy);
			writeProxy(out, (Proxy) replacement, value);
		} else if (value instanceof Throwable) {
			out.writeInt(RmiSerializable.Exception);
			writeException(out, (Throwable) value);
		} else if (value instanceof ObjID) {
			out.writeInt(RmiSerializable.ObjID);
			writeObjID(out, (ObjID) value);
		} else if (value instanceof VMID) {
			out.writeInt(RmiSerializable.VMID);
			writeVMID(out, (VMID) value);
		} else if (value instanceof Lease) {
			out.writeInt(RmiSerializable.Lease);
			writeLease(out, (Lease) value);
		} else if (value instanceof OBJECT) {
			out.writeInt(RmiSerializable.OBJECT);
			writeOBJECT(out, (OBJECT) value);
		} else if (value instanceof RmiSerializable) {
			out.writeInt(RmiSerializable.Self);
			writeSerializable(out, (RmiSerializable) value);
		} else {
			RuntimeException e = new RuntimeException("Object (" + value.getClass().getCanonicalName()
					+ ") is not an instance of z8.rmi.ISerializable");
			LOG.error(e.getMessage(), e);
			throw e;
		}
	}

	private void writeSerializable(ObjectOutputStream out, RmiSerializable serializable) throws IOException {
		writeClass(out, serializable);
		serializable.serialize(out);
	}

	private void writeOBJECT(ObjectOutputStream out, OBJECT object) throws IOException {
		writeClass(out, object);
		object.serialize(out);
	}

	private void writeProxy(ObjectOutputStream out, Proxy proxy, Object object) throws IOException {
		RemoteObjectInvocationHandler handler = (RemoteObjectInvocationHandler) Proxy.getInvocationHandler(proxy);

		UnicastRef unicastRef = (UnicastRef) handler.getRef();
		LiveRef liveRef = unicastRef.getLiveRef();

		writeClass(out, object);
		liveRef.write(out, true);
	}

	private void writeArray(ObjectOutputStream out, Object[] array) throws IOException {
		out.writeInt(array.length);
		writeClass(out, array);
		for (Object object : array)
			writeObject(out, object, object);
	}

	private void writeCollection(ObjectOutputStream out, Collection<?> collection) throws IOException {
		out.writeInt(collection != null ? collection.size() : -1);

		if (collection == null)
			return;

		writeClass(out, collection);
		for (Object object : collection)
			writeObject(out, object, object);
	}

	private void writeMap(ObjectOutputStream out, Map<?, ?> map) throws IOException {
		out.writeInt(map != null ? map.size() : -1);

		if (map == null)
			return;

		writeClass(out, map);
		for (Object key : map.keySet()) {
			out.writeObject(key);
			out.writeObject(map.get(key));
		}
	}

	private void writeObjID(ObjectOutputStream out, ObjID object) throws IOException {
		object.write(out);
	}

	private void writeVMID(ObjectOutputStream out, VMID vmid) throws IOException {
		vmid.uid.write(out);
		writeBytes(out, vmid.addr);
	}

	private void writeLease(ObjectOutputStream out, Lease lease) throws IOException {
		writeVMID(out, lease.getVMID());
		out.writeLong(lease.getValue());
	}

	private void writeClass(ObjectOutputStream out, Object object) throws IOException {
		Class<?> cls = object.getClass();
		if (object instanceof Object[])
			cls = cls.getComponentType();
		writeString(out, cls.getCanonicalName());
	}

	private void writeException(ObjectOutputStream out, Throwable object) throws IOException {
		writeClass(out, object);
		writeString(out, ErrorUtils.getMessage(object));
	}

	static public bool readBool(ObjectInputStream in) throws IOException {
		return new bool(in.readBoolean());
	}

	static public date readDate(ObjectInputStream in) throws IOException {
		return new date(in.readLong());
	}

	static public datespan readDatespan(ObjectInputStream in) throws IOException {
		return new datespan(in.readLong());
	}

	static public datetime readDatetime(ObjectInputStream in) throws IOException {
		return new datetime(in.readLong());
	}

	static public decimal readDecimal(ObjectInputStream in) throws IOException {
		BigInteger unscaled = new BigInteger(readBytes(in));
		int scale = in.readInt();
		return new decimal(new BigDecimal(unscaled, scale));
	}

	static public guid readGuid(ObjectInputStream in) throws IOException {
		return new guid(readUUID(in));
	}

	static public UUID readUUID(ObjectInputStream in) throws IOException {
		long leastSignificantBits = in.readLong();
		long mostSignificantBits = in.readLong();
		return new UUID(mostSignificantBits, leastSignificantBits);
	}

	static public byte[] readBytes(ObjectInputStream in) throws IOException {
		int length = in.readInt();
		if (length == -1)
			return null;

		byte[] bytes = new byte[length];
		in.readFully(bytes);
		return bytes;
	}

	static public integer readInteger(ObjectInputStream in) throws IOException {
		return new integer(in.readLong());
	}

	static public String readString(ObjectInputStream in) throws IOException {
		byte[] bytes = readBytes(in);

		if (bytes == null)
			return null;

		return new String(bytes, IOUtils.DefaultCharset);
	}

	static public primary readPrimary(ObjectInputStream in) throws IOException {
		int type = in.readInt();

		if (type == primary.Bool)
			return readBool(in);
		else if (type == primary.Date)
			return readDate(in);
		else if (type == primary.Datespan)
			return readDatespan(in);
		else if (type == primary.Datetime)
			return readDatetime(in);
		else if (type == primary.Decimal)
			return readDecimal(in);
		else if (type == primary.Guid)
			return readGuid(in);
		else if (type == primary.Integer)
			return readInteger(in);
		else if (type == primary.String)
			return new string(readString(in));
		else
			throw new RuntimeException("Unknown primary type");
	}

	@Override
	protected Object readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		int id = in.readInt();

		if (id == RmiSerializable.String)
			return readString(in);
		else if (id == RmiSerializable.Primary)
			return readPrimary(in);
		else if (id == RmiSerializable.Array)
			return readArray(in);
		else if (id == RmiSerializable.Collection)
			return readCollection(in);
		else if (id == RmiSerializable.Map)
			return readMap(in);
		else if (id == RmiSerializable.Proxy)
			return readProxy(in);
		else if (id == RmiSerializable.Exception)
			return readException(in);
		else if (id == RmiSerializable.ObjID)
			return readObjID(in);
		else if (id == RmiSerializable.VMID)
			return readVMID(in);
		else if (id == RmiSerializable.Lease)
			return readLease(in);
		else if (id == RmiSerializable.OBJECT)
			return readOBJECT(in);
		else if (id == RmiSerializable.Self)
			return readSerializable(in);

		throw new RuntimeException("Unknown object type");
	}

	private Object readArray(ObjectInputStream in) throws IOException, ClassNotFoundException {
		int length = in.readInt();
		String cls = readClass(in);

		Object[] array = (Object[]) Array.newInstance(getClass(cls), length);

		for (int i = 0; i < length; i++)
			array[i] = readObject(in);

		return array;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Object readCollection(ObjectInputStream in) throws IOException, ClassNotFoundException {
		int length = in.readInt();

		if (length == -1)
			return null;

		String cls = readClass(in);

		Collection collection = (Collection<?>) newObject(cls, null, null);

		for (int i = 0; i < length; i++)
			collection.add(readObject(in));

		return collection;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Object readMap(ObjectInputStream in) throws IOException, ClassNotFoundException {
		int count = in.readInt();

		if (count == -1)
			return null;

		String cls = readClass(in);

		Map map = (Map<?, ?>) newObject(cls, null, null);

		for (int i = 0; i < count; i++) {
			Object key = in.readObject();
			Object value = in.readObject();
			map.put(key, value);
		}

		return map;
	}

	private Object readSerializable(ObjectInputStream in) throws IOException, ClassNotFoundException {
		String cls = readClass(in);
		RmiSerializable serializable = (RmiSerializable) newObject(cls, null, null);
		serializable.deserialize(in);
		return serializable;
	}

	private Object readOBJECT(ObjectInputStream in) throws IOException, ClassNotFoundException {
		CLASS<?> cls = (CLASS<?>) newObject(readClass(in) + "$CLASS", new Class<?>[] { IObject.class },
				new Object[] { null });
		RmiSerializable serializable = (RmiSerializable) cls.newObject(null);
		serializable.deserialize(in);
		return serializable;
	}

	private String readClass(ObjectInputStream in) throws IOException, ClassNotFoundException {
		return readString(in);
	}

	private Object readProxy(ObjectInputStream in) throws IOException, ClassNotFoundException {
		String cls = readClass(in);
		LiveRef liveRef = LiveRef.read(in, true);

		RemoteObjectInvocationHandler handler = new RemoteObjectInvocationHandler(new UnicastRef(liveRef));
		return Proxy.newProxyInstance(this.getClass().getClassLoader(), getClass(cls).getInterfaces(), handler);
	}

	private ObjID readObjID(ObjectInputStream in) throws IOException, ClassNotFoundException {
		return ObjID.read(in);
	}

	private VMID readVMID(ObjectInputStream in) throws IOException, ClassNotFoundException {
		VMID vmid = new VMID();
		vmid.uid = UID.read(in);
		vmid.addr = readBytes(in);
		return vmid;
	}

	private Lease readLease(ObjectInputStream in) throws IOException, ClassNotFoundException {
		VMID vmid = readVMID(in);
		long duration = in.readLong();
		return new Lease(vmid, duration);
	}

	protected Object readException(ObjectInputStream in) throws IOException, ClassNotFoundException {
		String cls = readClass(in);
		String message = readString(in);
		return newObject(cls, new Class<?>[] { String.class }, new Object[] { message });
	}
}
