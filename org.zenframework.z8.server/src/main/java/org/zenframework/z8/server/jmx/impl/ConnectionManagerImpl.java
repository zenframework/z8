package org.zenframework.z8.server.jmx.impl;

import java.util.List;

import javax.management.openmbean.OpenDataException;

import org.zenframework.z8.server.db.Connection.Info;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.jmx.ConnectionManagerMXBean;

public class ConnectionManagerImpl implements ConnectionManagerMXBean {

	@Override
	public List<Info> getConnections() throws OpenDataException {
		return ConnectionManager.getConnectionsInfo();
	}

	@Override
	public List<org.zenframework.z8.server.engine.IDatabase.Info> getDatabases() throws OpenDataException {
		return ConnectionManager.getDatabasesInfo();
	}

}
