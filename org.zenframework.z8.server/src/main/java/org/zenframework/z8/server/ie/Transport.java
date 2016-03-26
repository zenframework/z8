package org.zenframework.z8.server.ie;

public interface Transport {

	void connect() throws TransportException;

	void close();

	void send(Message bean, String transportAddress) throws TransportException;

	Message receive() throws TransportException;

	void commit() throws TransportException;

	void rollback() throws TransportException;

	String getProtocol();

	String getUrl(String address);

	void init();

	void shutdown();

}
