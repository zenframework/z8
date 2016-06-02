package org.zenframework.z8.server.ie;

import org.zenframework.z8.server.base.file.FileInfo;

public interface Transport {

	void connect() throws TransportException;

	void close();

	void send(Message message, String transportAddress) throws TransportException;

	Message receive() throws TransportException;

	void commit() throws TransportException;

	void rollback() throws TransportException;

	boolean isSynchronousRequestSupported();

	FileInfo readFileSynchronously(FileInfo fileInfo, String transportAddress) throws TransportException;

	String getProtocol();

	String getUrl(String address);

	void init();

	void shutdown();

}
