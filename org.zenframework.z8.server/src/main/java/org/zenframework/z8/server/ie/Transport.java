package org.zenframework.z8.server.ie;

public interface Transport {

    void connect(TransportContext context) throws TransportException;
    
    void close();
    
    void send(Message bean) throws TransportException;
    
    Message receive() throws TransportException;

    void commit() throws TransportException;

    void rollback() throws TransportException;

    String getProtocol();
    
    String getUrl(String address);
    
    boolean usePersistency();
    
    void init();
    
    void shutdown();

}
