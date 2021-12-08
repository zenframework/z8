package org.zenframework.z8.server.security;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.zenframework.z8.server.engine.RmiIO;
import org.zenframework.z8.server.engine.RmiSerializable;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.guid;

public class Access implements RmiSerializable, Serializable {
	private static final long serialVersionUID = -4405245083252310301L;

	static public guid Read = new guid("1F204E48-C7CE-426B-B299-6B17FBD8407E");
	static public guid Write = new guid("D6DD09D6-AA75-46B4-8C98-7D6FAEB8F120");
	static public guid Create = new guid("E3341343-D7AA-4FB0-9A1E-E311F26D7787");
	static public guid Copy = new guid("1FCDFE2C-9476-44BE-BA46-706F2F6F6CCF");
	static public guid Destroy = new guid("A6EC4C5A-8DD9-496C-8D14-D01BE98C9411");
	static public guid Execute = new guid("562256F6-B897-4377-89D4-7E92982483F9");

	private Map<guid, bool> accessMap = new HashMap<guid, bool>();

	static public Access administrator() {
		Access access = new Access();
		access.set(Read, true);
		access.set(Write, true);
		access.set(Create, true);
		access.set(Copy, true);
		access.set(Destroy, true);

		access.set(Execute, true);
		return access;
	}

	static public Access user() {
		Access access = new Access();
		access.set(Read, true);
		return access;
	}

	static public Access guest() {
		return new Access();
	}

	public Access() {
	}

	public boolean get(guid id) {
		bool value = accessMap.get(id);
		return value != null ? value.get() : false;
	}

	public void set(guid id, boolean value) {
		if(value)
			accessMap.put(id, new bool(value));
		else
			accessMap.remove(id);
	}

	public boolean getRead() {
		return get(Read);
	}

	public void setRead(boolean read) {
		set(Read, read);
	}

	public boolean getWrite() {
		return get(Write);
	}

	public void setWrite(boolean write) {
		set(Write, write);
	}

	public boolean getCreate() {
		return get(Create);
	}

	public void setCreate(boolean create) {
		set(Create, create);
	}

	public boolean getCopy() {
		return get(Copy);
	}

	public void setCopy(boolean copy) {
		set(Copy, copy);
	}

	public boolean getDestroy() {
		return get(Destroy);
	}

	public void setDestroy(boolean destroy) {
		set(Destroy, destroy);
	}

	public boolean getExecute() {
		return get(Execute);
	}

	public void setExecute(boolean execute) {
		set(Execute, execute);
	}

	public Access or(Access access) {
		Access result = new Access();
		result.accessMap.putAll(accessMap);

		for(guid key : access.getKeys()) {
			if(!result.get(key) && access.get(key))
				result.set(key, true);
		}

		return result;
	}

	public Access and(Access access) {
		Access result = new Access();

		for(guid key : getKeys()) {
			if(get(key) && access.get(key))
				result.set(key, true);
		}

		return result;
	}

	public Set<guid> getKeys() {
		return accessMap.keySet();
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		serialize(out);
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		deserialize(in);
	}

	@Override
	public void serialize(ObjectOutputStream out) throws IOException {
		RmiIO.writeLong(out, serialVersionUID);

		out.writeObject(accessMap);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void deserialize(ObjectInputStream in) throws IOException, ClassNotFoundException {
		@SuppressWarnings("unused")
		long version = RmiIO.readLong(in);

		accessMap = (Map<guid, bool>)in.readObject();
	}
}
