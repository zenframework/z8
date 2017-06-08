package org.zenframework.z8.server.base.table.value;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.string;

public class TextExpression extends StringExpression {
	public static class CLASS<T extends TextExpression> extends StringExpression.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(TextExpression.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new TextExpression(container);
		}
	}

	public bool html;

	public TextExpression(IObject container) {
		super(container);
		setDefault(new string());
		length = new integer(0);
	}

	@Override
	public FieldType type() {
		return FieldType.Text;
	}

	@Override
	public void writeMeta(JsonWriter writer, Query query, Query context) {
		super.writeMeta(writer, query, context);
		writer.writeProperty(Json.html, html);
	}
}
