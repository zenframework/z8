package org.zenframework.z8.server.db.generator;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.zenframework.z8.server.base.query.RecordLock;
import org.zenframework.z8.server.base.table.system.Requests;
import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.db.sql.expressions.UnaryNot;
import org.zenframework.z8.server.db.sql.functions.InVector;
import org.zenframework.z8.server.engine.Runtime;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.string;

public class RequestGenerator {
	@SuppressWarnings("unused")
	private ILogger logger;

	private Requests requests = new Requests.CLASS<Requests>().get();

	private Collection<guid> requestKeys = new HashSet<guid>();

	public RequestGenerator(ILogger logger) {
		this.logger = logger;
		requestKeys.addAll(Runtime.instance().requestKeys());
	}

	public void run() {
		Connection connection = ConnectionManager.get();

		try {
			connection.beginTransaction();
			writeRequests();
			connection.commit();
		} catch(Throwable e) {
			connection.rollback();
			throw new RuntimeException(e);
		}
	}

	private void writeRequests() {
		requests.read(Arrays.asList(requests.primaryKey()), new UnaryNot(new InVector(requests.primaryKey(), requestKeys)));

		while(requests.next()) {
			guid request = requests.recordId();
			requests.destroy(request);
		}

		createRequests();
	}

	private void createRequests() {
		requests.read(Arrays.asList(requests.primaryKey()), new InVector(requests.primaryKey(), requestKeys));
		while(requests.next()) {
			guid request = requests.recordId();
			setRequestProperties(Runtime.instance().getRequestByKey(request).newInstance());
			requests.update(request);
			requestKeys.remove(request);
		}

		for(guid request : requestKeys) {
			setRequestProperties(Runtime.instance().getRequestByKey(request).newInstance());
			requests.create(request);
		}
	}

	private void setRequestProperties(OBJECT request) {
		requests.classId.get().set(request.classId());
		requests.name.get().set(new string(request.displayName()));
		requests.lock.get().set(RecordLock.Destroy);
	}
}
