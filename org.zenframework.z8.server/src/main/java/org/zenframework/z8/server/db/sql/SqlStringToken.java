package org.zenframework.z8.server.db.sql;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;

public class SqlStringToken extends SqlToken {
	private FieldType type;
	private String value;

	public SqlStringToken(String value, FieldType type) {
		this.type = type;
		this.value = value;
	}

	@Override
	public void collectFields(Collection<IField> fields) {
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) throws UnknownDatabaseException {
		return value;
	}

	@Override
	public FieldType type() {
		return type; //FieldType.Null;
	}
}
