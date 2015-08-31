package org.zenframework.z8.server.request;

import org.zenframework.z8.server.json.parser.JsonObject;

public interface IResponse {
    public String getContent();
    public void setContent(String content);

    public JsonObject getWriter();
    public void setWriter(JsonObject writer);
}
