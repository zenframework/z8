package org.zenframework.z8.server.base.table.value;

import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.runtime.IObject;
import  org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.string;

public class GeoJsonField extends StringField {
	static public integer Moscow = new integer(96872);
	static public string Point = new string("{\"type\": \"Point\", \"coordinates\": [0, 0] }");

	public static class CLASS<T extends GeoJsonField> extends StringField.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(GeoJsonField.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new GeoJsonField(container);
		}
	}

	public integer srs = new integer(Moscow);

	public GeoJsonField(IObject container) {
		super(container);
		indexed = bool.True;
		length = new integer(0);
		defaultValue = new string(Point);
	}

	@Override
	public FieldType type() {
		return FieldType.GeoJson;
	}

	@Override
	public FieldType metaType() {
		return FieldType.String;
	}

	@Override
	public String sqlType(DatabaseVendor vendor) {
		String name = type().vendorType(vendor);

		if(vendor == DatabaseVendor.Postgres)
			return name + "(Geometry, " + srs.get() + ")";

		return name;
	}

	@Override
	public String wrapForSelect(String value, DatabaseVendor vendor) {
		return "ST_AsGeoJSON(" + value + ")";
	}

	@Override
	public String wrapForInsert(String value, DatabaseVendor vendor) {
		return "ST_SetSRID(ST_GeomFromGeoJSON(" + value + "), " + srs.get() + ")";
	}
}
