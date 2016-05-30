package org.zenframework.z8.server.engine;

import java.lang.reflect.Method;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.zenframework.z8.rmi.ObjectIO;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.logs.Trace;

public final class ServerMain {

	private static enum ServerType {

		authcenter("org.zenframework.z8.server.engine.IAuthorityCenter", "org.zenframework.z8.auth.AuthorityCenter"),
		appserver("org.zenframework.z8.server.engine.IApplicationServer", "org.zenframework.z8.server.engine.ApplicationServer"),
		tservice("org.zenframework.z8.server.engine.ITransportService", "org.zenframework.z8.server.engine.TransportService"),
		tcenter("org.zenframework.z8.server.engine.ITransportCenter", "org.zenframework.z8.server.engine.TransportCenter");

		final String interfaceName;
		final String className;

		ServerType(String interfaceName, String className) {
			this.interfaceName = interfaceName;
			this.className = className;
		}

	}

	private static final Options Options = getOptions();

	private static final String ServerOpt = "server";
	private static final String ConfigOpt = "config";
	private static final String StopOpt = "stop";

	private static Options getOptions() {
		Options options = new Options();
		options.addOption(ServerOpt, true, "server type: authcenter, appserver, transport");
		options.addOption(ConfigOpt, true, "path to config file");
		options.addOption(StopOpt, false, "to stop running server");
		return options;
	}

	public static void main(String[] args) {
		try {
			ObjectIO.initialize(new RmiIO());
			
			CommandLineParser parser = new BasicParser();
			CommandLine cmd = parser.parse(Options, args);

			if (!cmd.hasOption(ServerOpt))
				throw new RuntimeException("Server type is not specified");
			ServerType serverType = ServerType.valueOf(cmd.getOptionValue(ServerOpt));
			if (serverType == null)
				throw new RuntimeException("Incorrect server type: " + cmd.getOptionValue(ServerOpt));
			
			Class<? extends IServer> serverInterface = getClass(serverType.interfaceName);
			Class<? extends IServer> serverClass = getClass(serverType.className);
			
			ServerConfig config = new ServerConfig(cmd.hasOption(ConfigOpt) ? cmd.getOptionValue(ConfigOpt) : null);
			Z8Context.init(config);

			if (!cmd.hasOption(StopOpt)) {
				Method startMethod = serverClass.getMethod("start", ServerConfig.class);
				startMethod.invoke(null, config);
			} else {
				Rmi.get(serverInterface).stop();
			}
		} catch (Throwable e) {
			Trace.logError(e);
			System.exit(-1);
		}
	}

	@SuppressWarnings("unchecked")
	private static Class<? extends IServer> getClass(String name) throws ClassNotFoundException {
		return (Class<? extends IServer>) Class.forName(name);
	}

}
