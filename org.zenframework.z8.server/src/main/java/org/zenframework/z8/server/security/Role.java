package org.zenframework.z8.server.security;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.zenframework.z8.server.engine.RmiIO;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.types.guid;

public class Role implements IRole {
	private static final long serialVersionUID = -8469285386216624596L;

	static public guid User = new guid("421A8413-A8EC-4DAB-9235-9A5FF83341C5");
	static public guid Guest = new guid("BE41CEB5-02DF-44EE-885F-B82DDEDCAA08");
	static public guid Administrator = new guid("DC08CA72-C668-412F-91B7-022F1C82AC09");
	static public guid Site = new guid("A991DAB6-24E2-4A92-8460-CA043C302B13");

	static public class strings {
		static public final String User = "Role.user";
		static public final String Guest = "Role.guest";
		static public final String Administrator = "Role.administrator";
	}

	static public class displayNames {
		public final static String User = Resources.get(strings.User);
		public final static String Guest = Resources.get(strings.Guest);
		public final static String Administrator = Resources.get(strings.Administrator);
	}

	private guid id = null;
	private IAccess access = null;

	static public Role administrator() {
		return new Role(Administrator, Access.administrator());
	}

	static public Role user() {
		return new Role(User, Access.user());
	}

	static public Role guest() {
		return new Role(Guest, Access.guest());
	}

	static public Role site() {
		return new Role(Site, Access.site());
	}

	public Role() {
	}

	public Role(guid id, IAccess access) {
		this.id = id;
		this.access = access;
	}

	public guid id() {
		return id;
	}

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