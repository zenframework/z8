package org.zenframework.z8.server.security;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.engine.RmiIO;
import org.zenframework.z8.server.engine.RmiSerializable;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.types.guid;

public class Privileges implements RmiSerializable, Serializable {
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

	Access defaultAccess;

	Map<guid, Access> tableAccess;
	Map<guid, Access> fieldAccess;
	Map<guid, Access> requestAccess;

	public Privileges() {
	}

	public Privileges(Access defaultAccess) {
		setDefaultAccess(defaultAccess);
	}

	public void setDefaultAccess(Access defaultAccess) {
		this.defaultAccess = defaultAccess;
	}

	public Access getTableAccess(Query table) {
		return getTableAccess(table.key());
	}

	public Access getTableAccess(guid table) {
		if(tableAccess == null)
			return defaultAccess;

		Access access = tableAccess.get(table);
		return access != null ? access : defaultAccess;
	}

	public void setTableAccess(guid table, Access access) {
		if(tableAccess == null)
			tableAccess = new HashMap<guid, Access>();
		tableAccess.put(table, access);
	}

	public Access getFieldAccess(Field field) {
		Query owner = field.owner();
		return getFieldAccess(owner != null ? owner.key() : null, field.key());
	}

	public Access getFieldAccess(guid table, guid field) {
		Access tableAccess = getTableAccess(table);

		if(fieldAccess == null)
			return tableAccess;

		Access access = fieldAccess.get(field);
		return access != null ? tableAccess.and(access) : tableAccess;
	}

	public void setFieldAccess(guid field, Access access) {
		if(fieldAccess == null)
			fieldAccess = new HashMap<guid, Access>();
		fieldAccess.put(field, access);
	}

	public Access getRequestAccess(guid request) {
		if(requestAccess == null)
			return defaultAccess;

		Access access = requestAccess.get(request);
		return access != null ? access : defaultAccess;
	}

	public void setRequestAccess(guid request, Access access) {
		if(requestAccess == null)
			requestAccess = new HashMap<guid, Access>();
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

		defaultAccess = (Access)in.readObject();
		tableAccess = (Map<guid, Access>)in.readObject();
		fieldAccess = (Map<guid, Access>)in.readObject();
		requestAccess = (Map<guid, Access>)in.readObject();
	}
}
