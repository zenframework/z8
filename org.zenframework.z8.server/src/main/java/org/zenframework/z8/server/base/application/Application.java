package org.zenframework.z8.server.base.application;

import org.zenframework.z8.server.base.json.JsonWriter;
import org.zenframework.z8.server.base.security.User;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.converter.FileConverter;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.request.IMonitor;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.runtime.RLinkedHashMap;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.utils.NumericUtils;

public class Application {
	static public bool z8_isSystemInstalled() {
		return new bool(ServerConfig.database().isSystemInstalled());
	}
	
	static public User.CLASS<? extends User> z8_user() {
		User.CLASS<User> cls = new User.CLASS<User>(null);
		User user = cls.get();
		user.initialize(ApplicationServer.getUser());
		return cls;
	}

	static public integer z8_currentTimeMillis() {
		return new integer(System.currentTimeMillis());
	}

	static public void z8_logInfo(string text) {
		ApplicationServer.getMonitor().logInfo(text.get());
	}

	static public void z8_logWarning(string text) {
		ApplicationServer.getMonitor().logWarning(text.get());
	}

	static public void z8_logError(string text) {
		ApplicationServer.getMonitor().logError(text.get());
	}

	static public void z8_info(string text) {
		IMonitor monitor = ApplicationServer.getMonitor();
		if(monitor != null)
			monitor.info(text.get());
	}

	static public void z8_warning(string text) {
		IMonitor monitor = ApplicationServer.getMonitor();
		if(monitor != null)
			monitor.warning(text.get());
	}

	static public void z8_error(string text) {
		IMonitor monitor = ApplicationServer.getMonitor();
		if(monitor != null)
			monitor.error(text.get());
	}

	static public void z8_print(file file) {
		ApplicationServer.getMonitor().print(file);
	}

	static public RLinkedHashMap<string, string> z8_requestParameters() {
		return (RLinkedHashMap<string, string>)ApplicationServer.getRequest().getParameters();
	}

	static public JsonWriter.CLASS<? extends JsonWriter> z8_responseWriter() {
		JsonWriter.CLASS<JsonWriter> writer = new JsonWriter.CLASS<JsonWriter>(null);
		writer.get().set(ApplicationServer.getRequest().getResponse().getWriter());
		return writer;
	}

	static public integer z8_maxDownloadSize() {
		return new integer(ServerConfig.webClientDownloadMax() * NumericUtils.Megabyte);
	}

	static public integer z8_maxUploadSize() {
		return new integer(ServerConfig.webServerUploadMax() * NumericUtils.Megabyte);
	}

	static public RCollection<string> z8_supportedFileTypes() {
		RCollection<string> result = new RCollection<string>();

		result.add(new string(FileConverter.PDF_EXTENSION));
		result.addAll(string.wrap(ServerConfig.textExtensions()));
		result.addAll(string.wrap(ServerConfig.imageExtensions()));
		result.addAll(string.wrap(ServerConfig.emailExtensions()));
		result.addAll(string.wrap(ServerConfig.officeExtensions()));

		return result;
	}
}
