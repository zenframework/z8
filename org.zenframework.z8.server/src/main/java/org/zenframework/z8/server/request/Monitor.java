package org.zenframework.z8.server.request;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.utils.ErrorUtils;

public class Monitor extends RequestTarget implements IMonitor {

	private file outputFile;
	private file logFile;

	private List<Message> logMessages = new ArrayList<Message>();

	private List<Message> messages = new ArrayList<Message>();

	public Monitor() {
		super(guid.create().toString());
	}

	public Monitor(String id) {
		super(id);
	}

	protected void collectLogMessages() {
		logMessages.addAll(messages);
	}

	@Override
	public void info(String text) {
		Trace.logEvent(text);
		messages.add(Message.info(text, request().displayName()));
	}

	@Override
	public void warning(String text) {
		Trace.logEvent(text);
		messages.add(Message.warning(text, request().displayName()));
	}

	@Override
	public void error(String text) {
		Trace.logEvent(text);
		messages.add(Message.error(text, request().displayName()));
	}

	@Override
	public void print(file file) {
		this.outputFile = file;
	}

	@Override
	public file getLog() {
		return logFile;
	}

	@Override
	public void logInfo(String text) {
		log(Message.info(text, request().displayName()));
	}

	@Override
	public void logWarning(String text) {
		log(Message.warning(text, request().displayName()));
	}

	@Override
	public void logError(String text) {
		log(Message.error(text, request().displayName()));
	}

	private void log(Message message) {
		if(logFile == null)
			logFile = new file();

		logFile.write(message + file.EOL);
	}

	@Override
	public void log(Throwable exception) {
		String message = ErrorUtils.getMessage(exception) + "\r\n" + ErrorUtils.getStackTrace(exception);
		logError(message);
	}

	public Collection<Message> getMessages() {
		return new ArrayList<Message>(messages);
	}

	protected void clearMessages() {
		messages.clear();
	}

	@Override
	public void writeResponse(JsonWriter writer) {
		if(outputFile != null)
			writer.writeProperty(Json.source, outputFile.path.get().replace('\\', '/'));

		writer.writeProperty(new string(Json.server), ApplicationServer.id);
		writer.writeInfo(getMessages(), ApplicationServer.id, logFile);
	}
}
