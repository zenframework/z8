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
	private int type;

	static public Message info(String text) {
		return new Message(text, Info);
	}

	static public Message warning(String text) {
		return new Message(text, Warning);
	}

	static public Message error(String text) {
		return new Message(text, Error);
	}

	private Message(String text, int type) {
		time = new date();
		this.text = text;
		this.type = type;
	}

	public date time() {
		return time;
	}

	public String text() {
		return text;
	}

	public void write(JsonWriter writer) {
		writer.writeProperty(Json.text, text);
		writer.writeProperty(Json.time, time);
		writer.writeProperty(Json.type, type == Info ? Json.info : (type == Warning ? Json.warning : Json.error));
	}

	@Override
	public String toString() {
		return type == Info ? "INFO" : (type == Warning ? "WARNING" : "ERROR") + ": [" + time.toString() + "] " + text;
	}
}
