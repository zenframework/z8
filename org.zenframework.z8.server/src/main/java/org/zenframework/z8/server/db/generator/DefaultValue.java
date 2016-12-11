package org.zenframework.z8.server.db.generator;

import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.types.binary;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;

public class DefaultValue {
	static public String get(DatabaseVendor vendor, Field field) {
		primary value = field.getDefaultValue();

		if(field.type() == FieldType.Text)
			value = new binary((string)value);

		return value.toDbConstant(vendor);
	}
}
