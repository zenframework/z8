package org.zenframework.z8.server.runtime;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.utils.IOUtils;

public class Runtimes {
	static private final String[] Z8RuntimePaths = { "META-INF/z8.runtime", "META-INF/z8_bl.runtime" };

	static public Set<IRuntime> loadRuntimes(ClassLoader classLoader) {
		Set<IRuntime> runtimes = new HashSet<IRuntime>();
		for(String path : Z8RuntimePaths) {
			try {
				Enumeration<URL> resources = classLoader.getResources(path);
				while(resources.hasMoreElements())
					loadRuntimes(classLoader, resources.nextElement(), runtimes);
			} catch(IOException e) {
				throw new RuntimeException("Can't load " + path + " resources", e);
			}
		}
		return runtimes;
	}

	static public Set<IRuntime> loadRuntimes(ClassLoader classLoader, File folder) {
		Set<IRuntime> runtimes = new HashSet<IRuntime>();
		for(String path : Z8RuntimePaths) {
			try {
				File file = new File(folder, path);
				if(file.exists())
					loadRuntimes(classLoader, file.toURI().toURL(), runtimes);
			} catch(IOException e) {
				throw new RuntimeException("Can't load " + path + " resources", e);
			}
		}
		return runtimes;
	}

	static private void loadRuntimes(ClassLoader classLoader, URL resource, Set<IRuntime> runtimes) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(resource.openStream()));
			String className;
			while((className = reader.readLine()) != null) {
				try {
					runtimes.add((IRuntime) classLoader.loadClass(className.trim()).newInstance());
				} catch (Throwable t) {
					Trace.logError("Can't load runtime-class " + className.trim(), t);
				}
			}
		} catch(IOException e) {
			Trace.logError("Can't load runtime-class from resource " + resource, e);
		} finally {
			IOUtils.closeQuietly(reader);
		}
	}

}
