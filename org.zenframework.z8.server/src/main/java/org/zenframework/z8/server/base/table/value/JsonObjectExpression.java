package org.zenframework.z8.server.base.table.value;

import org.zenframework.z8.server.base.json.parser.JsonObject;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.functions.json.JsonBuildObject;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.runtime.RLinkedHashMap;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.types.sql.sql_string;

@SuppressWarnings({"unchecked", "rawtypes"})
public class JsonObjectExpression extends Expression {
	public static class CLASS<T extends JsonObjectExpression> extends Expression.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(JsonObjectExpression.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new JsonObjectExpression(container);
		}
	}

	protected JsonBuildObject json;

	public JsonObjectExpression(IObject container) {
		super(container);
		aggregation = Aggregation.Array;
	}

	@Override
	public FieldType type() {
		return FieldType.String;
	}

	@Override
	public RCollection array(string json) {
		JsonArray jsonArray = new JsonArray(json.get());

		RCollection result = new RCollection();
		for(int i = 0; i < jsonArray.length(); i++)
			result.add(JsonObject.getJsonObject(jsonArray.getJsonObject(i)));

		return result;
	}

	@Override
	protected SqlToken z8_expression() {
		return json;
	}

	public JsonObject.CLASS<? extends JsonObject> z8_get() {
		return JsonObject.getJsonObject(new org.zenframework.z8.server.json.parser.JsonObject(((string)internalGet()).get()));
	}

	public sql_string sql_string() {
		return new sql_string(new SqlField(this));
	}

	public JsonObjectExpression.CLASS<? extends JsonObjectExpression> operatorAssign(RCollection fields) {
		json = new JsonBuildObject(CLASS.asList(fields));
		return (JsonObjectExpression.CLASS<?>)this.getCLASS();
	}

	public JsonObjectExpression.CLASS<? extends JsonObjectExpression> operatorAssign(RLinkedHashMap values) {
		json = new JsonBuildObject(values);
		return (JsonObjectExpression.CLASS<?>)this.getCLASS();
	}
}
