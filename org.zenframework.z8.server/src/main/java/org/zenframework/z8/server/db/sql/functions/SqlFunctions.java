package org.zenframework.z8.server.db.sql.functions;

import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.datespan;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.decimal;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.types.sql.sql_bool;
import org.zenframework.z8.server.types.sql.sql_datespan;
import org.zenframework.z8.server.types.sql.sql_date;
import org.zenframework.z8.server.types.sql.sql_decimal;
import org.zenframework.z8.server.types.sql.sql_guid;
import org.zenframework.z8.server.types.sql.sql_integer;
import org.zenframework.z8.server.types.sql.sql_string;

public class SqlFunctions {
	static public sql_datespan z8_IIF(sql_bool value, sql_datespan yes, sql_datespan no) {
		return value.z8_IIF(yes, no);
	}

	static public sql_date z8_IIF(sql_bool value, sql_date yes, sql_date no) {
		return value.z8_IIF(yes, no);
	}

	static public sql_decimal z8_IIF(sql_bool value, sql_decimal yes, sql_decimal no) {
		return value.z8_IIF(yes, no);
	}

	static public sql_guid z8_IIF(sql_bool value, sql_guid yes, sql_guid no) {
		return value.z8_IIF(yes, no);
	}

	static public sql_integer z8_IIF(sql_bool value, sql_integer yes, sql_integer no) {
		return value.z8_IIF(yes, no);
	}

	static public sql_string z8_IIF(sql_bool value, sql_string yes, sql_string no) {
		return value.z8_IIF(yes, no);
	}

	static public sql_bool z8_NVL(sql_bool value, sql_bool variants) {
		return new sql_bool(new Nvl(value, variants));
	}

	static public sql_datespan z8_NVL(sql_datespan value, sql_datespan variants) {
		return new sql_datespan(new Nvl(value, variants));
	}

	static public sql_date z8_NVL(sql_date value, sql_date variants) {
		return new sql_date(new Nvl(value, variants));
	}

	static public sql_decimal z8_NVL(sql_decimal value, sql_decimal variants) {
		return new sql_decimal(new Nvl(value, variants));
	}

	static public sql_guid z8_NVL(sql_guid value, sql_guid variants) {
		return new sql_guid(new Nvl(value, variants));
	}

	static public sql_integer z8_NVL(sql_integer value, sql_integer variants) {
		return new sql_integer(new Nvl(value, variants));
	}

	static public sql_string z8_NVL(sql_string value, sql_string variants) {
		return new sql_string(new Nvl(value, variants));
	}

	static public sql_bool z8_inVector(sql_bool value, RCollection<bool> variants) {
		return new sql_bool(new InVector(value, variants));
	}

	static public sql_bool z8_inVector(sql_datespan value, RCollection<datespan> variants) {
		return new sql_bool(new InVector(value, variants));
	}

	static public sql_bool z8_inVector(sql_date value, RCollection<date> variants) {
		return new sql_bool(new InVector(value, variants));
	}

	static public sql_bool z8_inVector(sql_decimal value, RCollection<decimal> variants) {
		return new sql_bool(new InVector(value, variants));
	}

	static public sql_bool z8_inVector(sql_guid value, RCollection<guid> variants) {
		return new sql_bool(new InVector(value, variants));
	}

	static public sql_bool z8_inVector(sql_integer value, RCollection<integer> variants) {
		return new sql_bool(new InVector(value, variants));
	}

	static public sql_bool z8_inVector(sql_string value, RCollection<string> variants) {
		return new sql_bool(new InVector(value, variants));
	}
}
