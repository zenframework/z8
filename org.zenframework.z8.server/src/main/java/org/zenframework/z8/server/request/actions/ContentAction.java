package org.zenframework.z8.server.request.actions;

import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.request.IResponse;
import org.zenframework.z8.server.types.binary;

public class ContentAction extends RequestAction {
	public ContentAction(ActionConfig config) {
		super(config);
	}

	@Override
	public void processRequest(IResponse response) throws Throwable {
		Connection connection = ConnectionManager.get();

		try {
			connection.beginTransaction();
			binary binary = getQuery().getContent();
			response.setInputStream(binary != null ? binary.get() : null);
			connection.commit();
		} catch(Throwable e) {
			connection.rollback();
			throw new RuntimeException(e);
		}
	}

	@Override
	public void writeResponse(JsonWriter writer) throws Throwable {
		throw new UnsupportedOperationException();
	}
}
