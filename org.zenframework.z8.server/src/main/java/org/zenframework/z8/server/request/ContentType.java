package org.zenframework.z8.server.request;

public enum ContentType {
	Text(Names.Text),
	Html(Names.Html),
	Xml(Names.Xml),
	Json(Names.Json),
	Binary(Names.Binary);

	class Names {
		static protected final String Text = "text/plain";
		static protected final String Html = "text/html";
		static protected final String Xml = "application/xml";
		static protected final String Json = "application/json";
		static protected final String Binary = "application/octet-stream";
	}

	private String fName = null;

	ContentType(String name) {
		fName = name;
	}

	@Override
	public String toString() {
		return fName;
	}

	static public ContentType fromString(String string) {
		for(ContentType type : values())
			if(type.fName.equals(string))
				return type;
		throw new RuntimeException("Unknown content type: '" + string + "'");
	}
}
