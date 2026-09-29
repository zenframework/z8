package org.zenframework.z8.server.base.table.system.view;

import java.util.Collection;

import org.zenframework.z8.server.base.application.Application;
import org.zenframework.z8.server.base.form.action.Action;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.system.TransportQueue;
import org.zenframework.z8.server.ie.Message;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.guid;

public class DownloadMessageAction extends Action {
	static public class CLASS<T extends DownloadMessageAction> extends Action.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(DownloadMessageAction.class);
			setDisplayName(TransportQueue.displayNames.DownloadMessage);
		}

		@Override
		public Object newObject(IObject container) {
			return new DownloadMessageAction(container);
		}
	}

	public DownloadMessageAction(IObject container) {
		super(container);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void z8_execute(RCollection records, Query.CLASS<? extends Query> context, RCollection selected, Query.CLASS<? extends Query> query) {
		TransportQueue record = TransportQueue.newInstance();
		for (guid recordId : (Collection<guid>) records) {
			Message message = record.getMessage(recordId);
			String jsonString = message.toJson().toString();
			file json = file.createTempFile("tq-" + message.getId() + "-", "json");
			json.write(jsonString);
			Application.z8_print(json);
		}
	}

}
