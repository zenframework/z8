package org.zenframework.z8.server.base.application;

import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.string;

public class ServerConfig {

	static public string z8_get(string key) {
		return new string(org.zenframework.z8.server.config.ServerConfig.get(key.get()));
	}

	static public string z8_get(string key, string defaultValue) {
		return new string(org.zenframework.z8.server.config.ServerConfig.get(key.get()));
	}

	static public bool z8_get(string key, bool defaultValue) {
		return new bool(org.zenframework.z8.server.config.ServerConfig.get(key.get(), defaultValue.get()));
	}

	static public integer z8_get(string key, integer defaultValue) {
		return new integer(org.zenframework.z8.server.config.ServerConfig.get(key.get(), defaultValue.getInt()));
	}

}
