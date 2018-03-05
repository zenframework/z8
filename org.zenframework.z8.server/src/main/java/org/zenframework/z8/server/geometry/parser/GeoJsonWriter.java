package org.zenframework.z8.server.geometry.parser;

import java.util.Collection;

import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.types.geometry;

public class GeoJsonWriter {
	private JsonWriter writer = new JsonWriter();
	private geometry source;

	static public String write(geometry source) {
		return new GeoJsonWriter(source).write();
	}

	private GeoJsonWriter(geometry source) {
		this.source = source;
	}

	public String write() {
		writeGeometry(source);
		return writer.toString();
	}

	private void writeGeometry(geometry geo) {
		writer.startObject();

		int shape = geo.shape();
		writer.writeProperty(GeoJson.Type, GeoJson.geoJsonType(shape));

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

		writer.finishObject();
	}

	private void writePosition(geometry position) {
		writer.write(position.x());
		writer.write(position.y());
	}

	private void writePoint(geometry geometry) {
		writer.startArray(GeoJson.Coordinates);
		writePosition(geometry);
		writer.finishArray();
	}

	private void writePoints(Collection<geometry> positions) {
		for(geometry position :  positions) {
			writer.startArray();
			writePosition(position);
			writer.finishArray();
		}
	}

	private void writeLine(geometry geometry) {
		writer.startArray(GeoJson.Coordinates);
		writePoints(geometry.points());
		writer.finishArray();
	}

	private void writePolygon(geometry geometry) {
		writer.startArray(GeoJson.Coordinates);

		for(geometry ring : geometry.points()) {
			writer.startArray();
			writePoints(ring.points());
			writer.finishArray();
		}

		writer.finishArray();
	}

	private void writeMultiPoint(geometry geometry) {
		writer.startArray(GeoJson.Coordinates);
		writePoints(geometry.points());
		writer.finishArray();
	}

	private void writeMultiLine(geometry geometry) {
		writer.startArray(GeoJson.Coordinates);

		for(geometry line : geometry.points())
			writePoints(line.points());

		writer.finishArray();
	}

	private void writeMultiPolygon(geometry geometry) {
		writer.startArray(GeoJson.Coordinates);

		for(geometry line : geometry.points())
			writePoints(line.points());

		writer.finishArray();
	}

	private void writeCollection(geometry collection) {
		writer.startArray(GeoJson.Geometries);

		for(geometry geometry : collection.points())
			writeGeometry(geometry);

		writer.finishArray();
	}
}
