package org.zenframework.z8.server.security;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.engine.RmiIO;
import org.zenframework.z8.server.types.guid;

public class Privileges implements IPrivileges {
	private static final long serialVersionUID = 8845983028465477335L;

	IAccess defaultAccess;

	Map<guid, IAccess> tableAccess;
	Map<guid, IAccess> fieldAccess;

	public Privileges() {
	}

	public Privileges(IAccess defaultAccess) {
		setDefaultAccess(defaultAccess);
	}

	public void setDefaultAccess(IAccess defaultAccess) {
		this.defaultAccess = defaultAccess;
	}

	public IAccess getAccess(Query table) {
		IAccess access = tableAccess.get(table.key());
		return access != null ? access : defaultAccess;
	}

	public void setTableAccess(guid table, IAccess access) {
		if(tableAccess == null)
			tableAccess = new HashMap<guid, IAccess>();
		tableAccess.put(table, access);
	}

	public IAccess getAccess(Field field) {
		IAccess access = fieldAccess.get(field.key());
		if(access == null)
			access = getAccess(field.owner());
		return access != null ? access : defaultAccess;
	}

	public void setFieldAccess(guid field, IAccess access) {
		if(fieldAccess == null)
			fieldAccess = new HashMap<guid, IAccess>();
		fieldAccess.put(field, access);
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

		out.writeObject(defaultAccess);
		out.writeObject(tableAccess);
		out.writeObject(fieldAccess);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void deserialize(ObjectInputStream in) throws IOException, ClassNotFoundException {
		@SuppressWarnings("unused")
		long version = RmiIO.readLong(in);

		defaultAccess = (IAccess)in.readObject();
		tableAccess = (Map<guid, IAccess>)in.readObject();
		fieldAccess= (Map<guid, IAccess>)in.readObject();
	}

	@Override
	public String toString() {
		return "defaults: " + defaultAccess + '\n' + 
				(tableAccess != null ? tableAccess.toString() : "table access: default") + '\n' +
				(fieldAccess != null ? fieldAccess.toString() : "field access: default");
	}
}
