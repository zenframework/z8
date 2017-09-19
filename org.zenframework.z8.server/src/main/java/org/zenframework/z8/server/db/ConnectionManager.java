package org.zenframework.z8.server.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.engine.Database;
import org.zenframework.z8.server.exceptions.ThreadInterruptedException;

public class ConnectionManager {
	static private Map<String, List<Connection>> schemas = new HashMap<String, List<Connection>>();

	public static synchronized Connection get() {
		return get(ServerConfig.database());
	}

	public static Database database() {
		return get().database();
	}

	public static DatabaseVendor vendor() {
		return database().vendor();
	}

	public static synchronized Connection get(Database database) {
		if(Thread.interrupted())
			throw new ThreadInterruptedException();

		if(database == null)
			database = ServerConfig.database();

		List<Connection> connections = schemas.get(database.schema());

		if(connections == null) {
			connections = new ArrayList<Connection>();
			schemas.put(database.schema(), connections);
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

		Connection connection = Connection.connect(database);
		connections.add(connection);

		connection.use();
		return connection;
	}

	public static synchronized void release() {
		release(null);
	}

	public static synchronized void release(Database database) {
		if(database == null)
			database = ServerConfig.database();

		List<Connection> connections = schemas.get(database.schema());

		if(connections == null)
			return;

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
