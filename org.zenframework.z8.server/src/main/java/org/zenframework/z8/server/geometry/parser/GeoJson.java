package org.zenframework.z8.server.geometry.parser;

import org.zenframework.z8.server.types.geometry;

public class GeoJson {
	static public final String Type = "type";
	static public final String Coordinates = "coordinates";
	static public final String Geometries = "geometries";
	static public final String Geometry = "geometry";
	static public final String Properties = "properties";
	static public final String Features = "features";

	static public final String Point = "Point";
	static public final String Line = "LineString";
	static public final String Polygon = "Polygon";
	static public final String MultiPoint = "MultiPoint";
	static public final String MultiLine = "MultiLineString";
	static public final String MultiPolygon = "MultiPolygon";
	static public final String Collection = "GeometryCollection";

	static public final String Feature = "Feature";
	static public final String FeatureCollection = "FeatureCollection";

	static public int geometryType(String type) {
		if(type.equals(Point))
			return geometry.point;
		else if(type.equals(Line))
			return geometry.line;
		else if(type.equals(Polygon))
			return geometry.polygon;
		else if(type.equals(MultiPoint))
			return geometry.multiPoint;
		else if(type.equals(MultiLine))
			return geometry.multiLine;
		else if(type.equals(MultiPolygon))
			return geometry.multiPolygon;
		else if(type.equals(Collection))
			return geometry.collection;
		return geometry.none;
	}

	static public String geoJsonType(int type) {
		switch(type) {
		case geometry.point:
			return Point;
		case geometry.line:
			return Line;
		case geometry.polygon:
			return Polygon;
		case geometry.multiPoint:
			return MultiPoint;
		case geometry.multiLine:
			return MultiLine;
		case geometry.multiPolygon:
			return MultiPolygon;
		case geometry.collection:
			return Collection;
		default:
			throw new IllegalArgumentException("Unknown Geometry Type: " + type);
		}
	}
}
