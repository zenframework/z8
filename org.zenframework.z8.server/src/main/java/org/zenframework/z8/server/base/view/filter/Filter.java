package org.zenframework.z8.server.base.view.filter;

import java.util.ArrayList;
import java.util.Collection;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.db.sql.Sql;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.expressions.And;
import org.zenframework.z8.server.db.sql.expressions.Or;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.json.parser.JsonObject;

public class Filter implements IFilter {
	private String logical;
	private Collection<IFilter> expressions;

	public Filter(String json, Query query) {
		this(new JsonArray(json == null ? "[]" : (!json.startsWith("[") ? "[" + json + "]" : json)), query);
	}

	public Filter(Collection<String> json, Query query) {
		for(String object : json)
			addExpression(new JsonObject(object), query);
	}

	protected Filter(JsonArray jsonArray, Query query) {
		for(Object object : jsonArray)
			addExpression((JsonObject)object, query);
	}

	protected Filter(JsonObject json, Query query) {
		this.logical = json.getString(Json.logical);
		JsonArray expressions = json.getJsonArray(Json.expressions);
		for(Object object : expressions)
			addExpression((JsonObject)object, query);
	}

	private void addExpression(JsonObject expression, Query query) {
		boolean isGroup = expression.has(Json.logical);
		if(expressions == null)
			expressions = new ArrayList<IFilter>();
		expressions.add(isGroup ? new Filter(expression, query) : new Expression(expression, query));
	}

	@Override
	public SqlToken where() {
		SqlToken result = null;

		if(expressions == null)
			return null;

		boolean unaryNot = Json.not.equalsIgnoreCase(logical);

		for(IFilter expression : expressions) {
			SqlToken where = expression.where();
			if(where == null)
				continue;
			if(result == null)
				result = where;
			else if(Json.or.equalsIgnoreCase(logical))
				result = new Or(result, where);
			else
				result = new And(result, where);
		}

		return result != null ? (unaryNot ? Sql.not(result) : Sql.group(result)) : null;
	}
}
