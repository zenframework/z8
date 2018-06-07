package org.zenframework.z8.server.security;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.engine.RmiIO;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.types.guid;

public class Privileges implements IPrivileges {
	static private final long serialVersionUID = 8845983028465477335L;

	static public class strings {
		static public final String NoReadAccess = "Privileges.noReadAccess";
		static public final String NoWriteAccess = "Privileges.noWriteAccess";
		static public final String NoDestroyAccess = "Privileges.noDestroyAccess";
		static public final String NoExecuteAccess = "Privileges.noExecuteAccess";
	}

	static public class displayNames {
		public final static String NoReadAccess = Resources.get(strings.NoReadAccess);
		public final static String NoWriteAccess = Resources.get(strings.NoWriteAccess);
		public final static String NoDestroyAccess = Resources.get(strings.NoDestroyAccess);
		public final static String NoExecuteAccess = Resources.get(strings.NoExecuteAccess);
	}

	IAccess defaultAccess;

	Map<guid, IAccess> tableAccess;
	Map<guid, IAccess> fieldAccess;
	Map<guid, IAccess> requestAccess;

	public Privileges() {
	}

	public Privileges(IAccess defaultAccess) {
		setDefaultAccess(defaultAccess);
	}

	public void setDefaultAccess(IAccess defaultAccess) {
		this.defaultAccess = defaultAccess;
	}

	public IAccess getTableAccess(Query table) {
		return getTableAccess(table.key());
	}

	public IAccess getTableAccess(guid table) {
		if(tableAccess == null)
			return defaultAccess;

		IAccess access = tableAccess.get(table);
		return access != null ? access : defaultAccess;
	}

	public void setTableAccess(guid table, IAccess access) {
		if(tableAccess == null)
			tableAccess = new HashMap<guid, IAccess>();
		tableAccess.put(table, access);
	}

	public IAccess getFieldAccess(Field field) {
		return getFieldAccess(field.owner().key(), field.key());
	}

	public IAccess getFieldAccess(guid table, guid field) {
		IAccess tableAccess = getTableAccess(table);

		if(fieldAccess == null)
			return tableAccess;

		IAccess access = fieldAccess.get(field);
		return access != null ? tableAccess.and(access) : tableAccess;
	}

	public void setFieldAccess(guid field, IAccess access) {
		if(fieldAccess == null)
			fieldAccess = new HashMap<guid, IAccess>();
		fieldAccess.put(field, access);
	}

	public IAccess getRequestAccess(guid request) {
		if(requestAccess == null)
			return defaultAccess;

		IAccess access = requestAccess.get(request);
		return access != null ? access : defaultAccess;
	}

	public void setRequestAccess(guid request, IAccess access) {
		if(requestAccess == null)
			requestAccess = new HashMap<guid, IAccess>();
		requestAccess.put(request, access);
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
		out.writeObject(requestAccess);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void deserialize(ObjectInputStream in) throws IOException, ClassNotFoundException {
		@SuppressWarnings("unused")
		long version = RmiIO.readLong(in);

		defaultAccess = (IAccess)in.readObject();
		tableAccess = (Map<guid, IAccess>)in.readObject();
		fieldAccess = (Map<guid, IAccess>)in.readObject();
		requestAccess = (Map<guid, IAccess>)in.readObject();
	}

	@Override
	public String toString() {
		return "defaults: " + defaultAccess + '\n' + 
				(tableAccess != null ? tableAccess.toString() : "table access: default") + '\n' +
				(fieldAccess != null ? fieldAccess.toString() : "field access: default");
	}
}
