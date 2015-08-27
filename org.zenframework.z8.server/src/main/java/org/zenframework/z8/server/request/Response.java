package org.zenframework.z8.server.request;

import org.zenframework.z8.server.json.parser.JsonObject;

public class Response implements IResponse {
    private String content = null;
    private JsonObject writer = null;

    @Override
    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public JsonObject getWriter() {
        return writer;
    }
    
    @Override
    public void setWriter(JsonObject writer) {
        this.writer = writer;
    }
}
