package org.zenframework.z8.server.db;

import org.zenframework.z8.server.utils.StringUtils;

public enum DatabaseVendor {
	Oracle(names.Oracle),
	SqlServer(names.SqlServer),
	Postgres(names.Postgres),
	H2(names.H2);

	class names {
		static protected final String Oracle = "Oracle";
		static protected final String SqlServer = "SqlServer";
		static protected final String Postgres = "Postgres";
		static protected final String H2 = "H2";
	}

	private String fName = null;

	DatabaseVendor(String name) {
		fName = name;
	}

	@Override
	public String toString() {
		return fName;
	}

	public static DatabaseVendor fromString(String string) {
		if(string == null)
			return DatabaseVendor.Postgres;

		string = string.toUpperCase();

		if(string.contains(names.Oracle.toUpperCase()))
			return DatabaseVendor.Oracle;
		else if(string.contains(names.SqlServer.toUpperCase()))
			return DatabaseVendor.SqlServer;
		else if(string.contains(names.Postgres.toUpperCase()))
			return DatabaseVendor.Postgres;
		else if(string.contains(names.H2.toUpperCase()))
			return DatabaseVendor.H2;
		else
			return DatabaseVendor.Postgres;
	}

	public String sqlName(String name) {
		return this == Oracle && name.length() > 15 ? StringUtils.translit(name, 30) : name;
	}

	public String quote(String name) {
		return quoteOpen() + sqlName(name) + quoteClose();
	}

	private char quoteOpen() {
		return this == Oracle || this == Postgres || this == H2 ? '"' : '[';
	}

	private char quoteClose() {
		return this == Oracle || this == Postgres || this == H2 ? '"' : ']';
	}
}
