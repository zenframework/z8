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

	private Collection<file> files = new ArrayList<file>();
	private file log;

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
	public void fatalError(String text) {
		Trace.logEvent(text);
		messages.add(Message.fatalError(text, request().displayName()));
	}

	@Override
	public void print(file file) {
		files.add(file);
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

	@Override
	public void logFatalError(String text) {
		log(Message.fatalError(text, request().displayName()));
	}

	protected void log(Message message) {
		if(log == null) {
			log = file.createTempFile("log");
			files.add(log);
		}

		log.write(message + file.EOL);
	}

	@Override
	public void log(Throwable exception) {
		String message = ErrorUtils.getMessage(exception) + "\r\n" + ErrorUtils.getStackTrace(exception);
		logError(message);
	}

	public Collection<file> getFiles() {
		return files;
	}

	public Collection<Message> getMessages() {
		return new ArrayList<Message>(messages);
	}

	protected void clearMessages() {
		messages.clear();
	}

	@Override
	public void writeResponse(JsonWriter writer) {
		writer.writeProperty(new string(Json.server), ApplicationServer.id);
		writer.writeInfo(getMessages(), getFiles(), ApplicationServer.id);
	}
}
