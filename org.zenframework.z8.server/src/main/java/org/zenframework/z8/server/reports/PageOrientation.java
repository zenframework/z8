package org.zenframework.z8.server.reports;

public enum PageOrientation {
	Portrait(Names.Portrait),
	Landscape(Names.Landscape);

	class Names {
		static protected final String Portrait = "portrait";
		static protected final String Landscape = "landscape";
	}

	private String fName = null;

	PageOrientation(String name) {
		fName = name;
	}

	@Override
	public String toString() {
		return fName;
	}

	static public PageOrientation fromString(String string) {
		if(Names.Portrait.equalsIgnoreCase(string)) {
			return PageOrientation.Portrait;
		} else if(Names.Landscape.equalsIgnoreCase(string)) {
			return PageOrientation.Landscape;
		} else {
			throw new RuntimeException("Unknown page orientation: '" + string + "'");
		}
	}
}
