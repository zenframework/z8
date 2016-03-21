package org.zenframework.z8.server.request;

import org.zenframework.z8.server.json.JsonWriter;

public interface IResponse {
    public String getContent();
    public void setContent(String content);

    public JsonWriter getWriter();
    public void setWriter(JsonWriter writer);
}
