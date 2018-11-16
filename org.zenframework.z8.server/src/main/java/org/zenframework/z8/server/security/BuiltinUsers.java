package org.zenframework.z8.server.security;

import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.types.guid;

public enum BuiltinUsers {
	System(names.system),
	Administrator(names.administrator);

	class names {
		static final String system = "00000000-0000-0000-0000-000000000001";
		static final String administrator = "00000000-0000-0000-0000-000000000002";
	}

	static public class strings {
		static public final String SystemName = "BuiltinUsers.system.name";
		static public final String SystemDescription = "BuiltinUsers.system.description";
		static public final String AdministratorName = "BuiltinUsers.administrator.name";
		static public final String AdministratorDescription = "BuiltinUsers.administrator.description";
	}

	static public class displayNames {
		public final static String SystemName = Resources.get(strings.SystemName);
		public final static String SystemDescription = Resources.get(strings.SystemDescription);
		public final static String AdministratorName = Resources.get(strings.AdministratorName);
		public final static String AdministratorDescription = Resources.get(strings.AdministratorDescription);
	}

	private String fName = null;

	BuiltinUsers(String name) {
		fName = name;
	}

	@Override
	public String toString() {
		return fName;
	}

	public guid guid() {
		return new guid(fName);
	}

	static public BuiltinUsers fromGuid(guid guid) {
		return fromString(guid.toString());
	}

	static public BuiltinUsers fromString(String string) {
		if(names.system.equals(string))
			return BuiltinUsers.System;
		else if(names.administrator.equals(string))
			return BuiltinUsers.Administrator;
		else
			throw new RuntimeException("Unknown builtin user id: '" + string + "'");
	}
}
