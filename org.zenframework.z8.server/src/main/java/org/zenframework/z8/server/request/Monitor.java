package org.zenframework.z8.server.request;

import java.util.ArrayList;
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
	private List<Message> monitorMessages = new ArrayList<Message>();

	public Monitor() {
		super(guid.create().toString());
	}

	public Monitor(String id) {
		super(id);
	}

	protected void collectLogMessages() {
		logMessages.addAll(monitorMessages);
	}

	@Override
	public void print(String text) {
		Trace.logEvent(text);
		monitorMessages.add(new Message(text));
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
	public void log(String text) {
		if (logFile == null)
			logFile = new file();

		logFile.write(new Message(text) + file.EOL);
	}

	@Override
	public void log(Throwable exception) {
		String message = ErrorUtils.getMessage(exception) + "\r\n" + ErrorUtils.getStackTrace(exception);
		log(message);
	}

	public String[] getMessages() {
		String[] result = new String[monitorMessages.size()];

		for (int i = 0; i < monitorMessages.size(); i++) {
			result[i] = monitorMessages.get(i).text();
		}

		return result;
	}

	protected void clearMessages() {
		monitorMessages.clear();
	}

	@Override
	public void writeResponse(JsonWriter writer) {
		if (outputFile != null)
			writer.writeProperty(Json.source, outputFile.path.get().replace('\\', '/'));

		writer.writeProperty(new string(Json.serverId), ApplicationServer.id);
		writer.writeInfo(getMessages(), ApplicationServer.id, logFile);
	}

}
