package org.zenframework.z8.server.geometry.parser;

import java.util.Collection;

import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.types.geometry;

public class GeoJsonWriter {
	private JsonWriter writer = new JsonWriter();
	private Object source;

	static public String write(geometry source) {
		return new GeoJsonWriter(source).write();
	}

	static public String write(Collection<geometry> geometries) {
		return new GeoJsonWriter(geometries).write();
	}

	private GeoJsonWriter(Object source) {
		this.source = source;
	}

	@SuppressWarnings("unchecked")
	public String write() {
		if(source instanceof geometry)
			writeGeometry((geometry)source);
		else
			writeFeatureCollection((Collection<geometry>)source);
		return writer.toString();
	}

	private void writeGeometry(geometry geo) {
		writer.startObject();

		int shape = geo.shape();
		writer.writeProperty(GeoJson.Type, GeoJson.geoJsonType(shape));

		switch(shape) {
		case geometry.point:
			writePoint(geo);
			break;
		case geometry.line:
			writeLine(geo);
			break;
		case geometry.polygon:
			writePolygon(geo);
			break;
		case geometry.multiPoint:
			writeMultiPoint(geo);
			break;
		case geometry.multiLine:
			writeMultiLine(geo);
			break;
		case geometry.multiPolygon:
			writeMultiPolygon(geo);
			break;
		case geometry.collection:
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

	private void writePoints(Collection<geometry> positions) {
		for(geometry position :  positions) {
			writer.startArray();
			writePosition(position);
			writer.finishArray();
		}
	}

	private void writePolygonPoints(geometry polygon) {
		for(geometry ring : polygon.points()) {
			writer.startArray();
			writePoints(ring.points());
			writer.finishArray();
		}
	}

	private void writePoint(geometry geometry) {
		writer.startArray(GeoJson.Coordinates);
		writePosition(geometry);
		writer.finishArray();
	}

	private void writeLine(geometry geometry) {
		writer.startArray(GeoJson.Coordinates);
		writePoints(geometry.points());
		writer.finishArray();
	}

	private void writePolygon(geometry geometry) {
		writer.startArray(GeoJson.Coordinates);
		writePolygonPoints(geometry);
		writer.finishArray();
	}

	private void writeMultiPoint(geometry geometry) {
		writer.startArray(GeoJson.Coordinates);
		writePoints(geometry.points());
		writer.finishArray();
	}

	private void writeMultiLine(geometry geometry) {
		writer.startArray(GeoJson.Coordinates);

		for(geometry line : geometry.points()) {
			writer.startArray();
			writePoints(line.points());
			writer.finishArray();
		}

		writer.finishArray();
	}

	private void writeMultiPolygon(geometry geometry) {
		writer.startArray(GeoJson.Coordinates);

		for(geometry polygon : geometry.points()) {
			writer.startArray();
			writePolygonPoints(polygon);
			writer.finishArray();
		}

		writer.finishArray();
	}

	private void writeCollection(geometry collection) {
		writer.startArray(GeoJson.Geometries);

		for(geometry element : collection.points()) {
			if(!element.isEmpty())
				writeGeometry(element);
		}

		writer.finishArray();
	}

	private void writeFeature(geometry geometry) {
		writer.startObject();
		writer.writeProperty(GeoJson.Type, GeoJson.Feature);
		writer.writeJsonProperty(GeoJson.Geometry, new GeoJsonWriter(source).write());
		writer.finishObject();
	}

	private void writeFeatureCollection(Collection<geometry> geometries) {
		writer.startObject();

		writer.writeProperty(GeoJson.Type, GeoJson.FeatureCollection);

		writer.startObject(GeoJson.Properties);
		writer.finishObject();

		writer.startArray(GeoJson.Features);
		for(geometry geometry : geometries)
			writeFeature(geometry);
		writer.finishArray();

		writer.finishObject();
	}
}
