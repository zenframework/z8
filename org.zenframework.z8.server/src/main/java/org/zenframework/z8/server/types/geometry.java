package org.zenframework.z8.server.types;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collection;

import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.geometry.parser.BinaryReader;
import org.zenframework.z8.server.geometry.parser.BinaryWriter;
import org.zenframework.z8.server.geometry.parser.GeoJsonReader;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.sql.sql_geometry;

public final class geometry extends primary {
	private static final long serialVersionUID = 8678133849134310611L;

	static public final int None = -1;
	static public final int Ring = 0;
	static public final int Point = 1;
	static public final int Line = 2;
	static public final int Polygon = 3;
	static public final int MultiPoint = 4;
	static public final int MultiLine = 5;
	static public final int MultiPolygon = 6;
	static public final int Collection = 7;

	static public int DefaultSRS = 96872;

	private String bytes;

	private int shape = None;
	private double x;
	private double y;
	private Collection<geometry> points;

	private int srs = DefaultSRS;

	public geometry() {
		this((geometry)null);
	}

	public geometry(geometry geometry) {
		set(geometry);
	}

	public geometry(double x, double y) {
		this(x, y, DefaultSRS, null);
	}

	public geometry(double x, double y, int srs, String bytes) {
		this.bytes = bytes;
		shape = Point;
		this.srs = srs;
		this.x = x;
		this.y = y;
	}

	public geometry(Collection<geometry> points, int shape) {
		this(points, shape, DefaultSRS, null);
	}

	public geometry(Collection<geometry> points, int shape, int srs, String bytes) {
		this.bytes = bytes;
		this.shape = shape;
		this.srs = srs;
		this.points = points;
	}

	public geometry(String bytes) {
		this.bytes = (bytes == null || bytes.isEmpty()) ? null : bytes;
	}

	public int shape() {
		checkShape();
		return shape;
	}

	public int srs() {
		checkShape();
		return srs;
	}

	public double x() {
		checkShape();
		return x;
	}

	public double y() {
		checkShape();
		return y;
	}

	public Collection<geometry> points() {
		checkShape();
		return points;
	}

	private void checkShape() {
		if(bytes != null && shape == None)
			set(BinaryReader.read(bytes));
	}

	private void checkBytes() {
		if(bytes == null && shape != None)
			bytes = BinaryWriter.write(this);
	}

	public boolean isEmpty() {
		return bytes == null && shape == None;
	}

	public String get() {
		checkBytes();
		return bytes;
	}

	public InputStream stream() {
		return isEmpty() ? null : new ByteArrayInputStream(BinaryWriter.writeBytes(this));
	}

	public void set(geometry geometry) {
		bytes = geometry != null ? geometry.bytes : null;
		shape = geometry != null ? geometry.shape : None;
		x = geometry != null ? geometry.x : 0;
		y = geometry != null ? geometry.y : 0;
		points = geometry != null ? geometry.points : null;
		srs = geometry != null ? geometry.srs : DefaultSRS;
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
		return shape != None ? "st_geomFromEWKT('" + get() + "')" : null;
	}

	@Override
	public int hashCode() {
		String bytes = get();
		return bytes != null ? bytes.hashCode() : super.hashCode();
	}

	@Override
	public boolean equals(Object object) {
		String bytes = get();
		if(bytes != null && object instanceof geometry) {
			geometry geometry = (geometry)object;
			return bytes.equals(geometry.get());
		}
		return false;
	}

	@Override
	public int compareTo(primary primary) {
		String bytes = get();
		if(bytes != null && primary instanceof geometry) {
			geometry geometry = (geometry)primary;
			return bytes.compareTo(geometry.get());
		}

		return -1;
	}

	public sql_geometry sql_geometry() {
		return new sql_geometry(this);
	}

	public bool operatorEqu(geometry x) {
		return new bool(equals(x));
	}

	public bool operatorNotEqu(geometry x) {
		return new bool(!equals(x));
	}

	public void operatorAssign(geometry value) {
		set(value);
	}

	static public geometry z8_fromBinary(string binary) {
		return new geometry(binary.get());
	}

	static public geometry z8_fromGeoJson(string json) {
		return json.isEmpty() ? new geometry() : GeoJsonReader.read(json.get());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	static public geometry z8_fromArray(RCollection geometries) {
		return new geometry(geometries, Collection);
	}
}