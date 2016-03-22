package org.zenframework.z8.server.ie.ws;

import javax.xml.ws.Endpoint;

import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.zenframework.z8.server.base.table.system.Properties;
import org.zenframework.z8.server.ie.AbstractTransport;
import org.zenframework.z8.server.ie.IeUtil;
import org.zenframework.z8.server.ie.Message;
import org.zenframework.z8.server.ie.TransportContext;
import org.zenframework.z8.server.ie.TransportException;
import org.zenframework.z8.server.runtime.ServerRuntime;

public class WsTransport extends AbstractTransport {

	public static final String PROTOCOL = "ws";

	private Endpoint endpoint;

	public WsTransport(TransportContext context) {
		super(context);
	}

	@Override
	public void init() {
		endpoint = Endpoint.publish(Properties.getProperty(ServerRuntime.WsEndpointProperty), new TransportServiceImpl(
				context.getProperty(TransportContext.SelfAddressProperty)));
	}

	@Override
	public void shutdown() {
		if (endpoint != null) {
			endpoint.stop();
			endpoint = null;
		}
	}

	@Override
	public void connect() throws TransportException {}

	@Override
	public void close() {}

	@Override
	public void send(Message message) throws TransportException {
		JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
		factory.getInInterceptors().add(new LoggingInInterceptor());
		factory.getOutInterceptors().add(new LoggingOutInterceptor());
		factory.setServiceClass(TransportService.class);
		factory.setAddress(message.getAddress());
		message.getExportEntry().setFiles(IeUtil.fileInfosToXmlFiles(message.getFiles()));
		TransportService client = (TransportService) factory.create();
		client.sendMessage(message.getId(), message.getSender(), message.getExportEntry());
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
