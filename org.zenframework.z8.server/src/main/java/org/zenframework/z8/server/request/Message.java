package org.zenframework.z8.server.request;

import org.zenframework.z8.server.types.datetime;

public class Message {
    private datetime time;
    private String text;

    public Message(String text) {
        time = new datetime();
        this.text = text;
    }

    public datetime time() {
        return time;
    }

    public String text() {
        return text;
    }

    @Override
    public String toString() {
        return "[" + time.toString() + "] " + text;
    }
}
