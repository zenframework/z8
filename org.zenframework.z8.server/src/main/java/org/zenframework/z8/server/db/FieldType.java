package org.zenframework.z8.server.db;

public enum FieldType {
	None(DataTypes.None),
	Null(DataTypes.Null),

	Attachments(DataTypes.Attachments),
	Binary(DataTypes.Binary),
	Boolean(DataTypes.Boolean),
	Date(DataTypes.Date),
	Datetime(DataTypes.Datetime),
	Datespan(DataTypes.Datespan),
	Decimal(DataTypes.Decimal),
	File(DataTypes.File),
	Geometry(DataTypes.Geometry),
	Guid(DataTypes.Guid),
	Integer(DataTypes.Integer),
	String(DataTypes.String),
	Text(DataTypes.Text);

	class DataTypes {
		static protected final String None = "none";
		static protected final String Null = "null";

		static protected final String Attachments = "attachments";
		static protected final String Binary = "binary";
		static protected final String Boolean = "boolean";
		static protected final String Date = "date";
		static protected final String Datetime = "datetime";
		static protected final String Datespan = "datespan";
		static protected final String Decimal = "float";
		static protected final String File = "file";
		static protected final String Geometry = "geometry";
		static protected final String Guid = "guid";
		static protected final String Integer = "int";
		static protected final String String = "string";
		static protected final String Text = "text";
	}

	private String fName = null;

	FieldType(String name) {
		fName = name;
	}

	@Override
	public String toString() {
		return fName;
	}

	static public FieldType fromString(String string) {
		for(FieldType type : values())
			if(type.fName.equals(string))
				return type;
		throw new RuntimeException("Unknown data type: '" + string + "'");
	}

	static public FieldType fromExcel(String type) {
		if(type.equalsIgnoreCase("VARCHAR") || type.equalsIgnoreCase("TEXT"))
			return FieldType.String;
		else if(type.equalsIgnoreCase("NUMBER") || type.equalsIgnoreCase("CURRENCY"))
			return FieldType.Decimal;
		else if(type.equalsIgnoreCase("DATETIME"))
			return FieldType.Date;

		return FieldType.String;
	}

	public static FieldType parse(String name, Object... params) {
		name = name.toLowerCase();

		if (name.equals("blob") || name.equals("varbinary") || name.equals("bytea"))
			return Binary;

		if (name.equals("raw") || name.equals("uniqueidentifier") || name.equals("uuid"))
			return Guid;

		if (name.equals("tinyint") || name.equals("smallint"))
			return Boolean;

		if (name.equals("bigint"))
			return Integer;

		if (name.equals("numeric"))
			return Decimal;

		if (name.equals("geometry"))
			return Geometry;

		if (name.equals("nvarchar2") || name.equals("nvarchar") || name.equals("character varying"))
			return String;

		if (!name.equals("number"))
			throw new IllegalArgumentException("Unknown SQL type " + name);

		int precision = params.length > 0 && params[0] instanceof Number ? ((Number) params[0]).intValue() : 0;
		int scale = params.length > 1 && params[1] instanceof Number ? ((Number) params[1]).intValue() : 0;

		if (precision == 1)
			return Boolean;

		return scale == 0 ? Integer : Decimal;
	}

	public boolean isNumeric() {
		return this == Integer || this == Decimal;
	}

	public boolean isDate() {
		return this == Date || this == Datetime;
	}
}
