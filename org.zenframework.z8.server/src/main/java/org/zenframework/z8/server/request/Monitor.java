package org.zenframework.z8.server.request;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.types.exception;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.string;

public class Monitor extends RequestTarget implements IMonitor {

	private Collection<file> files = new ArrayList<file>();
	private file log;
	private boolean hasErrors = false;

	private List<Message> messages = new ArrayList<Message>();

	public Monitor() {
		super(guid.create().toString());
	}

	public Monitor(String id) {
		super(id);
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
		error(new exception(text));
	}

	@Override
	public void error(Throwable exception) {
		Trace.logError(exception);
		messages.add(Message.error(exception, request().displayName()));
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
		logError(new exception(text));
	}

	@Override
	public void logError(Throwable exception) {
		hasErrors = true;
		log(Message.error(exception, request().displayName()));
	}

	protected void log(Message message) {
		Trace.logEvent(message);

		if(log == null)
			log = file.createLogFile(request().displayName(), "log");

		log.write(message.toLogString() + file.EOL, true);
	}

	@Override
	public Collection<file> getFiles() {
		return files;
	}

	@Override
	public boolean hasErrors() {
		return hasErrors;
	}

	@Override
	public file getLog() {
		return log;
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
