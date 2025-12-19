package org.zenframework.z8.server.reports;

import org.eclipse.birt.report.model.api.elements.DesignChoiceConstants;

public enum VertAlign {
	Top(Names.Top, DesignChoiceConstants.VERTICAL_ALIGN_TOP),
	Middle(Names.Middle, DesignChoiceConstants.VERTICAL_ALIGN_MIDDLE),
	Bottom(Names.Bottom, DesignChoiceConstants.VERTICAL_ALIGN_BOTTOM);

	class Names {
		static protected final String Top = "top";
		static protected final String Middle = "middle";
		static protected final String Bottom = "bottom";
	}

	private String fName = null;
	private String designAlignment = null;

	VertAlign(String name, String design) {
		fName = name;
		designAlignment = design;
	}

	@Override
	public String toString() {
		return fName;
	}

	public String toDesignAlign() {
		return designAlignment;
	}

	static public VertAlign fromString(String string) {
		if(Names.Top.equalsIgnoreCase(string))
			return VertAlign.Top;
		else if(Names.Middle.equalsIgnoreCase(string))
			return VertAlign.Middle;
		else if(Names.Bottom.equalsIgnoreCase(string))
			return VertAlign.Bottom;
		else
			throw new RuntimeException("Unknown alignment: '" + string + "'");
	}
}
