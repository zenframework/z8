package org.zenframework.z8.server.types;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
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

	static public final int none = -1;
	static public final int ring = 0;
	static public final int point = 1;
	static public final int line = 2;
	static public final int polygon = 3;
	static public final int multiPoint = 4;
	static public final int multiLine = 5;
	static public final int multiPolygon = 6;
	static public final int collection = 7;

	static public final integer None = new integer(none);
	static public final integer Ring = new integer(ring);
	static public final integer Point = new integer(point);
	static public final integer Line = new integer(line);
	static public final integer Polygon = new integer(polygon);
	static public final integer MultiPoint = new integer(multiPoint);
	static public final integer MultiLine = new integer(multiLine);
	static public final integer MultiPolygon = new integer(multiPolygon);
	static public final integer Collection = new integer(collection);

	static public double[][] EmptyExtent = { {0, 0}, {0, 0} };

	static public int DefaultSRS = 96872;

	private String bytes;

	private int shape = none;
	private double x;
	private double y;
	private Collection<geometry> points;
	private double[][] extent;                 // [[xMin, yMin], [xMax, yMax]]

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
		shape = point;
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
		if(bytes != null && shape == none)
			set(BinaryReader.read(bytes));
	}

	private void checkBytes() {
		if(bytes == null && shape != none)
			bytes = BinaryWriter.writeHexString(this);
	}

	public boolean isEmpty() {
		return bytes == null && shape == none;
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
		shape = geometry != null ? geometry.shape : none;
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
		return shape != none ? "st_geomFromEWKT('" + get() + "')" : null;
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

	public double x1() {
		return extent()[0][0];
	}

	public double y1() {
		return extent()[0][1];
	}

	public double x2() {
		return extent()[1][0];
	}

	public double y2() {
		return extent()[1][1];
	}

	public double width() {
		double[][] extent = extent();
		return extent[1][0] - extent[0][0];
	}

	public double height() {
		double[][] extent = extent();
		return extent[1][1] - extent[0][1];
	}

	public geometry center() {
		int shape = shape();
		if(shape == point || shape == none)
			return this;

		double[][] extent = extent();
		return new geometry((extent[0][0] + extent[1][0]) / 2, (extent[0][1] + extent[1][1]) / 2);
	}

	private double[] point() {
		int shape = shape();
		if(shape == point || shape == none)
			return new double[] { x, y };
		throw new IllegalArgumentException("Geometry type must be 1 (Point), have " + shape);
	}

	public double[][] extent() {
		if(extent != null)
			return extent;

		switch(shape()) {
		case none:
		case point:
			return extent = new double[][]{ point(), point() };
		case line:
		case ring:
		case polygon:
		case multiPoint:
		case multiLine:
		case multiPolygon:
		case collection:
			for(geometry geometry : points()) {
				if(extent == null)
					extent = geometry.extent();
				else
					extent = unionExtents(extent, geometry.extent());
			}
			return extent = extent == null ? EmptyExtent : extent;
		default:
			throw new IllegalArgumentException("Unknown geometry type: " + shape);
		}
	}

	static public double[][] unionExtents(double[][] extent1, double[][] extent2) {
		return new double[][] {
			new double[] { Math.min(extent1[0][0], extent2[0][0]), Math.min(extent1[0][1], extent2[0][1]) },
			new double[] { Math.max(extent1[1][0], extent2[1][0]), Math.max(extent1[1][1], extent2[1][1]) }
		};
	}

	public integer z8_shape() {
		return new integer(shape());
	}

	public decimal z8_x() {
		return new decimal(x());
	}

	public decimal z8_y() {
		return new decimal(y());
	}

	public geometry z8_center() {
		return center();
	}

	@SuppressWarnings("rawtypes")
	public RCollection z8_points() {
		return new RCollection<geometry>(points());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public RCollection z8_extent() {
		double[][] extent = extent();
		return new RCollection(new decimal[][] { { new decimal(extent[0][0]), new decimal(extent[0][1]) }, { new decimal(extent[1][0]), new decimal(extent[1][1]) } });
	}

	public decimal z8_x1() {
		return new decimal(x1());
	}

	public decimal z8_y1() {
		return new decimal(y1());
	}

	public decimal z8_x2() {
		return new decimal(x2());
	}

	public decimal z8_y2() {
		return new decimal(y2());
	}

	public decimal z8_width() {
		return new decimal(width());
	}

	public decimal z8_height() {
		return new decimal(height());
	}

	static public geometry z8_point(decimal x, decimal y) {
		return new geometry(x.getDouble(), y.getDouble());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	static public geometry z8_line(RCollection points) {
		return new geometry(points, line);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	static public geometry z8_collection(RCollection geometries) {
		return new geometry(geometries, collection);
	}

	static public geometry fromHexString(String hexString) {
		return new geometry(hexString);
	}

	static public geometry fromGeoJson(String json) {
		return (json == null || json.isEmpty()) ? new geometry() : GeoJsonReader.read(json);
	}

	static public geometry z8_fromHexString(string hexString) {
		return fromHexString(hexString.get());
	}

	static public geometry z8_fromGeoJson(string json) {
		return fromGeoJson(json.get());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	static public geometry z8_fromArray(RCollection geometries) {
		Collection<geometry> points = new ArrayList<geometry>();
		for(geometry geometry : (Collection<geometry>)geometries) {
			if(geometry.shape() == collection)
				points.addAll(geometry.points());
			else if(geometry.shape() != none)
				points.add(geometry);
		}
		return new geometry(points, collection);
	}
}