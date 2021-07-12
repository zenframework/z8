package org.zenframework.z8.server.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.engine.IDatabase;

public class ConnectionManager {
	static private Map<String, List<Connection>> databaseConnections = new HashMap<String, List<Connection>>();
	static private Map<String, IDatabase> databases = new HashMap<String, IDatabase>();

	public static IDatabase database() {
		return get().database();
	}

	public static DatabaseVendor vendor() {
		return database().vendor();
	}

	public static Connection get() {
		return get(ApplicationServer.getDatabase());
	}

	public static Connection get(IDatabase database) {
		List<Connection> connections =  null;

		Object lock = database.getLock();

		synchronized(lock) {
			String key = database.key();
			connections = databaseConnections.get(key);

			if(connections == null) {
				connections = new ArrayList<Connection>();
				databaseConnections.put(key, connections);
				databases.put(key, database);
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
		for(Map.Entry<String, List<Connection>> entry : databaseConnections.entrySet()) {
			synchronized(databases.get(entry.getKey()).getLock()) {
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
