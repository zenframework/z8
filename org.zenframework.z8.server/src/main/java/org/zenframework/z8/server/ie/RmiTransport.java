package org.zenframework.z8.server.ie;

import java.util.List;

import org.zenframework.z8.server.base.file.FileInfo;
import org.zenframework.z8.server.base.table.system.Files;
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
	public void send(Message message, String transportAddress) throws TransportException {
		RmiAddress address = new RmiAddress(transportAddress);
		try {
			List<FileInfo> fileInfos = IeUtil.filesToFileInfos(message.getExportEntry().getFiles().getFile());
			for (FileInfo fileInfo : fileInfos) {
				message.getFiles().add(Files.getFile(fileInfo));
			}
			ITransportService server = (ITransportService) Rmi.get(ITransportService.class, address);
			server.sendMessage(message);
		} catch (Exception e) {
			throw new TransportException("Can't send message to '" + message.getAddress(), e);
		}
	}

	@Override
	public Message receive() throws TransportException {
		return null;
	}

	@Override
	public void commit() throws TransportException {}

	@Override
	public void rollback() throws TransportException {}

	@Override
	public String getProtocol() {
		return PROTOCOL;
	}

}
