package org.zenframework.z8.server.base.table.value.aggregator;

import org.zenframework.z8.server.base.file.FileInfo;
import org.zenframework.z8.server.base.json.parser.JsonObject;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.bool;

public class AttachmentsAggregator extends JsonArrayAggregator {

	public static class CLASS<T extends JsonArrayAggregator> extends JsonArrayAggregator.CLASS<T> {

		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(AttachmentsAggregator.class);
			setName(AttachmentsAggregator.class.getName());
			setDisplayName(AttachmentsAggregator.class.getName());
		}

		@Override
		public Object newObject(IObject container) {
			return new AttachmentsAggregator(container);
		}

	}

	public AttachmentsAggregator(IObject container) {
		super(container);
	}

	@Override
	public bool z8_keepElement(JsonObject.CLASS<? extends JsonObject> obj) {
		return z8_keepFile(FileInfo.z8_parse(obj));
	}

	public bool z8_keepFile(FileInfo.CLASS<? extends FileInfo> fileInfo) {
		return new bool(true);
	}

	@Override
	public bool z8_equals(JsonObject.CLASS<? extends JsonObject> o1, JsonObject.CLASS<? extends JsonObject> o2) {
		return new bool(o1.get().z8_has(Json.id).get() && o2.get().z8_has(Json.id).get()
				&& o1.get().z8_getGuid(Json.id).get().equals(o2.get().z8_getGuid(Json.id).get())
				|| o1.get().z8_has(Json.path).get() && o2.get().z8_has(Json.path).get()
				&& o1.get().z8_getString(Json.path).get().equals(o2.get().z8_getString(Json.path).get()));
	}

}
