package org.zenframework.z8.server.engine;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
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
		interconnection("org.zenframework.z8.interconnection.InterconnectionCenter");

		final String className;

		ServerType(String className) {
			this.className = className;
		}

	}

	private static final Options Options = getOptions();

	private static final String ServerOpt = "server";
	private static final String ConfigOpt = "config";
	private static final String StopOpt = "stop";

	private static Options getOptions() {
		Options options = new Options();
		options.addOption(ServerOpt, true, "server type: authcenter, appserver, interconnection");
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
			ServerType server = ServerType.valueOf(cmd.getOptionValue(ServerOpt));
			if (server == null)
				throw new RuntimeException("Incorrect server type: " + cmd.getOptionValue(ServerOpt));
			
			Class<? extends IServer> serverClass = (Class<? extends IServer>) Class.forName(server.className);
			
			ServerConfig config = new ServerConfig(cmd.hasOption(ConfigOpt) ? cmd.getOptionValue(ConfigOpt) : null);

			if (!cmd.hasOption(StopOpt))
				serverClass.getMethod("launch", ServerConfig.class).invoke(null, config);
			else
				Rmi.get(RmiServer.serverName(serverClass)).stop();
		} catch (Throwable e) {
			Trace.logError(e);
			System.exit(-1);
		}
	}
}
