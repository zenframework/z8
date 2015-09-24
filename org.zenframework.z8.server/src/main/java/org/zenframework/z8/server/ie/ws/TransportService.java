package org.zenframework.z8.server.ie.ws;

import java.util.UUID;

import javax.jws.WebParam;
import javax.jws.WebService;

import org.zenframework.z8.ie.xml.ExportEntry;
import org.zenframework.z8.server.ie.TransportException;

@WebService
public interface TransportService {

    int RESULT_OK = 0;
    int RESULT_ERROR = 1;

    void sendMessage(@WebParam(name = "id") UUID id, @WebParam(name = "sender") String sender,
            @WebParam(name = "exportEntry") ExportEntry exportEntry) throws TransportException;

}
