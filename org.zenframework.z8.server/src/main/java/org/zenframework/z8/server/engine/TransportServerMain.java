package org.zenframework.z8.server.engine;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.logs.Trace;

public final class TransportServerMain {

	private static final Options Options = getOptions();

	private static final String Config = "config";
	private static final String Stop = "stop";

	private static Options getOptions() {
		Options options = new Options();
		options.addOption(Config, true, "path to config file");
		options.addOption(Stop, false, "to stop running server");
		return options;
	}

	public static void main(String[] args) {
		try {
			CommandLineParser parser = new BasicParser();
			CommandLine cmd = parser.parse(Options, args);

			ServerConfig config = new ServerConfig(cmd.hasOption(Config) ? cmd.getOptionValue(Config) : null);

			if(!cmd.hasOption(Stop))
				start(config);
			else
				stop(config);
		} catch(Throwable e) {
			Trace.logError(e);
			System.exit(-1);
		}
	}

	// DO NOT CHANGE method name OR parameters! Used in method.invoke (see Z8
	// project WebApp, class org.zenframework.z8.web.servlet.Servlet)
	public static void start(ServerConfig config) throws RemoteException {
		TransportServer.start(config);
	}

	// DO NOT CHANGE method name OR parameters! Used in method.invoke (see Z8
	// project WebApp, class org.zenframework.z8.web.servlet.Servlet)
	public static void stop(ServerConfig config) throws MalformedURLException, RemoteException, NotBoundException {
		IServer server = Rmi.get(ITransportServer.class);
		server.stop();
	}

}
