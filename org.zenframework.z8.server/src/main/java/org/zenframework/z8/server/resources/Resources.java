package org.zenframework.z8.server.resources;

import org.zenframework.z8.server.config.SystemProperty;
import org.zenframework.z8.server.locale.Locale;
import org.zenframework.z8.server.logs.Trace;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class Resources {
    public static String DefaultLanguage = "ru";

    public static Locale locale = new Locale();

    private static Resources instance;

    private Map<String, Properties> boundles = new ConcurrentHashMap<String, Properties>();

    static {
        instance = new Resources();
    }

    public static String get(String id) {
        return getResources().getString(id);
    }

    public static Resources getResources() {
        return instance;
    }

    private Resources() {
        load(DefaultLanguage);
    }

    private String getString(String key) {
        Properties props = boundles.get(locale.getUserLanguage());

        if(props == null) {
            throw new RuntimeException("No resource boundles found for '" + locale.getUserLanguage() + "'.");
        }

        String value = props.getProperty(key);

        if(value == null) {
            return key; //TODO:throw new ResourceNotFoundException(key);
        }

        return value;
    }

    static public String format(String key, Object... format) {
        MessageFormat form = new MessageFormat(get(key));
        return form.format(format);
    }

    public boolean load(final String language) {
        if(boundles.containsKey(language)) {
            return true;
        }

        String configFileFolder = System.getProperty(SystemProperty.ConfigFilePath);
        File resourcesFolder = new File(configFileFolder != null ? configFileFolder : "", "resources");

        String os_name = System.getProperty("os.name");
        Trace.logEvent("Loading resource boundles for '" + language + "'. System locale: '"
                + java.util.Locale.getDefault().toString() + "'. OS name: '" + os_name + "'.");

        File[] files = resourcesFolder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                String nls = "_" + language + ".nls";
                String xml = "_" + language + ".xml";
                String name = file.getName();
                return file.isFile() && (name.endsWith(nls) || name.endsWith(xml));
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
            Properties properties = new Properties();
            try {
                properties.loadFromXML(new FileInputStream(file));
                boundle.putAll(properties);
            }
            catch(IOException e) {
                Trace.logError(e);
                result = false;
            }
        }

        return result;
    }
}
