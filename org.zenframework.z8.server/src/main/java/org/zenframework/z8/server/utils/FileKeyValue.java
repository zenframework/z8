package org.zenframework.z8.server.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FileKeyValue implements IKeyValue<String, String> {

	private static final Log LOG = LogFactory.getLog(FileKeyValue.class);

	private final Properties store = new Properties();
	private final File file;

	public FileKeyValue(File file) {
		this.file = file;
		if (file.exists()) {
			try {
				store.loadFromXML(new FileInputStream(file));
			} catch (IOException e) {
				LOG.error("Can't load key-value map from '" + file + "'", e);
			}
		}
	}

	@Override
	public void set(String key, String value) {
		String savedUrl = store.getProperty(key);
		if (!value.equals(savedUrl)) {
			synchronized (store) {
				store.setProperty(key, value);
				OutputStream out = null;
				try {
					out = new FileOutputStream(file);
					store.storeToXML(out, "Transport servers registry");
				} catch (IOException e) {
					LOG.error("Can't save key-value '" + key + "' = '" + value + "' to '" + file + "'", e);
				} finally {
					IOUtils.closeQuietly(out);
				}
			}
		}
	}

	@Override
	public String get(String key) {
		return store.getProperty(key);
	}

}
