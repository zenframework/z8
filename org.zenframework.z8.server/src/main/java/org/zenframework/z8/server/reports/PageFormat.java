package org.zenframework.z8.server.reports;

public enum PageFormat {
	A0(Names.A0),
	A1(Names.A1),
	A2(Names.A2),
	A3(Names.A3),
	A4(Names.A4),
	A5(Names.A5),
	A6(Names.A6),
	C3(Names.C3),
	C4(Names.C4),
	C5(Names.C5),
	C6(Names.C6),
	Letter(Names.Letter);

	class Names {
		static protected final String A0 = "A0";
		static protected final String A1 = "A1";
		static protected final String A2 = "A2";
		static protected final String A3 = "A3";
		static protected final String A4 = "A4";
		static protected final String A5 = "A5";
		static protected final String A6 = "A6";
		static protected final String C3 = "C3";
		static protected final String C4 = "C4";
		static protected final String C5 = "C5";
		static protected final String C6 = "C6";
		static protected final String Letter = "Letter";
	}

	private String fName = null;

	PageFormat(String name) {
		fName = name;
	}

	@Override
	public String toString() {
		return fName;
	}

	static public PageFormat fromString(String string) {
		if(Names.A0.equalsIgnoreCase(string)) {
			return PageFormat.A0;
		} else if(Names.A1.equalsIgnoreCase(string)) {
			return PageFormat.A1;
		} else if(Names.A2.equalsIgnoreCase(string)) {
			return PageFormat.A2;
		} else if(Names.A3.equalsIgnoreCase(string)) {
			return PageFormat.A3;
		} else if(Names.A4.equalsIgnoreCase(string)) {
			return PageFormat.A4;
		} else if(Names.A5.equalsIgnoreCase(string)) {
			return PageFormat.A5;
		} else if(Names.A6.equalsIgnoreCase(string)) {
			return PageFormat.A6;
		} else if(Names.C3.equalsIgnoreCase(string)) {
			return PageFormat.C3;
		} else if(Names.C4.equalsIgnoreCase(string)) {
			return PageFormat.C4;
		} else if(Names.C5.equalsIgnoreCase(string)) {
			return PageFormat.C5;
		} else if(Names.C6.equalsIgnoreCase(string)) {
			return PageFormat.C6;
		} else if(Names.Letter.equalsIgnoreCase(string)) {
			return PageFormat.Letter;
		} else {
			throw new RuntimeException("Unknown page format: '" + string + "'");
		}
	}

	static public float pageWidth(PageFormat format) {
		switch(format) {
		case A0:
			return 841;
		case A1:
			return 594;
		case A2:
			return 420;
		case A3:
			return 297;
		case A4:
			return 210;
		case A5:
			return 148;
		case A6:
			return 105;
		case C3:
			return 324;
		case C4:
			return 229;
		case C5:
			return 162;
		case C6:
			return 114;
		case Letter:
			return 215.9F;
		default:
			throw new RuntimeException("Unknown page width; format: '" + format.toString() + "'");
		}
	}

	static public float pageHeight(PageFormat format) {
		switch(format) {
		case A0:
			return 1189;
		case A1:
			return 841;
		case A2:
			return 594;
		case A3:
			return 420;
		case A4:
			return 297;
		case A5:
			return 210;
		case A6:
			return 148;
		case C3:
			return 458;
		case C4:
			return 324;
		case C5:
			return 229;
		case C6:
			return 162;
		case Letter:
			return 355.6F;
		default:
			throw new RuntimeException("Unknown page height; format: '" + format.toString() + "'");
		}
	}
}
