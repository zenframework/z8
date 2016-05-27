package org.zenframework.z8.server.ie.ws;

import java.util.UUID;

import org.zenframework.z8.ie.xml.ExportEntry;
import org.zenframework.z8.server.base.table.system.Files;
import org.zenframework.z8.server.ie.ExportMessages;
import org.zenframework.z8.server.ie.IeUtil;
import org.zenframework.z8.server.ie.Import;
import org.zenframework.z8.server.ie.Message;
import org.zenframework.z8.server.ie.TransportException;

public class TransportServiceImpl implements TransportService {

	private final String endpoint;

	public TransportServiceImpl(String endpoint) {
		this.endpoint = endpoint;
	}

	@Override
	public void sendMessage(UUID id, String sender, ExportEntry exportEntry) throws TransportException {
		Message message = Message.instance(id);
		message.setAddress(endpoint);
		message.setSender(sender);
		try {
			message.setFiles(IeUtil.xmlFilesToFileInfos(exportEntry.getFiles()));
			message.setExportEntry(exportEntry);
			new ExportMessages.CLASS<ExportMessages>(null).get().addMessage(message, endpoint, ExportMessages.Direction.IN);
			Import.importFiles(message, Files.instance());
		} catch (Exception e) {
			throw new TransportException("Can't send IE message " + message.getId() + " from " + message.getSender()
					+ " to " + message.getAddress(), e);
		}
	}

}
