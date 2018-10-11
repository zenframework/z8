package org.zenframework.z8.server.db.sql.functions.date;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;

public class ExtractDatePart extends SqlToken {
	final static String Second = "second";
	final static String Minute = "minute";
	final static String Hour = "hour";

	final static String Day = "day";
	final static String Month = "month";
	final static String Year = "year";
	
	private SqlToken date;
	private String part;

	protected ExtractDatePart(SqlToken date, String part) {
		this.date = date;
		this.part = part;
	}

	@Override
	public void collectFields(Collection<IField> fields) {
		date.collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		switch(vendor) {
		case Postgres:
			return "extract(" + part + " from to_timestamp(" + date.format(vendor, options) + " / 1000))";
		case Oracle:
		case SqlServer:
		default:
			throw new UnknownDatabaseException();
		}
	}

	@Override
	public FieldType type() {
		return FieldType.Integer;
	}
}
