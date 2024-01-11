package org.zenframework.z8.server.engine;

import java.rmi.RemoteException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.zenframework.z8.rmi.ObjectIO;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.logs.Trace;

public final class ServerMain {

	static {
		ObjectIO.initialize(new RmiIO());
	}

	private static enum ServerType {
		authcenter("org.zenframework.z8.auth.AuthorityCenter"),
		appserver("org.zenframework.z8.server.engine.ApplicationServer"),
		interconnection("org.zenframework.z8.interconnection.InterconnectionCenter"),
		webserver("org.zenframework.z8.webserver.WebServer");

		final String className;

		ServerType(String className) {
			this.className = className;
		}

		IServer getServer() {
			if(this == authcenter)
				return ServerConfig.authorityCenter();
			if(this == appserver)
				return ServerConfig.applicationServer();
			if(this == interconnection)
				return ServerConfig.interconnectionCenter();
			if(this == webserver)
				return ServerConfig.webServer();
			throw new RuntimeException("Unknown server type");
		}
	}

	private static final Options Options = getOptions();

	private static final String ServerOpt = "server";
	private static final String ServerClassOpt = "server_class";
	private static final String ConfigOpt = "config";
	private static final String StopOpt = "stop";

	private static Options getOptions() {
		Options options = new Options();
		options.addOption(ServerOpt, true, "server type: authcenter, appserver, interconnection");
		options.addOption(ServerClassOpt, true, "server class");
		options.addOption(ConfigOpt, true, "path to config file");
		options.addOption(StopOpt, false, "to stop running server");
		return options;
	}

	public static void main(String[] args) {
		try {
			CommandLineParser parser = new DefaultParser();
			CommandLine cmd = parser.parse(Options, args);

			ServerConfig.load(cmd.hasOption(ConfigOpt) ? cmd.getOptionValue(ConfigOpt) : null);

			IServer server = getServer(cmd, !cmd.hasOption(StopOpt));

			if(cmd.hasOption(StopOpt)) {
				server.stop();
				return;
			}

			server.start();

			java.lang.Runtime.getRuntime().addShutdownHook(new Thread("Z8-shutdown") {
				@Override
				public void run() {
					try {
						server.stop();
					} catch(RemoteException e) {}
				}
			});
		} catch(Throwable e) {
			Trace.logError("Couldn't start server " + args, e);
			System.exit(-1);
		}
	}

	@SuppressWarnings("unchecked")
	private static IServer getServer(CommandLine cmd, boolean newInstance) throws Exception {
		ServerType serverType = null;

		if (cmd.hasOption(ServerOpt)) {
			serverType = ServerType.valueOf(cmd.getOptionValue(ServerOpt));

			if (serverType == null)
				throw new RuntimeException("Incorrect server type: " + cmd.getOptionValue(ServerOpt));
			else if (!newInstance)
				return serverType.getServer();
		}

		String className = null;

		if (cmd.hasOption(ServerClassOpt))
			className = cmd.getOptionValue(ServerClassOpt);
		else if (serverType != null)
			className = serverType.className;
		else
			throw new RuntimeException("Server type is not specified");

		Class<? extends IServer> serverClass = (Class<? extends IServer>)Class.forName(className);
		return serverClass.getConstructor().newInstance();
	}
}
