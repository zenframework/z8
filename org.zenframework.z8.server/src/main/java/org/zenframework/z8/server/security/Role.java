package org.zenframework.z8.server.security;

import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.types.guid;

public enum Role {
	User(guids.User),
	Administrator(guids.Administrator);

	static public class strings {
		static public final String User = "Role.user";
		static public final String Administrator = "Role.administrator";
	}

	static public class displayNames {
		public final static String User = Resources.get(strings.User);
		public final static String Administrator = Resources.get(strings.Administrator);
	}

	static class guids {
		static protected final guid User = new guid("421A8413-A8EC-4DAB-9235-9A5FF83341C5");
		static protected final guid Administrator = new guid("DC08CA72-C668-412F-91B7-022F1C82AC09");
	}

	private guid id = null;

	Role(guid id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return id.toString();
	}

	public guid guid() {
		return id;
	}

	static public Role fromGuid(guid guid) {
		if(guids.User.equals(guid)) {
			return Role.User;
		} else if(guids.Administrator.equals(guid)) {
			return Role.Administrator;
		} else {
			throw new RuntimeException("Unknown security group: '" + guid + "'");
		}
	}

}
