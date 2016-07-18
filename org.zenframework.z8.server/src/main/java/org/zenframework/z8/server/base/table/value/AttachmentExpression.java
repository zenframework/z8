package org.zenframework.z8.server.base.table.value;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.datetime;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.string;

public class AttachmentExpression extends TextExpression {

	public static class CLASS<T extends AttachmentExpression> extends TextExpression.CLASS<T> {

		public CLASS(IObject container) {
			super(container);
			setJavaClass(AttachmentExpression.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new AttachmentExpression(container);
		}
	}

	public AttachmentExpression(IObject container) {
		super(container);
	}

	@SuppressWarnings("unchecked")
	@Override
	public string z8_get() {
		Query container = ((Query.CLASS<Query>) getContainer().getCLASS()).get();
		String attachmentName = attachmentName();
		if (attachmentName != null) {
			JsonArray arr = new JsonArray();
			JsonObject obj = new JsonObject();
			String fileName = new StringBuilder(1024).append("table/").append(container.classId()).append('/')
					.append(container.recordId()).append('/').append(contentFieldName()).append('/')
					.append(attachmentName()).toString();
			obj.put(Json.size, attachmentSize());
			obj.put(Json.time, attachmentDatetime());
			obj.put(Json.name, attachmentName);
			obj.put(Json.path, fileName);
			arr.put(obj);
			return new string(arr.toString());
		}
		return new string("");
	}

	protected int attachmentSize() {
		return z8_attachmentSize().getInt();
	}

	protected datetime attachmentDatetime() {
		return z8_attachmentDatetime();
	}

	protected String attachmentName() {
		return z8_attachmentName().get();
	}

	protected String contentFieldName() {
		return z8_contentFieldName().get();
	}

	protected integer z8_attachmentSize() {
		return new integer(0);
	}

	protected datetime z8_attachmentDatetime() {
		return new datetime();
	}

	protected string z8_attachmentName() {
		throw new UnsupportedOperationException("Method attachmentName() must be overriden");
	}

	protected string z8_contentFieldName() {
		throw new UnsupportedOperationException("Method contentFieldName() must be overriden");
	}

}
