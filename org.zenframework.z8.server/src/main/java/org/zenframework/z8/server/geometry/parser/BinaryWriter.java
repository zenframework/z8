package org.zenframework.z8.server.geometry.parser;

import java.io.ByteArrayOutputStream;
import java.util.Collection;

import org.zenframework.z8.server.types.geometry;

public class BinaryWriter {
	private geometry source;
	private ByteArrayOutputStream bytes = new ByteArrayOutputStream();
	private boolean asText;

	static private final char[] hextypes = "0123456789ABCDEF".toCharArray();

	static public String write(geometry source) {
		return new BinaryWriter(source, true).write().toString();
	}

	static public byte[] writeBytes(geometry source) {
		return new BinaryWriter(source, false).write();
	}

	private BinaryWriter(geometry source, boolean asText) {
		this.source = source;
		this.asText = asText;
	}

	private byte[] write() {
		writeGeometry(source);
		return bytes.toByteArray();
	}

	private void writeGeometry(geometry geo) {
		setByte((byte)(asText ? 1 : 0));

		int shape = geo.shape();
		int srs = geo.srs();

		setInt(srs != 0 ? shape | 0x20000000 : shape);

		if(srs != 0)
			setInt(srs);

		switch(shape) {
		case geometry.Point:
			writePoint(geo);
			break;
		case geometry.Line:
			writeLine(geo);
			break;
		case geometry.Polygon:
			writePolygon(geo);
			break;
		case geometry.MultiPoint:
			writeMultiPoint(geo);
			break;
		case geometry.MultiLine:
			writeMultiLine(geo);
			break;
		case geometry.MultiPolygon:
			writeMultiPolygon(geo);
			break;
		case geometry.Collection:
			writeCollection(geo);
			break;
		default :
			throw new IllegalArgumentException("Unknown Geometry Type: " + shape);
		}
	}

	private void writePoint(geometry geometry) {
		setDouble(geometry.x());
		setDouble(geometry.y());
	}

	private void writeGeometryArray(Collection<geometry> geometries) {
		for(geometry geometry : geometries)
			writeGeometry(geometry);
	}

	private void writePoints(geometry geometry) {
		Collection<geometry> points = geometry.points();
		setInt(points.size());
		for(geometry point : points)
			writePoint(point);
	}

	private void writeLine(geometry geometry) {
		writePoints(geometry);
	}

	private void writePolygon(geometry geometry) {
		Collection<geometry> rings = geometry.points();
		setInt(rings.size());
		for(geometry ring : rings)
			writePoints(ring);
	}

	private void writeMultiPoint(geometry geometry) {
		Collection<geometry> points = geometry.points();
		setInt(points.size());
		writeGeometryArray(points);
	}

	private void writeMultiLine(geometry geometry) {
		Collection<geometry> lines = geometry.points();
		setInt(lines.size());
		writeGeometryArray(lines);
	}

	private void writeMultiPolygon(geometry geometry) {
		Collection<geometry> polygons = geometry.points();
		setInt(polygons.size());
		writeGeometryArray(polygons);
	}

	private void writeCollection(geometry geometry) {
		Collection<geometry> geometries = geometry.points();
		setInt(geometries.size());
		writeGeometryArray(geometries);
	}

	private void setByte(byte b) {
		if(asText) {
			bytes.write(hextypes[(b >>> 4) & 0xF]);
			bytes.write(hextypes[b & 0xF]);
		} else
			bytes.write(b);
	}

	private void setInt(int value) {
		if(asText) {
			setByte((byte) value);
			setByte((byte) (value >>> 8));
			setByte((byte) (value >>> 16));
			setByte((byte) (value >>> 24));
		} else {
			setByte((byte) (value >>> 24));
			setByte((byte) (value >>> 16));
			setByte((byte) (value >>> 8));
			setByte((byte) value);
		}
	}

	protected void setLong(long value) {
		if(asText) {
			setByte((byte) value);
			setByte((byte) (value >>> 8));
			setByte((byte) (value >>> 16));
			setByte((byte) (value >>> 24));
			setByte((byte) (value >>> 32));
			setByte((byte) (value >>> 40));
			setByte((byte) (value >>> 48));
			setByte((byte) (value >>> 56));
		} else {
			setByte((byte) (value >>> 56));
			setByte((byte) (value >>> 48));
			setByte((byte) (value >>> 40));
			setByte((byte) (value >>> 32));
			setByte((byte) (value >>> 24));
			setByte((byte) (value >>> 16));
			setByte((byte) (value >>> 8));
			setByte((byte) value);
		}
	}

	public void setDouble(double data) {
		long bitrep = Double.doubleToLongBits(data);
		setLong(bitrep);
	}
}
