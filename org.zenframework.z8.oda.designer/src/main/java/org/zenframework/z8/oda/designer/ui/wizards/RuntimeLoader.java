package org.zenframework.z8.oda.designer.ui.wizards;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.zenframework.z8.oda.designer.plugin.Plugin;
import org.zenframework.z8.server.engine.Runtime;

public class RuntimeLoader extends URLClassLoader {
	static private Runtime runtime;
	static private ClassLoader classLoader;

	static public Runtime getRuntime() throws Throwable {
		if(runtime == null)
			runtime = new Runtime(getClassLoader(getUrl()));

		return runtime;
	}

	static private ClassLoader getClassLoader(File path) throws Throwable {
		if(classLoader == null)
			classLoader = new RuntimeLoader(new File(path, "lib"), Runtime.class.getClassLoader());

		return classLoader;
	}

	static private File getUrl() {
		File path = Plugin.getWebInfPath();

		if(path.toString().isEmpty())
			throw new RuntimeException("WEB-INF path is not set. See Preferences - Z8 Property Page");

		return path;
	}

	static private URL[] getJars(File dir) throws Throwable {
		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".jar");
			}
		};

		File[] files = dir.listFiles(filter);

		List<URL> urls = new ArrayList<URL>();

		for(File file : files)
			urls.add(file.toURI().toURL());

		return urls.toArray(new URL[0]);
	}

	private RuntimeLoader(File path, ClassLoader parent) throws Throwable {
		super(getJars(path), parent);
	}

}
