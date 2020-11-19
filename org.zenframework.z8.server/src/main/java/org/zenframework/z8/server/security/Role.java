package org.zenframework.z8.server.security;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.zenframework.z8.server.engine.RmiIO;
import org.zenframework.z8.server.types.guid;

public class Role implements IRole {
	private static final long serialVersionUID = -8469285386216624596L;

	static public guid User = new guid("421A8413-A8EC-4DAB-9235-9A5FF83341C5");
	static public guid Guest = new guid("BE41CEB5-02DF-44EE-885F-B82DDEDCAA08");
	static public guid Administrator = new guid("DC08CA72-C668-412F-91B7-022F1C82AC09");

	static public class displayNames {
		public final static String User = "User";
		public final static String Guest = "Guest";
		public final static String Administrator = "Administrator";
	}

	private guid id = null;
	private String name = null;
	private IAccess access = null;

	static public Role administrator() {
		return new Role(Administrator, displayNames.Administrator, Access.administrator());
	}

	static public Role user() {
		return new Role(User, displayNames.User, Access.user());
	}

	static public Role guest() {
		return new Role(Guest, displayNames.Guest, Access.guest());
	}

	public Role() {
	}

	public Role(guid id, String name, IAccess access) {
		this.id = id;
		this.name = name;
		this.access = access;
	}

	@Override
	public guid id() {
		return id;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public IAccess access() {
		return access;
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

		RmiIO.writeGuid(out, id);
		out.writeObject(access);
	}

	@Override
	public void deserialize(ObjectInputStream in) throws IOException, ClassNotFoundException {
		@SuppressWarnings("unused")
		long version = RmiIO.readLong(in);

		id = RmiIO.readGuid(in);
		access = (IAccess)in.readObject();
	}
}