package org.zenframework.z8.server.base.application;

import java.io.File;

import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.string;

public class Resources {

	public static file loadResource(String path) {
		return new file(new File(ServerConfig.applicationPath(), path));
	}

	public static file z8_loadResource(string path) {
		return loadResource(path.get());
	}

}
