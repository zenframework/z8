package org.zenframework.z8.server.routing;

import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;

/**
 * The action represents a map of entities aliases
 */
public class AliasTable extends OBJECT {
	public static class CLASS<T extends AliasTable> extends OBJECT.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(AliasTable.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new AliasTable(container);
		}
	}

	public AliasTable(IObject container) {
		super(container);
	}

	@Override
	public void writeResponse(JsonWriter writer) {
		writer.writeProperty(Json.data, EntitiesAliasMap.instance());
	}
}
