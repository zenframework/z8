package org.zenframework.z8.server.request.actions;

import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.request.IResponse;
import org.zenframework.z8.server.types.binary;

public class ContentAction extends RequestAction {
	private IResponse response;

	public ContentAction(ActionConfig config) {
		super(config);
	}

	@Override
	public void processRequest(IResponse response) throws Throwable {
		this.response = response;
		super.processRequest(response);
	}

	@Override
	public void writeResponse(JsonWriter writer) throws Throwable {
		Connection connection = ConnectionManager.get();

		try {
			connection.beginTransaction();
			binary binary = getQuery().getContent();
			if(binary != null)
				response.setInputStream(binary.get());
			connection.commit();
		} catch(Throwable e) {
			connection.rollback();
			throw new RuntimeException(e);
		}
	}
}
