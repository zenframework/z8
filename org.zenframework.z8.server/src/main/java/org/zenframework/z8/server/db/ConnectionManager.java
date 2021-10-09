package org.zenframework.z8.server.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.engine.Database;

public class ConnectionManager {
	static private Map<Database, List<Connection>> databaseConnections = new HashMap<Database, List<Connection>>();

	public static Database getDatabase() {
		return get().getDatabase();
	}

	public static DatabaseVendor getVendor() {
		return getDatabase().getVendor();
	}

	public static Connection get() {
		return get(ApplicationServer.getDatabase());
	}

	public static Connection get(Database database) {
		List<Connection> connections =  null;

		Object lock = database.getLock();

		synchronized(lock) {
			connections = databaseConnections.get(database);

			if(connections == null) {
				connections = new ArrayList<Connection>();
				databaseConnections.put(database, connections);
			}

			for(Connection connection : connections) {
				if(connection.isCurrent())
					return connection;
			}

			for(Connection connection : connections) {
				if(!connection.isInUse()) {
					connection.use();
					return connection;
				}
			}
		}

		Connection connection = Connection.connect(database);
		connection.use();

		synchronized(lock) {
			connections.add(connection);
		}

		return connection;
	}

	public static void release() {
		for(Map.Entry<Database, List<Connection>> entry : databaseConnections.entrySet()) {
			synchronized(entry.getKey().getLock()) {
				List<Connection> connections = entry.getValue();
				Iterator<Connection> iterator = connections.iterator();

				while(iterator.hasNext()) {
					Connection connection = iterator.next();

					if(connection.isCurrent())
						connection.release();

					if(connection.isUnused()) {
						connection.close();
						iterator.remove();
					}
				}
			}
		}
	}
}
