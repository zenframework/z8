package org.zenframework.z8.web.servlet;

import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

public class WebSocketServlet extends org.eclipse.jetty.websocket.servlet.WebSocketServlet {

	private static final long serialVersionUID = 5663293485070117804L;

	@Override
	public void configure(WebSocketServletFactory factory) {
		 factory.register(WebSocketListener.class);
	}

}
