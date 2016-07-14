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
import java.rmi.server.RemoteRef;
import java.rmi.server.RemoteStub;
import java.rmi.server.UID;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.zenframework.z8.rmi.ObjectIO;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.runtime.CLASS;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.datespan;
import org.zenframework.z8.server.types.datetime;
import org.zenframework.z8.server.types.decimal;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.utils.IOUtils;
import org.zenframework.z8.server.utils.ProxyUtils;

import sun.rmi.server.UnicastRef;
import sun.rmi.transport.LiveRef;

@SuppressWarnings({ "restriction", "deprecation" })
public class RmiIO extends ObjectIO {
	static private Map<String, Constructor<?>> constructors = Collections.synchronizedMap(new HashMap<String, Constructor<?>>());
	static private Map<String, Class<?>> classes = Collections.synchronizedMap(new HashMap<String, Class<?>>());

	static private Constructor<?> getConstructor(String name, Class<?>[] parameters) {
		try {
			Constructor<?> constructor = constructors.get(name);

			if(constructor != null)
				return constructor;

			constructor = getClass(name).getConstructor(parameters);
			constructors.put(name, constructor);

			return constructor;
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}

	static private Class<?> getClass(String name) {
		try {
			Class<?> cls = classes.get(name);

			if(cls != null)
				return cls;

			cls = Class.forName(name);
			classes.put(name, cls);
			return cls;
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}

	static private Object newObject(String name, Class<?>[] arguments, Object[] parameters) {
		try {
			return getConstructor(name, arguments).newInstance(parameters);
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}

	static public void writeBool(ObjectOutputStream out, bool value) throws IOException {
		writeBoolean(out, value.get());
	}

	static public void writeDate(ObjectOutputStream out, date value) throws IOException {
		writeLong(out, value.getTicks());
	}

	static public void writeDatespan(ObjectOutputStream out, datespan value) throws IOException {
		writeLong(out, value.get());
	}

	static public void writeDatetime(ObjectOutputStream out, datetime value) throws IOException {
		writeLong(out, value.getTicks());
	}

	static public void writeDecimal(ObjectOutputStream out, decimal value) throws IOException {
		BigDecimal decimal = value.get();
		writeBytes(out, decimal.unscaledValue().toByteArray());
		writeInt(out, decimal.scale());
	}

	static public void writeBytes(ObjectOutputStream out, byte[] value) throws IOException {
		writeInt(out, value != null ? value.length : -1);
		if(value != null)
			out.write(value);
	}

	static public void writeGuid(ObjectOutputStream out, guid value) throws IOException {
		writeUUID(out, value.get());
	}

	static public void writeUUID(ObjectOutputStream out, UUID value) throws IOException {
		writeLong(out, value.getLeastSignificantBits());
		writeLong(out, value.getMostSignificantBits());
	}

	static public void writeInteger(ObjectOutputStream out, integer value) throws IOException {
		writeLong(out, value.get());
	}

	static public void writeString(ObjectOutputStream out, string value) throws IOException {
		writeString(out, value.get());
	}

	static public void writeFile(ObjectOutputStream out, file value) throws IOException {
		writeSerializable(out, (RmiSerializable)value);
	}

	static public void writeString(ObjectOutputStream out, String value) throws IOException {
		writeBytes(out, value != null ? value.getBytes(IOUtils.DefaultCharset) : null);
	}

	static public void writeBoolean(ObjectOutputStream out, boolean value) throws IOException {
		out.writeBoolean(value);
	}

	static public void writeByte(ObjectOutputStream out, byte value) throws IOException {
		out.write(value);
	}

	static public void writeChar(ObjectOutputStream out, char value) throws IOException {
		out.writeChar(value);
	}

	static public void writeShort(ObjectOutputStream out, short value) throws IOException {
		out.writeShort(value);
	}

	static public void writeInt(ObjectOutputStream out, int value) throws IOException {
		out.writeInt(value);
	}
	
	static public void writeLong(ObjectOutputStream out, long value) throws IOException {
		out.writeLong(value);
	}

	static public void writeFloat(ObjectOutputStream out, float value) throws IOException {
		out.writeFloat(value);
	}

	static public void writeDouble(ObjectOutputStream out, double value) throws IOException {
		out.writeDouble(value);
	}

	static private void writeSerializable(ObjectOutputStream out, RmiSerializable serializable) throws IOException {
		writeString(out, serializable.getClass().getCanonicalName());
		serializable.serialize(out);
	}

	static public void writePrimary(ObjectOutputStream out, primary value) throws IOException {
		if(value instanceof bool) {
			writeByte(out, RmiIOType.Boolean);
			writeBool(out, (bool)value);
		} else if(value instanceof date) {
			writeByte(out, RmiIOType.Date);
			writeDate(out, (date)value);
		} else if(value instanceof datespan) {
			writeByte(out, RmiIOType.Datespan);
			writeDatespan(out, (datespan)value);
		} else if(value instanceof datetime) {
			writeByte(out, RmiIOType.Datetime);
			writeDatetime(out, (datetime)value);
		} else if(value instanceof decimal) {
			writeByte(out, RmiIOType.Decimal);
			writeDecimal(out, (decimal)value);
		} else if(value instanceof guid) {
			writeByte(out, RmiIOType.Guid);
			writeGuid(out, (guid)value);
		} else if(value instanceof integer) {
			writeByte(out, RmiIOType.Integer);
			writeInteger(out, (integer)value);
		} else if(value instanceof string) {
			writeByte(out, RmiIOType.String);
			writeString(out, (string)value);
		} else if(value instanceof file) {
			writeByte(out, RmiIOType.File);
			writeFile(out, (file)value);
		} else
			throw new RuntimeException("Unknown primary type");
	}
	
	@Override
	protected void writeObject(ObjectOutputStream out, Object object) throws IOException {
		if(object instanceof RmiServer)
			object = ((RmiServer)object).proxy();
			
		if(object == null) {
			writeByte(out, RmiIOType.Null);
		
		// primitives
		} else if(object instanceof Boolean) {
			writeByte(out, RmiIOType.Boolean);
			writeBoolean(out, (Boolean)object);
		} else if(object instanceof Byte) {
			writeByte(out, RmiIOType.Byte);
			writeByte(out, (Byte)object);
		} else if(object instanceof Character) {
			writeByte(out, RmiIOType.Char);
			writeChar(out, (Character)object);
		} else if(object instanceof Short) {
			writeByte(out, RmiIOType.Short);
			writeShort(out, (Short)object);
		} else if(object instanceof Integer) {
			writeByte(out, RmiIOType.Integer);
			writeInt(out, (Integer)object);
		} else if(object instanceof Long) {
			writeByte(out, RmiIOType.Long);
			writeLong(out, (Long)object);
		} else if(object instanceof Float) {
			writeByte(out, RmiIOType.Float);
			writeFloat(out, (Float)object);
		} else if(object instanceof Double) {
			writeByte(out, RmiIOType.Double);
			writeDouble(out, (Double)object);
		} else if(object instanceof String) {
			writeByte(out, RmiIOType.String);
			writeString(out, (String)object);

		// primary
		} else if(object instanceof primary) {
			writeByte(out, RmiIOType.Primary);
			writePrimary(out, (primary)object);

		// arrays, collections, maps
		} else if(object instanceof Object[]) {
			writeByte(out, RmiIOType.Array);
			writeArray(out, (Object[])object);
		} else if(object instanceof Collection) {
			writeByte(out, RmiIOType.Collection);
			writeCollection(out, (Collection<?>)object);
		} else if(object instanceof Map) {
			writeByte(out, RmiIOType.Map);
			writeMap(out, (Map<?, ?>)object);
		
		// RMI internals
		} else if(object instanceof Proxy) {
			writeByte(out, RmiIOType.Proxy);
			writeProxy(out, (Proxy)object);
		} else if(object instanceof RemoteStub) {
			writeByte(out, RmiIOType.RemoteStub);
			writeRemoteStub(out, (RemoteStub)object);
		} else if(object instanceof Throwable) {
			writeByte(out, RmiIOType.Exception);
			writeException(out, (Throwable)object);
		} else if(object instanceof ObjID) {
			writeByte(out, RmiIOType.ObjID);
			writeObjID(out, (ObjID)object);
		} else if(object instanceof VMID) {
			writeByte(out, RmiIOType.VMID);
			writeVMID(out, (VMID)object);
		} else if(object instanceof Lease) {
			writeByte(out, RmiIOType.Lease);
			writeLease(out, (Lease)object);

		// OBJECT, RmiSerializable
		} else if(object instanceof OBJECT) {
			writeByte(out, RmiIOType.OBJECT);
			writeOBJECT(out, (OBJECT)object);
		} else if(object instanceof RmiSerializable) {
			writeByte(out, RmiIOType.Self);
			writeSerializable(out, (RmiSerializable)object);
		} else {
			RuntimeException e = new RuntimeException("Object (" + object.getClass().getCanonicalName() + ") is not an instance of z8.rmi.RmiSerializable");
			Trace.logError(e);
			throw e;
		}
	}

	private void writeOBJECT(ObjectOutputStream out, OBJECT object) throws IOException {
		writeString(out, object.getClass().getCanonicalName());
		object.serialize(out);
	}

	private void writeProxy(ObjectOutputStream out, Proxy proxy) throws IOException {

		Class<?>[] interfaces = proxy.getClass().getInterfaces();

		writeInt(out, interfaces.length);

		for(Class<?> cls : interfaces)
			writeString(out, cls.getCanonicalName());

		LiveRef liveRef = ProxyUtils.getLiveRef(proxy);
		liveRef.write(out, true);
	}

	private void writeRemoteStub(ObjectOutputStream out, RemoteStub stub) throws IOException {
		UnicastRef unicastRef = (UnicastRef)stub.getRef();
		LiveRef liveRef = unicastRef.getLiveRef();

		writeString(out, stub.getClass().getCanonicalName());
		liveRef.write(out, true);
	}

	private void writeArray(ObjectOutputStream out, Object[] array) throws IOException {
		writeInt(out, array.length);

		writeString(out, array.getClass().getComponentType().getCanonicalName());
		
		for(Object object : array)
			writeObject(out, object);
	}

	private void writeCollection(ObjectOutputStream out, Collection<?> collection) throws IOException {
		writeInt(out, collection != null ? collection.size() : -1);

		if(collection == null)
			return;

		writeString(out, collection.getClass().getCanonicalName());

		for(Object object : collection)
			writeObject(out, object);
	}

	private void writeMap(ObjectOutputStream out, Map<?, ?> map) throws IOException {
		writeInt(out, map != null ? map.size() : -1);

		if(map == null)
			return;

		writeString(out, map.getClass().getCanonicalName());

		for(Object key : map.keySet()) {
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
		writeLong(out, lease.getValue());
	}

	private void writeException(ObjectOutputStream out, Throwable object) throws IOException {
		writeString(out, object.getClass().getCanonicalName());
		writeString(out, object.getMessage());
	}

	static public bool readBool(ObjectInputStream in) throws IOException {
		return new bool(readBoolean(in));
	}

	static public date readDate(ObjectInputStream in) throws IOException {
		return new date(readLong(in));
	}

	static public datespan readDatespan(ObjectInputStream in) throws IOException {
		return new datespan(readLong(in));
	}

	static public datetime readDatetime(ObjectInputStream in) throws IOException {
		return new datetime(readLong(in));
	}

	static public decimal readDecimal(ObjectInputStream in) throws IOException {
		BigInteger unscaled = new BigInteger(readBytes(in));
		int scale = readInt(in);
		return new decimal(new BigDecimal(unscaled, scale));
	}

	static public guid readGuid(ObjectInputStream in) throws IOException {
		return new guid(readUUID(in));
	}

	static public file readFile(ObjectInputStream in) throws IOException, ClassNotFoundException {
		return (file)readSerializable(in);
	}

	static public UUID readUUID(ObjectInputStream in) throws IOException {
		long leastSignificantBits = readLong(in);
		long mostSignificantBits = readLong(in);
		return new UUID(mostSignificantBits, leastSignificantBits);
	}

	static public byte[] readBytes(ObjectInputStream in) throws IOException {
		int length = readInt(in);
		if(length == -1)
			return null;

		byte[] bytes = new byte[length];
		in.readFully(bytes);
		return bytes;
	}

	static public integer readInteger(ObjectInputStream in) throws IOException {
		return new integer(readLong(in));
	}

	static public boolean readBoolean(ObjectInputStream in) throws IOException {
		return in.readBoolean();
	}

	static public byte readByte(ObjectInputStream in) throws IOException {
		return in.readByte();
	}

	static public char readChar(ObjectInputStream in) throws IOException {
		return in.readChar();
	}

	static public short readShort(ObjectInputStream in) throws IOException {
		return in.readShort();
	}

	static public int readInt(ObjectInputStream in) throws IOException {
		return in.readInt();
	}

	static public long readLong(ObjectInputStream in) throws IOException {
		return in.readLong();
	}

	static public float readFloat(ObjectInputStream in) throws IOException {
		return in.readFloat();
	}

	static public double readDouble(ObjectInputStream in) throws IOException {
		return in.readDouble();
	}

	static public String readString(ObjectInputStream in) throws IOException {
		byte[] bytes = readBytes(in);

		if(bytes == null)
			return null;

		return new String(bytes, IOUtils.DefaultCharset);
	}

	static public Object readSerializable(ObjectInputStream in) throws IOException, ClassNotFoundException {
		String cls = readString(in);
		RmiSerializable serializable = (RmiSerializable)newObject(cls, null, null);
		serializable.deserialize(in);
		return serializable;
	}

	static public primary readPrimary(ObjectInputStream in) throws IOException, ClassNotFoundException {
		byte type = readByte(in);

		if(type == RmiIOType.Boolean)
			return readBool(in);
		else if(type == RmiIOType.Date)
			return readDate(in);
		else if(type == RmiIOType.Datespan)
			return readDatespan(in);
		else if(type == RmiIOType.Datetime)
			return readDatetime(in);
		else if(type == RmiIOType.Decimal)
			return readDecimal(in);
		else if(type == RmiIOType.Guid)
			return readGuid(in);
		else if(type == RmiIOType.File)
			return readFile(in);
		else if(type == RmiIOType.Integer)
			return readInteger(in);
		else if(type == RmiIOType.String)
			return new string(readString(in));
		else
			throw new RuntimeException("Unknown primary type");
	}

	@Override
	protected Object readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		byte id = readByte(in);

		if(id == RmiIOType.Null)
			return null;

		// primitives
		else if(id == RmiIOType.Boolean)
			return readBoolean(in);
		else if(id == RmiIOType.Byte)
			return readByte(in);
		else if(id == RmiIOType.Char)
			return readChar(in);
		else if(id == RmiIOType.Short)
			return readShort(in);
		else if(id == RmiIOType.Integer)
			return readInt(in);
		else if(id == RmiIOType.Long)
			return readLong(in);
		else if(id == RmiIOType.Float)
			return readFloat(in);
		else if(id == RmiIOType.Double)
			return readDouble(in);
		else if(id == RmiIOType.String)
			return readString(in);

		// primary
		else if(id == RmiIOType.Primary)
			return readPrimary(in);
		
		// Arrays, collections, maps
		else if(id == RmiIOType.Array)
			return readArray(in);
		else if(id == RmiIOType.Collection)
			return readCollection(in);
		else if(id == RmiIOType.Map)
			return readMap(in);

		// RMI internals
		else if(id == RmiIOType.Proxy)
			return readProxy(in);
		else if(id == RmiIOType.RemoteStub)
			return readRemoteStub(in);
		else if(id == RmiIOType.Exception)
			return readException(in);
		else if(id == RmiIOType.ObjID)
			return readObjID(in);
		else if(id == RmiIOType.VMID)
			return readVMID(in);
		else if(id == RmiIOType.Lease)
			return readLease(in);

		// OBJECT, RmiSerializable
		else if(id == RmiIOType.OBJECT)
			return readOBJECT(in);
		else if(id == RmiIOType.Self)
			return readSerializable(in);

		throw new RuntimeException("Unknown object type");
	}

	private Object readArray(ObjectInputStream in) throws IOException, ClassNotFoundException {
		int length = readInt(in);
		String cls = readString(in);

		Object[] array = (Object[])Array.newInstance(getClass(cls), length);

		for(int i = 0; i < length; i++)
			array[i] = readObject(in);

		return array;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Object readCollection(ObjectInputStream in) throws IOException, ClassNotFoundException {
		int length = readInt(in);

		if(length == -1)
			return null;

		String cls = readString(in);

		Collection collection = (Collection<?>)newObject(cls, null, null);

		for(int i = 0; i < length; i++)
			collection.add(readObject(in));

		return collection;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Object readMap(ObjectInputStream in) throws IOException, ClassNotFoundException {
		int count = readInt(in);

		if(count == -1)
			return null;

		String cls = readString(in);

		Map map = (Map<?, ?>)newObject(cls, null, null);

		for(int i = 0; i < count; i++) {
			Object key = in.readObject();
			Object value = in.readObject();
			map.put(key, value);
		}

		return map;
	}

	private Object readOBJECT(ObjectInputStream in) throws IOException, ClassNotFoundException {
		CLASS<?> cls = (CLASS<?>)newObject(readString(in) + "$CLASS", new Class<?>[] { IObject.class }, new Object[] { null });
		RmiSerializable serializable = (RmiSerializable)cls.newObject(null);
		serializable.deserialize(in);
		return serializable;
	}

	private Object readProxy(ObjectInputStream in) throws IOException, ClassNotFoundException {
		int count = readInt(in);
		
		Class<?>[] interfaces = new Class<?>[count];
		
		for(int i = 0; i < count; i++)
			interfaces[i] = getClass(readString(in));
		
		LiveRef liveRef = LiveRef.read(in, true);
		return ProxyUtils.newProxy(liveRef, interfaces);
	}

	private Object readRemoteStub(ObjectInputStream in) throws IOException, ClassNotFoundException {
		String cls = readString(in);
		LiveRef liveRef = LiveRef.read(in, true);
		return newObject(cls, new Class<?>[] { RemoteRef.class }, new Object[] { new UnicastRef(liveRef) });
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
		long duration = readLong(in);
		return new Lease(vmid, duration);
	}

	private Object readException(ObjectInputStream in) throws IOException, ClassNotFoundException {
		String cls = readString(in);
		String message = readString(in);

		try {
			return newObject(cls, new Class<?>[] { String.class }, new Object[] { message });
		} catch(Throwable e) {
		}
		
		try {
			return newObject(cls, new Class<?>[] { String.class, Error.class }, new Object[] { message, null });
		} catch(Throwable e) {
		}
		
		return newObject(cls, new Class<?>[] { String.class, Throwable.class }, new Object[] { message, null });
	}
}
