package org.zenframework.z8.server.base.table.value;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.types.primary;

public interface IField {
	public String name();

	public String displayName();

	public FieldType type();

	public int size();

	public int scale();

	public primary getDefaultValue();

	public String sqlType(DatabaseVendor vendor);

	public Query owner();

	public primary get();

	public void set(primary value);
}
