package org.zenframework.z8.server.jmx;

import java.util.List;

import javax.management.MXBean;
import javax.management.openmbean.OpenDataException;

import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.engine.IDatabase;

@MXBean
public interface ConnectionManagerMXBean {

	String Name = "org.zenframework.z8.server:type=db,name=ConnectionManager";

	List<Connection.Info> getConnections() throws OpenDataException;
	List<IDatabase.Info> getDatabases() throws OpenDataException;

}
