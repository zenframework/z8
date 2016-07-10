package org.zenframework.z8.server.ie.ws;

import javax.jws.WebParam;
import javax.jws.WebService;

import org.zenframework.z8.ie.xml.ExportEntry;
import org.zenframework.z8.server.ie.TransportException;
import org.zenframework.z8.server.types.guid;

@WebService
public interface TransportService {

	int RESULT_OK = 0;
	int RESULT_ERROR = 1;

	void sendMessage(@WebParam(name = "messageClass") String messageClass, @WebParam(name = "id") guid id,
			@WebParam(name = "sender") String sender, @WebParam(name = "address") String address,
			@WebParam(name = "exportEntry") ExportEntry exportEntry) throws TransportException;

}
