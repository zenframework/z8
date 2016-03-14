package org.zenframework.z8.server.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.engine.Database;

public class ConnectionManager {
	static private Map<String, List<Connection>> schemas = new HashMap<String, List<Connection>>();

	public static synchronized Connection get() {
		return get(ApplicationServer.database());
	}

	public static synchronized Connection get(Database database) {
		if(database == null)
			database = ApplicationServer.database();

		List<Connection> connections = schemas.get(database.schema());

		if(connections == null) {
			connections = new ArrayList<Connection>();
			schemas.put(database.schema(), connections);
		}

		for(Connection connection : connections) {
			if(connection.isCurrent())
				return connection;
		}

		Iterator<Connection> iterator = connections.iterator();
		while(iterator.hasNext()) {
			Connection connection = iterator.next();
			if(connection.isUnused()) {
				connection.safeClose();
				iterator.remove();
			}
		}

		for(Connection connection : connections) {
			if(!connection.isInUse()) {
				connection.use();
				return connection;
			}
		}

		Connection connection = Connection.connect(database);
		connections.add(connection);

		connection.use();
		return connection;
	}

	public static synchronized void release() {
		release(ApplicationServer.database());
	}

	public static synchronized void release(Database database) {
		if(database == null)
			database = ApplicationServer.database();

		List<Connection> connections = schemas.get(database.schema());

		if(connections == null)
			return;

		for(Connection connection : connections) {
			if(connection.isCurrent()) {
				connection.release();
				return;
			}
		}

		Iterator<Connection> iterator = connections.iterator();
		while(iterator.hasNext()) {
			Connection connection = iterator.next();
			if(connection.isUnused()) {
				connection.safeClose();
				iterator.remove();
			}
		}
	}
}
