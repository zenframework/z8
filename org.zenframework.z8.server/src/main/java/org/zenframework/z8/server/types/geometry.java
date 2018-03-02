package org.zenframework.z8.server.types;

import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.types.sql.sql_geometry;

public final class geometry extends primary {
	private static final long serialVersionUID = 8678133849134310611L;

	static private String emptyValue = "{\"type\": \"Point\", \"coordinates\": [0, 0] }";

	static public geometry Null = new geometry();

	private String value = emptyValue;

	static public integer DefaultSRS = new integer(96872);

	public integer srs = DefaultSRS;

	public geometry() {
	}

	public geometry(geometry str) {
		set(str);
	}

	public geometry(String str) {
		set(str);
	}

	public geometry(string str) {
		set(str.get());
	}

	public String get() {
		return value;
	}

	public void set(geometry geometry) {
		set(geometry != null ? geometry.value : null);
	}

	public void set(string value) {
		set(value.get());
	}

	public void set(String value) {
		this.value = (value != null && !value.isEmpty()) ? value : emptyValue;
	}

	@Override
	public String toString() {
		return get();
	}

	@Override
	public FieldType type() {
		return FieldType.Geometry;
	}

	@Override
	public String toDbConstant(DatabaseVendor vendor) {
		return "'" + get() + "'";
	}

	static public String toDbGeometry(String value) {
		return "st_setSRID(st_geomFromGeoJson(" + value + "), " + DefaultSRS.get() + ")";
	}

	public String toDbGeometry(DatabaseVendor vendor) {
		return "st_setSRID(st_geomFromGeoJson(" + toDbConstant(vendor) + "), " + srs.get() + ")";
	}

	@Override
	public int hashCode() {
		return get().hashCode();
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof geometry)
			return operatorEqu((geometry)object).get();
		return false;
	}

	@Override
	public int compareTo(primary primary) {
		if(primary instanceof geometry) {
			geometry geometry = (geometry)primary;
			return value.compareTo(geometry.value);
		}

		return -1;
	}

	public sql_geometry sql_geometry() {
		return new sql_geometry(this);
	}

	public bool operatorEqu(geometry x) {
		return new bool(value.equals(x.value));
	}

	public bool operatorNotEqu(geometry x) {
		return new bool(!value.equals(x.value));
	}

	public void operatorAssign(geometry value) {
		set(value);
	}

	public void operatorAssign(string value) {
		set(value);
	}
}
