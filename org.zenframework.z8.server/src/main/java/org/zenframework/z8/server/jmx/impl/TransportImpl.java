package org.zenframework.z8.server.jmx.impl;

import java.util.List;

import javax.management.openmbean.OpenDataException;

import org.zenframework.z8.server.ie.rmi.Transport;
import org.zenframework.z8.server.jmx.TransportMXBean;

public class TransportImpl implements TransportMXBean {

	@Override
	public List<Transport.Info> getTransports() throws OpenDataException {
		return Transport.getTransportsInfo();
	}

}
