package org.zenframework.z8.server.request;

import java.io.InputStream;

import org.zenframework.z8.server.json.JsonWriter;

public interface IResponse {
	public ContentType getContentType();
	public void setContentType(ContentType contentType);

	public String getContent();
	public void setContent(String content);

	public InputStream getInputStream();
	public void setInputStream(InputStream content);

	public JsonWriter getWriter();
	public void setWriter(JsonWriter writer);
}
