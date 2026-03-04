package org.zenframework.z8.server.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.engine.IDatabase;

public class ConnectionManager {
	static private Map<IDatabase, List<Connection>> databaseConnections = new HashMap<IDatabase, List<Connection>>();

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
		for(Map.Entry<IDatabase, List<Connection>> entry : databaseConnections.entrySet()) {
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

	public static List<IDatabase.Info> getDatabasesInfo() {
		List<IDatabase.Info> dbInfo = new ArrayList<IDatabase.Info>(10);

		for(IDatabase db : databaseConnections.keySet())
			dbInfo.add(db.getInfo());

		return dbInfo;
	}

	public static List<Connection.Info> getConnectionsInfo() {
		List<Connection.Info> connInfo = new ArrayList<Connection.Info>(100);

		for(Map.Entry<IDatabase, List<Connection>> entry : databaseConnections.entrySet()) {
			List<Connection> connections;

			synchronized(entry.getKey().getLock()) {
				connections = new ArrayList<Connection>(entry.getValue());
			}

			for(Connection connection : connections)
				connInfo.add(connection.getInfo());
		}

		return connInfo;
	}
}
