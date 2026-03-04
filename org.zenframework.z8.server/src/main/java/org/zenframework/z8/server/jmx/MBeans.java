package org.zenframework.z8.server.jmx;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.zenframework.z8.server.jmx.impl.ConnectionManagerImpl;
import org.zenframework.z8.server.logs.Trace;

public class MBeans {

	private static final MBeans Instance = new MBeans();

	private final MBeanServer server;

	private MBeans() {
		server = ManagementFactory.getPlatformMBeanServer();
	}

	public static MBeans getInstance() {
		return Instance;
	}

	public void registerMBeans() {
		register(ConnectionManagerMXBean.Name, new ConnectionManagerImpl(), ConnectionManagerMXBean.class);
	}

	protected <T> void register(String name, T mbean, Class<T> type) {
		try {
			server.registerMBean(mbean, new ObjectName(name));
		} catch (Exception e) {
			Trace.logError("Can't register MBean '" + name +"'", e);
		}
	}
}
