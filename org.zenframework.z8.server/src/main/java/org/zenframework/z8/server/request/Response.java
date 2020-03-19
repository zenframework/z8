package org.zenframework.z8.server.request;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.zenframework.z8.server.json.JsonWriter;

public class Response implements IResponse {
	private String content = null;
	private InputStream inputStream = null;
	private JsonWriter writer = null;

	@Override
	public String getContent() {
		return content;
	}

	@Override
	public void setContent(String content) {
		this.content = content;
	}

	@Override
	public InputStream getInputStream() {
		if(inputStream == null && content != null)
			inputStream = new ByteArrayInputStream(content.getBytes());
		return inputStream;
	}

	@Override
	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	@Override
	public JsonWriter getWriter() {
		return writer;
	}

	@Override
	public void setWriter(JsonWriter writer) {
		this.writer = writer;
	}
}
