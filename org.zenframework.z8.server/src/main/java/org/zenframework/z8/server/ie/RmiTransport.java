package org.zenframework.z8.server.ie;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.zenframework.z8.server.base.file.FileInfo;
import org.zenframework.z8.server.base.table.system.Properties;
import org.zenframework.z8.server.engine.ITransportCenter;
import org.zenframework.z8.server.engine.ITransportService;
import org.zenframework.z8.server.engine.Rmi;
import org.zenframework.z8.server.engine.RmiAddress;
import org.zenframework.z8.server.runtime.ServerRuntime;

public class RmiTransport extends AbstractTransport {

	private String rmiAddress;
	
	public RmiTransport(TransportContext context) {
		super(context);
	}

	public RmiTransport(String sender, String address) {
		super(null);
	
		String transportCenter = Properties.getProperty(ServerRuntime.TransportCenterAddressProperty).trim();
		
		try {
			List<TransportRoute> routes = Rmi.get(ITransportCenter.class, new RmiAddress(transportCenter)).getTransportRoutes(address);
			
			if(routes.size() != 1)
				throw new RuntimeException("bad routes");
			
			rmiAddress = routes.get(0).getAddress();
			
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public static final String PROTOCOL = "rmi";

	@Override
	public void connect() throws TransportException {}

	@Override
	public void close() {}

	public void send(Message message) throws TransportException {
		send(message, rmiAddress);
	}

	public void send(Message message, String transportAddress) throws TransportException {
		try {
			getServer(rmiAddress).sendMessage(message);
		} catch(Throwable e) {
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
