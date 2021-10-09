package org.zenframework.z8.server.base.table.value;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.security.Access;
import org.zenframework.z8.server.types.primary;

public interface IField extends IObject {
	public FieldType type();
	public FieldType metaType();

	public String sqlType(DatabaseVendor vendor);

	public int size();
	public int scale();

	public primary getDefaultValue();

	public primary get();
	public void set(primary value);

	public primary parse(String value);

	public boolean wasNull();
	public boolean changed();

	public Query owner();
	public Access access();

	public boolean isPrimaryKey();
	public boolean isParentKey();
	public boolean isLockKey();
	public boolean isExpression();
}
