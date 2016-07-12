package org.zenframework.z8.server.ie.ws;

import org.zenframework.z8.ie.xml.ExportEntry;
import org.zenframework.z8.server.base.table.system.MessagesQueue;
import org.zenframework.z8.server.ie.IeUtil;
import org.zenframework.z8.server.ie.Import;
import org.zenframework.z8.server.ie.Message;
import org.zenframework.z8.server.ie.TransportException;
import org.zenframework.z8.server.request.Loader;
import org.zenframework.z8.server.types.guid;

public class TransportServiceImpl implements TransportService {

	private final String endpoint;

	public TransportServiceImpl(String endpoint) {
		this.endpoint = endpoint;
	}

	@Override
	public void sendMessage(String messageClass, guid id, String sender, String address, ExportEntry exportEntry)
			throws TransportException {
		Message message = (Message) Loader.getInstance(messageClass);
		message.setId(id);
		message.setAddress(address);
		message.setSender(sender);
		try {
			message.setFiles(IeUtil.xmlFilesToFileInfos(exportEntry.getFiles()));
			message.setExportEntry(exportEntry);
			MessagesQueue.newInstance().addMessage(message, endpoint, MessagesQueue.Direction.IN);
			Import.importFiles(message);
		} catch (Exception e) {
			throw new TransportException("Can't send IE message " + message.getId() + " from " + message.getSender()
					+ " to " + message.getAddress(), e);
		}
	}

}
