package org.zenframework.z8.server.engine;

import java.rmi.RemoteException;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zenframework.z8.rmi.ObjectIO;
import org.zenframework.z8.server.config.ServerConfig;

public final class ServerMain {

	static {
		ObjectIO.initialize(new RmiIO());
	}

	private static final Log LOG = LogFactory.getLog(ServerMain.class);

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
			if (this == authcenter)
				return ServerConfig.authorityCenter();
			if (this == appserver)
				return ServerConfig.applicationServer();
			if (this == interconnection)
				return ServerConfig.interconnectionCenter();
			if (this == webserver)
				return ServerConfig.webServer();
			throw new RuntimeException("Unknown server type");
		}
	}

	private static final Options Options = getOptions();

	private static final String ServerOpt = "server";
	private static final String ServerClassOpt = "server-class";
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

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		try {
			CommandLineParser parser = new BasicParser();
			CommandLine cmd = parser.parse(Options, args);

			if (!cmd.hasOption(ServerOpt))
				throw new RuntimeException("Server type is not specified");
			final ServerType serverType = ServerType.valueOf(cmd.getOptionValue(ServerOpt));
			if (serverType == null)
				throw new RuntimeException("Incorrect server type: " + cmd.getOptionValue(ServerOpt));

			final Class<? extends IServer> serverClass;
			if (cmd.getOptionValue(ServerClassOpt) != null) {
				serverClass = (Class<? extends IServer>) Class.forName(cmd.getOptionValue(ServerClassOpt));
			} else {
				serverClass = (Class<? extends IServer>) Class.forName(serverType.className);
			}

			ServerConfig config = new ServerConfig(cmd.hasOption(ConfigOpt) ? cmd.getOptionValue(ConfigOpt) : null);

			if (cmd.hasOption(StopOpt)) {
				serverType.getServer().stop();
			} else {
				serverClass.getMethod("launch", ServerConfig.class).invoke(null, config);
				java.lang.Runtime.getRuntime().addShutdownHook(new Thread("Z8-shutdown") {

					@Override
					public void run() {
						try {
							serverType.getServer().stop();
						} catch (RemoteException e) {}
					}

				});
			}
		} catch (Throwable e) {
			LOG.error("Couldn't start server " + args, e);
			System.exit(-1);
		}
	}
	
}
