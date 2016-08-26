package org.zenframework.z8.server.request;

import org.zenframework.z8.server.types.date;

public class Message {
    private date time;
    private String text;

    public Message(String text) {
        time = new date();
        this.text = text;
    }

    public date time() {
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
