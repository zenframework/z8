package org.zenframework.z8.server.request;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.zenframework.z8.server.json.JsonWriter;

public class Response implements IResponse {
	private String content = null;
	private ContentType contentType = ContentType.Json;

	private InputStream inputStream = null;
	private JsonWriter writer = null;

	@Override
	public ContentType getContentType() {
		return contentType;
	}

	@Override
	public void setContentType(ContentType contentType) {
		this.contentType = contentType;
	}

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
		if(inputStream != null)
			return inputStream;
		return new ByteArrayInputStream(content != null ? content.getBytes() : new byte[0]);
	}

	@Override
	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
		this.contentType = ContentType.Binary;
	}

	@Override
	public JsonWriter getWriter() {
		return writer;
	}

	@Override
	public void setWriter(JsonWriter writer) {
		this.writer = writer;
		this.contentType = ContentType.Json;
	}
}
