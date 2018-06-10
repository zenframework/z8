package org.zenframework.z8.server.types.sql;

import org.zenframework.z8.server.db.sql.SqlConst;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.expressions.Group;
import org.zenframework.z8.server.db.sql.expressions.Intersects;
import org.zenframework.z8.server.db.sql.expressions.Operation;
import org.zenframework.z8.server.db.sql.expressions.Rel;
import org.zenframework.z8.server.db.sql.functions.conversion.ToString;
import org.zenframework.z8.server.db.sql.functions.geometry.CollectionSize;
import org.zenframework.z8.server.db.sql.functions.geometry.Element;
import org.zenframework.z8.server.db.sql.functions.geometry.EndPoint;
import org.zenframework.z8.server.db.sql.functions.geometry.Extract;
import org.zenframework.z8.server.db.sql.functions.geometry.GeometryType;
import org.zenframework.z8.server.db.sql.functions.geometry.Intersection;
import org.zenframework.z8.server.db.sql.functions.geometry.Length;
import org.zenframework.z8.server.db.sql.functions.geometry.Split;
import org.zenframework.z8.server.db.sql.functions.geometry.StartPoint;
import org.zenframework.z8.server.db.sql.functions.geometry.Union;
import org.zenframework.z8.server.types.geometry;

public class sql_geometry extends sql_primary {
	public sql_geometry() {
		super(new SqlConst(new geometry()));
	}

	public sql_geometry(geometry value) {
		super(new SqlConst(value));
	}

	public sql_geometry(SqlToken token) {
		super(token);
	}

	@Override
	public sql_string z8_toString() {
		return new sql_string(new ToString(this));
	}

	public sql_geometry operatorPriority() {
		return new sql_geometry(new Group(this));
	}

	public sql_bool operatorEqu(sql_geometry value) {
		return new sql_bool(new Rel(this, Operation.Eq, value));
	}

	public sql_bool operatorNotEqu(sql_geometry value) {
		return new sql_bool(new Rel(this, Operation.NotEq, value));
	}

	public sql_bool operatorAnd(sql_geometry value) {
		return new sql_bool(new Intersects(this, value));
	}

	public sql_integer z8_collectionSize() {
		return new sql_integer(new CollectionSize(this));
	}

	public sql_decimal z8_length() {
		return new sql_decimal(new Length(this));
	}

	public sql_geometry z8_element(sql_integer n) {
		return new sql_geometry(new Element(this, n));
	}

	public sql_geometry z8_split(sql_geometry splitter) {
		return new sql_geometry(new Split(this, splitter));
	}

	public sql_geometry z8_extract(sql_integer type) {
		return new sql_geometry(new Extract(this, type));
	}

	public sql_string z8_strType() {
		return new sql_string(new GeometryType(this));
	}

	public sql_bool z8_intersects(sql_geometry geom) {
		return new sql_bool(new Intersects(this, geom));
	}

	public sql_geometry z8_intersect(sql_geometry geom) {
		return new sql_geometry(new Intersection(this, geom));
	}

	public sql_geometry z8_startPoint() {
		return new sql_geometry(new StartPoint(this));
	}

	public sql_geometry z8_endPoint() {
		return new sql_geometry(new EndPoint(this));
	}

	public sql_geometry z8_union() {
		return new sql_geometry(new Union(this));
	}
}
