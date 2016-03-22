package org.zenframework.z8.server.request;

import org.zenframework.z8.server.json.JsonWriter;

public class Response implements IResponse {
    private String content = null;
    private JsonWriter writer = null;

    @Override
    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String getContent() {
        return content;
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
