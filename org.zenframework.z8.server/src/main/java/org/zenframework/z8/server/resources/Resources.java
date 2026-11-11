package org.zenframework.z8.server.resources;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.zenframework.z8.server.base.file.Folders;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.resources.NlsHandler.Filter;

public class Resources {
	private static Resources instance;

	private Map<String, Properties> serverBundles = new ConcurrentHashMap<String, Properties>();
	private Map<String, Properties> clientBundles = new ConcurrentHashMap<String, Properties>();

	static {
		instance = new Resources();
	}

	public static String get(String id) {
		return getResources().getString(id);
	}

	public static String getByKey(String id) {
		if(id.length() < 2 || id.charAt(0) != '$' || id.charAt(id.length() - 1) != '$')
			return id;

		return get(id.substring(1, id.length() - 1));
	}

	public static Resources getResources() {
		return instance;
	}

	private Resources() {
		load(ServerConfig.language());
	}

	private String getString(String key) {
		Properties properties = serverBundles.get(ServerConfig.language());

		if(properties == null)
			return key;

		String value = properties.getProperty(key);
		return value != null ? value : key;
	}

	static public String format(String key, Object... format) {
		return MessageFormat.format(get(key), format);
	}

	static public Properties getCliendBundle(String language) {
		return getResources().clientBundles.get(language);
	}

	public boolean load(final String language) {
		if(serverBundles .containsKey(language))
			return true;

		String os_name = System.getProperty("os.name");
		Trace.logEvent("Loading resource boundles for '" + language + "'. System locale: '" + java.util.Locale.getDefault().toString() + "'. OS name: '" + os_name + "'.");

		final String nls = "_" + language + ".nls";
		final String xml = "_" + language + ".xml";

		File[] files = Folders.Resources.listFiles(new FileFilter() {
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

		Properties serverBundle = new Properties();
		serverBundles.put(language, serverBundle);

		Properties clientBundle = new Properties();
		clientBundles.put(language, clientBundle);

		Map<Filter, Properties> targets = new LinkedHashMap<>();
		targets.put(NlsHandler.newAttributeFilter(Filter.SERVER, Filter.TRUE), serverBundle);
		targets.put(NlsHandler.newAttributeFilter(Filter.CLIENT, Filter.TRUE), clientBundle);

		SAXParserFactory factory = SAXParserFactory.newInstance();

		for(File file : files) {
			try {
				SAXParser parser = factory.newSAXParser();
				parser.parse(new FileInputStream(file), new NlsHandler(targets));
			} catch(Exception e) {
				Trace.logError(e);
				result = false;
			}
		}

		return result;
	}
}
