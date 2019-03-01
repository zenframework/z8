package org.zenframework.z8.server.request;

import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.utils.ErrorUtils;

public class Message {
	static private int Info = 0;
	static private int Warning = 1;
	static private int Error = 2;
/*	static private int FatalError = 3;*/

	private date time;
	private Object textOrException;
	private String source;
	private int type;

	static public Message info(String text, String source) {
		return new Message(text, source, Info);
	}

	static public Message warning(String text, String source) {
		return new Message(text, source, Warning);
	}

	static public Message error(Throwable exception, String source) {
		return new Message(exception, source, Error);
	}

	private Message(Object textOrException, String source, int type) {
		time = new date();
		this.textOrException = textOrException;
		this.source = source;
		this.type = type;
	}

	public date time() {
		return time;
	}

	public String text() {
		return textOrException instanceof String ? (String)textOrException : ErrorUtils.getMessage((Throwable)textOrException);
	}

	public Throwable exception() {
		return textOrException instanceof Throwable ? (Throwable)textOrException : null;
	}

	public String source() {
		return source;
	}

	public void write(JsonWriter writer) {
		writer.writeProperty(Json.text, text());
		writer.writeProperty(Json.source, source());
		writer.writeProperty(Json.time, time());
		writer.writeProperty(Json.type, type == Info ? Json.info : (type == Warning ? Json.warning : Json.error));
	}

	@Override
	public String toString() {
		return (type == Info ? "INFO" : (type == Warning ? "WARNING" : "ERROR")) + ": [" + time.toString() + "] " + text();
	}

	public String toLogString() {
		return toString() + (type == Error ? file.EOL + ErrorUtils.getStackTrace(exception()) : "");
	}
}
