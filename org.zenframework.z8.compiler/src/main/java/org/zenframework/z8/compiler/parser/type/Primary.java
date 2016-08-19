package org.zenframework.z8.compiler.parser.type;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.workspace.CompilationUnit;

public class Primary {
	final static public String Void = "void";
	final static public String Primary = "primary";
	final static public String Boolean = "bool";
	final static public String Integer = "int";
	final static public String Decimal = "decimal";
	final static public String String = "string";
	final static public String Date = "date";
	final static public String Datetime = "datetime";
	final static public String Datespan = "datespan";
	final static public String Guid = "guid";
	final static public String Binary = "binary";
	final static public String Exception = "exception";
	final static public String File = "file";

	final static public String SqlBoolean = "sql_bool";
	final static public String SqlInteger = "sql_int";
	final static public String SqlDecimal = "sql_decimal";
	final static public String SqlString = "sql_string";
	final static public String SqlDate = "sql_date";
	final static public String SqlDatetime = "sql_datetime";
	final static public String SqlDatespan = "sql_datespan";
	final static public String SqlGuid = "sql_guid";
	final static public String SqlBinary = "sql_binary";

	final static public String VoidPath = "org/zenframework/z8/lang/void.bl";
	final static public String PrimaryPath = "org/zenframework/z8/lang/primary.bl";
	final static public String BooleanPath = "org/zenframework/z8/lang/bool.bl";
	final static public String IntegerPath = "org/zenframework/z8/lang/int.bl";
	final static public String DecimalPath = "org/zenframework/z8/lang/decimal.bl";
	final static public String StringPath = "org/zenframework/z8/lang/string.bl";
	final static public String DatePath = "org/zenframework/z8/lang/date.bl";
	final static public String DatetimePath = "org/zenframework/z8/lang/datetime.bl";
	final static public String DatespanPath = "org/zenframework/z8/lang/datespan.bl";
	final static public String GuidPath = "org/zenframework/z8/lang/guid.bl";
	final static public String BinaryPath = "org/zenframework/z8/lang/binary.bl";
	final static public String ExceptionPath = "org/zenframework/z8/lang/exception.bl";
	final static public String FilePath = "org/zenframework/z8/lang/file.bl";

	final static public String SqlBooleanPath = "org/zenframework/z8/lang/sql/sql_bool.bl";
	final static public String SqlIntegerPath = "org/zenframework/z8/lang/sql/sql_int.bl";
	final static public String SqlDecimalPath = "org/zenframework/z8/lang/sql/sql_decimal.bl";
	final static public String SqlStringPath = "org/zenframework/z8/lang/sql/sql_string.bl";
	final static public String SqlDatePath = "org/zenframework/z8/lang/sql/sql_date.bl";
	final static public String SqlDatetimePath = "org/zenframework/z8/lang/sql/sql_datetime.bl";
	final static public String SqlDatespanPath = "org/zenframework/z8/lang/sql/sql_datespan.bl";
	final static public String SqlGuidPath = "org/zenframework/z8/lang/sql/sql_guid.bl";
	final static public String SqlBinaryPath = "org/zenframework/z8/lang/sql/sql_binary.bl";

	static private Map<String, IPath> nameToPathMap;
	static private Map<String, String> primaryToSqlMap;

	static {
		nameToPathMap = new HashMap<String, IPath>();

		nameToPathMap.put(Void, new Path(VoidPath));
		nameToPathMap.put(Primary, new Path(PrimaryPath));
		nameToPathMap.put(Boolean, new Path(BooleanPath));
		nameToPathMap.put(Integer, new Path(IntegerPath));
		nameToPathMap.put(Decimal, new Path(DecimalPath));
		nameToPathMap.put(String, new Path(StringPath));
		nameToPathMap.put(Date, new Path(DatePath));
		nameToPathMap.put(Datetime, new Path(DatetimePath));
		nameToPathMap.put(Datespan, new Path(DatespanPath));
		nameToPathMap.put(Guid, new Path(GuidPath));
		nameToPathMap.put(Binary, new Path(BinaryPath));
		nameToPathMap.put(Exception, new Path(ExceptionPath));
		nameToPathMap.put(File, new Path(FilePath));

		nameToPathMap.put(SqlBoolean, new Path(SqlBooleanPath));
		nameToPathMap.put(SqlInteger, new Path(SqlIntegerPath));
		nameToPathMap.put(SqlDecimal, new Path(SqlDecimalPath));
		nameToPathMap.put(SqlString, new Path(SqlStringPath));
		nameToPathMap.put(SqlDate, new Path(SqlDatePath));
		nameToPathMap.put(SqlDatetime, new Path(SqlDatetimePath));
		nameToPathMap.put(SqlDatespan, new Path(SqlDatespanPath));
		nameToPathMap.put(SqlGuid, new Path(SqlGuidPath));
		nameToPathMap.put(SqlBinary, new Path(SqlBinaryPath));

		primaryToSqlMap = new HashMap<String, String>();

		primaryToSqlMap.put(Boolean, SqlBoolean);
		primaryToSqlMap.put(Integer, SqlInteger);
		primaryToSqlMap.put(Decimal, SqlDecimal);
		primaryToSqlMap.put(String, SqlString);
		primaryToSqlMap.put(Date, SqlDate);
		primaryToSqlMap.put(Datetime, SqlDatetime);
		primaryToSqlMap.put(Datespan, SqlDatespan);
		primaryToSqlMap.put(Guid, SqlGuid);
		primaryToSqlMap.put(Binary, SqlBinary);
	}

	public static IPath nameToPath(String name) {
		return nameToPathMap.get(name);
	}

	public static IType resolveType(CompilationUnit compilationUnit, String name) {
		IType type = compilationUnit.resolveType(name);

		if(type == null) {
			compilationUnit.error(compilationUnit.getType().getPosition(), name + " cannot be resolved to a type");
		} else {
			compilationUnit.addContributor(type.getCompilationUnit());
		}

		return type;
	}

	public static String getSqlTypeName(String name) {
		return primaryToSqlMap.get(name);
	}
}
