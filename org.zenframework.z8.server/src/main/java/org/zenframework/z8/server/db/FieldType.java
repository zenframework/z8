package org.zenframework.z8.server.db;

import org.zenframework.z8.server.exceptions.db.UnknownDataTypeException;

import java.sql.Types;

public enum FieldType {
    None(DataTypes.None),
    Guid(DataTypes.Guid),
    Boolean(DataTypes.Boolean),
    Integer(DataTypes.Integer),
    String(DataTypes.String),
    Date(DataTypes.Date),
    Datetime(DataTypes.DateTime),
    Datespan(DataTypes.DateSpan),
    Decimal(DataTypes.Decimal),
    Binary(DataTypes.Binary),
    Text(DataTypes.Text),
    File(DataTypes.File),
    Null(DataTypes.Null);

    class DataTypes {
        static protected final String None = "none";
        static protected final String Guid = "guid";
        static protected final String Boolean = "boolean";
        static protected final String Integer = "int";
        static protected final String String = "string";
        static protected final String Date = "date";
        static protected final String DateTime = "datetime";
        static protected final String DateSpan = "datespan";
        static protected final String Decimal = "float";
        static protected final String Binary = "binary";
        static protected final String Text = "text";
        static protected final String File = "file";
        static protected final String Null = "null";
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
        if(DataTypes.None.equals(string)) {
            return FieldType.None;
        }
        else if(DataTypes.Guid.equals(string)) {
            return FieldType.Guid;
        }
        else if(DataTypes.Boolean.equals(string)) {
            return FieldType.Boolean;
        }
        else if(DataTypes.Integer.equals(string)) {
            return FieldType.Integer;
        }
        else if(DataTypes.String.equals(string)) {
            return FieldType.String;
        }
        else if(DataTypes.Date.equals(string)) {
            return FieldType.Date;
        }
        else if(DataTypes.DateTime.equals(string)) {
            return FieldType.Datetime;
        }
        else if(DataTypes.DateSpan.equals(string)) {
            return FieldType.Datespan;
        }
        else if(DataTypes.Decimal.equals(string)) {
            return FieldType.Decimal;
        }
        else if(DataTypes.Binary.equals(string)) {
            return FieldType.Binary;
        }
        else if(DataTypes.Text.equals(string)) {
            return FieldType.Text;
        }
        else if(DataTypes.File.equals(string)) {
            return FieldType.File;
        }
        else if(DataTypes.Null.equals(string)) {
            return FieldType.Null;
        }
        else {
            throw new RuntimeException("Unknown data type: '" + string + "'");
        }
    }

    static public FieldType fromExcel(String type) {
        if(type.equalsIgnoreCase("VARCHAR") || type.equalsIgnoreCase("TEXT")) {
            return FieldType.String;
        }
        else if(type.equalsIgnoreCase("NUMBER") || type.equalsIgnoreCase("CURRENCY")) {
            return FieldType.Decimal;
        }
        else if(type.equalsIgnoreCase("DATETIME")) {
            return FieldType.Datetime;
        }

        return FieldType.String;
    }

    public int jdbcType() {
        switch(this) {
        case Guid:
            return Types.CHAR;
        case Boolean:
            return Types.BIT;
        case Integer:
            return Types.BIGINT;
        case String:
            return Types.VARCHAR;
        case Date:
            return Types.DATE;
        case Datetime:
            return Types.TIMESTAMP;
        case Datespan:
            return Types.BIGINT;
        case Decimal:
            return Types.DECIMAL;
        case Text:
        case Binary:
            return Types.LONGVARBINARY;
        default:
            throw new UnknownDataTypeException(this);
        }
    }

    public String vendorType(DatabaseVendor vendor) {
        if(this == Guid) {
            if(vendor == DatabaseVendor.Oracle)
                return "RAW";
            else if(vendor == DatabaseVendor.SqlServer)
                return "UNIQUEIDENTIFIER";
            else if(vendor == DatabaseVendor.Postgres)
                return "uuid";
        }
        else if(this == Boolean) {
            if(vendor == DatabaseVendor.Oracle)
                return "NUMBER";
            else if(vendor == DatabaseVendor.SqlServer)
                return "TINYINT";
            else if(vendor == DatabaseVendor.Postgres)
                return "smallint";
        }
        else if(this == Datespan || this == Integer) {
            if(vendor == DatabaseVendor.Oracle)
                return "NUMBER";
            else if(vendor == DatabaseVendor.SqlServer)
                return "BIGINT";
            else if(vendor == DatabaseVendor.Postgres)
                return "bigint";
        }
        else if(this == String) {
            if(vendor == DatabaseVendor.Oracle)
                return "NVARCHAR2";
            else if(vendor == DatabaseVendor.SqlServer)
                return "NVARCHAR";
            else if(vendor == DatabaseVendor.Postgres)
                return "character varying";
        }
        else if(this == Date || this == Datetime) {
            if(vendor == DatabaseVendor.Oracle)
                return "DATE";
            else if(vendor == DatabaseVendor.SqlServer)
                return "DATETIME";
            else if(vendor == DatabaseVendor.Postgres)
                return "timestamp without time zone";
        }
        else if(this == Decimal) {
            if(vendor == DatabaseVendor.Oracle)
                return "NUMBER";
            else if(vendor == DatabaseVendor.SqlServer)
                return "NUMERIC";
            else if(vendor == DatabaseVendor.Postgres)
                return "numeric";
        }
        else if(this == Text || this == Binary) {
            if(vendor == DatabaseVendor.Oracle)
                return "BLOB";
            else if(vendor == DatabaseVendor.SqlServer)
                return "VARBINARY";
            else if(vendor == DatabaseVendor.Postgres)
                return "bytea";
        }
        else if(this == File) {
            if(vendor == DatabaseVendor.Postgres)
                return "bytea";
            throw new UnknownDataTypeException(this);
        }

        throw new UnknownDataTypeException(this);
    }
}
