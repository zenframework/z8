package org.zenframework.z8.server.engine;

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
import sun.net.httpserver.DefaultHttpServerProvider;

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

	private HttpServer server;
	private JolokiaHttpHandler jolokiaHandler;

	public MonitoringServer() {
	}

	public void start() {
		if (server != null)
			stop();

		try {
			server = createHttpServer();

			jolokiaHandler = createJolokiaHandler();
			jolokiaHandler.start(false);

			server.createContext(JolokiaPath, jolokiaHandler);
			server.start();

			Trace.logEvent("Monitoring server started on port " + server.getAddress().getPort());
		} catch (IOException e) {
			Trace.logError("Could not start monitoring server", e);
		}
	}

	public void stop() {
		if (server != null) {
			jolokiaHandler.stop();
			server.stop(0);
		}

		jolokiaHandler = null;
		server = null;
	}

	private static HttpServer createHttpServer() throws IOException {
		InetSocketAddress address = new InetSocketAddress(ServerConfig.monitoringServerPort());
		Executor executor = Executors.newFixedThreadPool(ServerConfig.monitoringServerThreads(), new DaemonThreadFactory());

		// HttpServer.create(address, 0) creates Jetty server
		HttpServer server = new DefaultHttpServerProvider().createHttpServer(address, 0);
		server.setExecutor(executor);

		return server;
	}

	private JolokiaHttpHandler createJolokiaHandler() {
		Map<String, String> params = new HashMap<String, String>();
		params.put(ConfigKey.AGENT_ID.getKeyValue(), NetworkUtil.getAgentId(hashCode(), "jvm"));

		Configuration config = new Configuration();
		config.updateGlobalConfiguration(params);

		return new JolokiaHttpHandler(config);
	}
}
