package org.zenframework.z8.server.ie.ws;

import java.util.UUID;

import org.zenframework.z8.ie.xml.ExportEntry;
import org.zenframework.z8.server.ie.ExportMessages;
import org.zenframework.z8.server.ie.IeUtil;
import org.zenframework.z8.server.ie.Import;
import org.zenframework.z8.server.ie.Message;
import org.zenframework.z8.server.ie.TransportException;
import org.zenframework.z8.server.request.Loader;

public class TransportServiceImpl implements TransportService {

	@Override
	public void sendMessage(String messageClass, UUID id, String sender, String address, ExportEntry exportEntry)
			throws TransportException {
		Message message = (Message) Loader.getInstance(messageClass);
		message.setId(id);
		message.setAddress(address);
		message.setSender(sender);
		try {
			message.setFiles(IeUtil.xmlFilesToFileInfos(exportEntry.getFiles()));
			message.setExportEntry(exportEntry);
			ExportMessages.newInstance().addMessage(message, ExportMessages.Direction.IN);
			Import.importFiles(message);
		} catch (Exception e) {
			throw new TransportException("Can't send IE message " + message.getId() + " from " + message.getSender()
					+ " to " + message.getAddress(), e);
		}
	}

}
