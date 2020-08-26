package org.zenframework.z8.server.resources;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.zenframework.z8.server.base.file.Folders;
import org.zenframework.z8.server.logs.Trace;

public class Resources {
	public static String DefaultLanguage = "ru";

	private static Resources instance;

	private Map<String, Properties> boundles = new ConcurrentHashMap<String, Properties>();

	static {
		instance = new Resources();
	}

	public static String get(String id) {
		String value = getResources().getString(id);
		return value == null ? id : value;
	}

	public static String getOrNull(String id) {
		 return getResources().getString(id);
	}

	public static Resources getResources() {
		return instance;
	}

	private Resources() {
		load(DefaultLanguage);
	}

	private String getString(String key) {
		Properties properties = boundles.get(DefaultLanguage);

		if(properties == null)
			return null;

		String value = properties.getProperty(key);
		return value;
	}

	static public String format(String key, Object... format) {
		return MessageFormat.format(get(key), format);
	}

	public boolean load(final String language) {
		if(boundles.containsKey(language))
			return true;

		File resourcesFolder = new File(Folders.Base, "resources");

		String os_name = System.getProperty("os.name");
		Trace.logEvent("Loading resource boundles for '" + language + "'. System locale: '" + java.util.Locale.getDefault().toString() + "'. OS name: '" + os_name + "'.");

		final String nls = "_" + language + ".nls";
		final String xml = "_" + language + ".xml";

		File[] files = resourcesFolder.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				if(!file.isFile())
					return false;
				String name = file.getName();
				return name.endsWith(xml) || name.endsWith(nls);
			}
		});

		if(files == null || files.length == 0) {
			Trace.logEvent("No resource boundles found for '" + language + "'.");
			return false;
		}

		boolean result = true;

		Properties boundle = new Properties();
		boundles.put(language, boundle);

		for(File file : files) {
			try {
				boundle.loadFromXML(new FileInputStream(file));
			} catch(IOException e) {
				Trace.logError(e);
				result = false;
			}
		}

		return result;
	}
}
