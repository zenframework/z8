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
			return geometry.Point;
		else if(type.equals(Line))
			return geometry.Line;
		else if(type.equals(Polygon))
			return geometry.Polygon;
		else if(type.equals(MultiPoint))
			return geometry.MultiPoint;
		else if(type.equals(MultiLine))
			return geometry.MultiLine;
		else if(type.equals(MultiPolygon))
			return geometry.MultiPolygon;
		else if(type.equals(Collection))
			return geometry.Collection;
		return geometry.None;
	}

	static public String geoJsonType(int type) {
		switch(type) {
		case geometry.Point:
			return Point;
		case geometry.Line:
			return Line;
		case geometry.Polygon:
			return Polygon;
		case geometry.MultiPoint:
			return MultiPoint;
		case geometry.MultiLine:
			return MultiLine;
		case geometry.MultiPolygon:
			return MultiPolygon;
		case geometry.Collection:
			return Collection;
		default:
			throw new IllegalArgumentException("Unknown Geometry Type: " + type);
		}
	}
}
