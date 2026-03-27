package org.zenframework.z8.server.jmx;

import java.util.List;

import javax.management.MXBean;
import javax.management.openmbean.OpenDataException;

import org.zenframework.z8.server.ie.rmi.Transport;

@MXBean
public interface TransportMXBean {

	String Name = "org.zenframework.z8.server:type=transport,name=Transport";

	List<Transport.Info> getTransports() throws OpenDataException;

}
