package org.zenframework.z8.server.ie;

import java.io.IOException;
import java.net.URISyntaxException;

import org.zenframework.z8.server.base.file.FileInfo;
import org.zenframework.z8.server.engine.ITransportService;
import org.zenframework.z8.server.engine.Rmi;
import org.zenframework.z8.server.engine.RmiAddress;

public class RmiTransport extends AbstractTransport {

	public RmiTransport(TransportContext context) {
		super(context);
	}

	public static final String PROTOCOL = "rmi";

	@Override
	public void connect() throws TransportException {}

	@Override
	public void close() {}

	@Override
	public void send(Message message, String transportAddress) throws TransportException, ImportException {
		try {
			getServer(transportAddress).sendMessage(message);
		} catch (ImportException e) {
			throw e;
		} catch (Throwable e) {
			throw new TransportException(e);
		}
	}

	@Override
	public Message receive() throws TransportException {
		return null;
	}

	@Override
	public void commit() throws TransportException {}

	@Override
	public void rollback() {}

	@Override
	public boolean isSynchronousRequestSupported() {
		return true;
	}

	@Override
	public FileInfo readFileSynchronously(FileInfo fileInfo, String transportAddress) throws TransportException {
		try {
			return getServer(transportAddress).readFile(fileInfo);
		} catch (Exception e) {
			throw new TransportException("Can't read remote file " + fileInfo + " synchronously", e);
		}
	}

	@Override
	public String getProtocol() {
		return PROTOCOL;
	}

	private static ITransportService getServer(String transportAddress) throws IOException, URISyntaxException {
		RmiAddress address = new RmiAddress(transportAddress);
		return (ITransportService) Rmi.get(ITransportService.class, address);
	}

}
