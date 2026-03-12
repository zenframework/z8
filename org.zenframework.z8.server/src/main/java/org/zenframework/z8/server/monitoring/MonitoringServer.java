package org.zenframework.z8.server.monitoring;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.jolokia.config.ConfigKey;
import org.jolokia.config.Configuration;
import org.jolokia.jvmagent.handler.JolokiaHttpHandler;
import org.jolokia.util.NetworkUtil;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.logs.Trace;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.spi.HttpServerProvider;

@SuppressWarnings("restriction")
public class MonitoringServer {

	private static final String ThreadPrefix = "Monitoring-";
	private static final String JolokiaPath = "/jolokia";

	private static class DaemonThreadFactory implements ThreadFactory {

		private int counter = 0;

		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r, ThreadPrefix + ++counter);
			t.setDaemon(true);
			return t;
		}
	}

	private static MonitoringServer Instance;

	public static boolean isEnabled() {
		return ServerConfig.isMonitoringServerEnabled();
	}

	public static MonitoringServer get() {
		if (Instance == null)
			Instance = new MonitoringServer();
		return Instance;
	}

	private HttpServer server;
	private JolokiaHttpHandler jolokiaHandler;

	public void start() {
		if (server != null)
			throw new RuntimeException("Monitoring server started already");

		try {
			server = createHttpServer();

			jolokiaHandler = createJolokiaHandler(hashCode());
			jolokiaHandler.start(false);

			server.createContext(JolokiaPath, jolokiaHandler);
			server.start();

			Trace.logEvent("Monitoring server started on port " + server.getAddress().getPort());
		} catch (IOException e) {
			throw new RuntimeException("Could not start monitoring server", e);
		}
	}

	public void stop() {
		if (server == null)
			return;

		jolokiaHandler.stop();
		server.stop(0);

		jolokiaHandler = null;
		server = null;
	}

	private static HttpServer createHttpServer() throws IOException {
		InetSocketAddress address = new InetSocketAddress(ServerConfig.monitoringServerPort());
		Executor executor = Executors.newFixedThreadPool(ServerConfig.monitoringServerThreads(), new DaemonThreadFactory());

		HttpServer server = getHttpServerPropvider().createHttpServer(address, 0);
		server.setExecutor(executor);

		return server;
	}

	private static JolokiaHttpHandler createJolokiaHandler(int id) {
		Map<String, String> params = new HashMap<String, String>();
		params.put(ConfigKey.AGENT_ID.getKeyValue(), NetworkUtil.getAgentId(id, "jvm"));

		Configuration config = new Configuration();
		config.updateGlobalConfiguration(params);

		return new JolokiaHttpHandler(config);
	}

	private static HttpServerProvider getHttpServerPropvider() {
		String providerClass = ServerConfig.monitoringServerProvider();

		try {
			return (HttpServerProvider) Class.forName(providerClass).newInstance();
		} catch (Exception e) {
			throw new RuntimeException("Could not load HTTP server provider '" + providerClass + "'", e);
		}
	}
}
