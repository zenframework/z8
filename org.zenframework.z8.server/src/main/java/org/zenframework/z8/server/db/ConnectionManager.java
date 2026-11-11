package org.zenframework.z8.server.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.zenframework.z8.server.db.dialect.DatabaseDialect;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.engine.IDatabase;

public class ConnectionManager {
	static private Map<IDatabase, Collection<Connection>> databaseConnections = new HashMap<IDatabase, Collection<Connection>>();

	public static IDatabase database() {
		return get().database();
	}

	public static DatabaseVendor vendor() {
		return database().vendor();
	}

	public static DatabaseDialect dialect() {
		return database().dialect();
	}

	public static Connection get() {
		return get(ApplicationServer.getDatabase());
	}

	public static Connection get(IDatabase database) {
		Collection<Connection> connections =  null;

		Object lock = database.getLock();

		synchronized (lock) {
			connections = databaseConnections.get(database);

			if (connections == null) {
				connections = new HashSet<Connection>();
				databaseConnections.put(database, connections);
			}

			for (Connection connection : connections) {
				if (connection.isCurrent())
					return connection;
			}

			for (Connection connection : connections) {
				if (!connection.isInUse()) {
					connection.use();
					return connection;
				}
			}
		}

		Connection connection = Connection.connect(database);
		connection.use();

		synchronized (lock) {
			connections.add(connection);
		}

		return connection;
	}

	public static void release() {
		Set<Connection> toRelease = new HashSet<Connection>();
		Set<Connection> toClose = new HashSet<Connection>();

		for (Map.Entry<IDatabase, Collection<Connection>> entry : databaseConnections.entrySet()) {
			synchronized(entry.getKey().getLock()) {
				Iterator<Connection> iterator = entry.getValue().iterator();

				while (iterator.hasNext()) {
					Connection connection = iterator.next();

					if (connection.isCurrent())
						toRelease.add(connection);

					if (connection.isUnused()) {
						toClose.add(connection);
						iterator.remove();
					}
				}
			}
		}

		for (Connection connection : toRelease)
			connection.release();

		for (Connection connection : toClose)
			connection.close();
	}

	public static List<IDatabase.Info> getDatabasesInfo() {
		List<IDatabase.Info> dbInfo = new ArrayList<IDatabase.Info>(10);

		for(IDatabase db : databaseConnections.keySet())
			dbInfo.add(db.getInfo());

		return dbInfo;
	}

	public static List<Connection.Info> getConnectionsInfo() {
		List<Connection.Info> connInfo = new ArrayList<Connection.Info>(100);

		for (Map.Entry<IDatabase, Collection<Connection>> entry : databaseConnections.entrySet()) {
			Set<Connection> connections;

			synchronized (entry.getKey().getLock()) {
				connections = new HashSet<Connection>(entry.getValue());
			}

			for (Connection connection : connections)
				connInfo.add(connection.getInfo());
		}

		return connInfo;
	}
}
