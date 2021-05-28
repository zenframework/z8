package org.zenframework.z8.web.servlet;

import java.io.IOException;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

public class WebSocketListener extends WebSocketAdapter {

	@Override
	public void onWebSocketBinary(byte[] payload, int offset, int len) {
		// TODO Auto-generated method stub
		super.onWebSocketBinary(payload, offset, len);
	}

	@Override
	public void onWebSocketClose(int statusCode, String reason) {
		// TODO Auto-generated method stub
		super.onWebSocketClose(statusCode, reason);
	}

	@Override
	public void onWebSocketConnect(Session sess) {
		// TODO Auto-generated method stub
		super.onWebSocketConnect(sess);
	}

	@Override
	public void onWebSocketError(Throwable cause) {
		// TODO Auto-generated method stub
		super.onWebSocketError(cause);
	}

	@Override
	public void onWebSocketText(String message) {
		// TODO Auto-generated method stub
		super.onWebSocketText(message);
		try {
			this.getRemote().sendString("Test String");
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

}
