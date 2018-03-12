package org.zenframework.z8.server.db.sql;

public enum SortDirection {
	Asc(Names.Asc),
	Desc(Names.Desc);

	class Names {
		static protected final String Asc = "asc";
		static protected final String Desc = "desc";
	}

	private String fName = null;

	SortDirection(String name) {
		fName = name;
	}

	@Override
	public String toString() {
		return fName;
	}

	static public SortDirection fromString(String string) {
		for (SortDirection dir : values())
			if (dir.fName.equals(string))
				return dir;
		throw new RuntimeException("Unknown sort direction: '" + string + "'");
	}
}
