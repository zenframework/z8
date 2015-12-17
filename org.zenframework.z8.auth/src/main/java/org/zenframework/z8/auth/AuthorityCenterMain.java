package org.zenframework.z8.auth;

import java.io.File;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.StringTokenizer;

import org.zenframework.z8.server.config.AuthCenterConfig;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.config.SystemProperty;
import org.zenframework.z8.server.engine.IAuthorityCenter;
import org.zenframework.z8.server.engine.IServer;
import org.zenframework.z8.server.engine.Rmi;
import org.zenframework.z8.server.logs.Trace;

public final class AuthorityCenterMain
{
	public static void main(String[] args) {
		boolean bStart = false, bStop = false;
		for (String anArgc : args) {
			if (anArgc.equalsIgnoreCase("start")) {
				bStart = true;
			}
			if (anArgc.equalsIgnoreCase("stop")) {
				bStop = true;
			}
			StringTokenizer strToken = new StringTokenizer(anArgc, "=");
			String key = strToken.nextToken();
			String value = "";
			if (strToken.countTokens() > 0)
				value = strToken.nextToken();
			if (key.equalsIgnoreCase("settings")) {
				System.setProperty(SystemProperty.ConfigFilePath, new File(value).getAbsolutePath());
			}
		}
		if (bStart) {
			try {
				start(new AuthCenterConfig());
			} catch (Throwable e) {
				try {
					Trace.logError(e);
				} catch (Throwable ex) {
				}
				try {
					stop(new AuthCenterConfig());
				} catch (Throwable ex) {
				}
				System.exit(-1);
			}
		}
		if (bStop) {
			try {
				stop(new AuthCenterConfig());
			} catch (Throwable e) {
				try {
					Trace.logError(e);
				} catch (Throwable ex) {
				}
				System.exit(-1);
			}
		}
	}

	// DO NOT CHANGE method name OR parameters! Used in method.invoke (see Z8
	// project WebApp, class org.zenframework.z8.web.servlet.Servlet)
	public static void start(ServerConfig config) throws RemoteException {
		Rmi.authorityCenter = new AuthorityCenter(config);
	}

	// DO NOT CHANGE method name OR parameters! Used in method.invoke (see Z8
	// project WebApp, class org.zenframework.z8.web.servlet.Servlet)
	public static void stop(ServerConfig config) throws MalformedURLException,
	        RemoteException, NotBoundException {
        
	    IServer server = Rmi.connect(Rmi.localhost, config.getAuthorityCenterPort(), IAuthorityCenter.Name);
        server.stop();

        Rmi.authorityCenter = null;
	}
}
