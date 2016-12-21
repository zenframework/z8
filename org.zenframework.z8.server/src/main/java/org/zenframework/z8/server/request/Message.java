package org.zenframework.z8.server.request;

import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.types.date;

public class Message {
	static private int Info = 0;
	static private int Warning = 1;
	static private int Error = 2;

	private date time;
	private String text;
	private String source;
	private int type;

	static public Message info(String text, String source) {
		return new Message(text, source, Info);
	}

	static public Message warning(String text, String source) {
		return new Message(text, source, Warning);
	}

	static public Message error(String text, String source) {
		return new Message(text, source, Error);
	}

	private Message(String text, String source, int type) {
		time = new date();
		this.text = text;
		this.source = source;
		this.type = type;
	}

	public date time() {
		return time;
	}

	public String text() {
		return text;
	}

	public String source() {
		return source;
	}

	public void write(JsonWriter writer) {
		writer.writeProperty(Json.text, text);
		writer.writeProperty(Json.source, source);
		writer.writeProperty(Json.time, time);
		writer.writeProperty(Json.type, type == Info ? Json.info : (type == Warning ? Json.warning : Json.error));
	}

	@Override
	public String toString() {
		return type == Info ? "INFO" : (type == Warning ? "WARNING" : "ERROR") + ": [" + time.toString() + "] " + text;
	}
}
