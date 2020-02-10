package org.zenframework.z8.app.runtime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;

import org.zenframework.z8.server.db.generator.DBGenerator;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.request.Loader;
import org.zenframework.z8.server.runtime.AbstractRuntime;

public class ApplicationRuntime extends AbstractRuntime {

	private static final String RUNTIME_LISTENERS = "META-INF/z8/runtime.listener";

	public ApplicationRuntime() {
		try {
			Enumeration<URL> resources = getClass().getClassLoader().getResources(RUNTIME_LISTENERS);
			while (resources.hasMoreElements()) {
				BufferedReader in = new BufferedReader(new InputStreamReader(resources.nextElement().openStream()));
				for (String line = in.readLine(); line != null; line = in.readLine()) {
					DBGenerator.addListener((DBGenerator.Listener) Loader.getInstance(line.trim()));
					Trace.log().info("Runtime listener added: " + line);
				}
			}
		} catch (IOException e) {
			Trace.log().error("Couldn't read resources '" + RUNTIME_LISTENERS + "'", e);
		}
	}

}
