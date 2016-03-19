package org.zenframework.z8.server.ie;

import java.io.Closeable;

public interface Transport extends Closeable {

    void connect() throws TransportException;

    void close();

    void send(Message bean) throws TransportException;

    Message receive() throws TransportException;

    void commit() throws TransportException;

    void rollback() throws TransportException;

    String getProtocol();

    String getUrl(String address);

    void init();

    void shutdown();

}
