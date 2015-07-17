package org.zenframework.z8.server.ie;

import java.util.LinkedList;
import java.util.List;

public class NullTransport extends AbstractTransport {

    public static final String PROTOCOL = "null";
    
    private final List<Message> messages = new LinkedList<Message>();

    @Override
    public void connect(TransportContext context) throws TransportException {}

    @Override
    public void close() {}

    @Override
    public synchronized void send(Message message) {
        messages.add(message);
    }

    @Override
    public synchronized Message receive() {
        return messages.isEmpty() ? null : messages.remove(0);
    }

    @Override
    public void commit() {}

    @Override
    public void rollback() throws TransportException {}

    @Override
    public String getProtocol() {
        return PROTOCOL;
    }

    @Override
    public boolean usePersistency() {
        return false;
    }

}
