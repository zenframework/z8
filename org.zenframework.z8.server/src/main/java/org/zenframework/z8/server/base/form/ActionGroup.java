package org.zenframework.z8.server.base.form;

import org.zenframework.z8.server.base.form.action.Action;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.RCollection;

public class ActionGroup extends Section {
	public static class CLASS<T extends ActionGroup> extends Control.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(ActionGroup.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new ActionGroup(container);
		}
	}

	public RCollection<Action.CLASS<? extends Action>> actions = new RCollection<Action.CLASS<? extends Action>>();

	public ActionGroup(IObject container) {
		super(container);
	}

	@Override
	public void writeMeta(JsonWriter writer, Query query, Query context) {
		super.writeMeta(writer, query, context);
		writer.writeProperty(Json.isActionGroup, true);
		writer.writeActions(CLASS.asList(actions), query, context); 
	}
}
