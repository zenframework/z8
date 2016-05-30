package org.zenframework.z8.server.ie;

import java.io.Serializable;

import org.zenframework.z8.server.types.guid;

public class TransportRoute implements Serializable {

	private static final long serialVersionUID = -2215330098958924695L;

	private final guid routeId;
	private final String receiver;
	private final String protocol;
	private final String address;

	private int priority = 0;
	private boolean active = true;

	public TransportRoute(String address, String protocol, String transportAddress) {
		this.routeId = guid.create();
		this.receiver = address;
		this.protocol = protocol;
		this.address = transportAddress;
	}

	public TransportRoute(guid routeId, String receiver, String protocol, String address) {
		this.routeId = routeId;
		this.receiver = receiver;
		this.protocol = protocol;
		this.address = address;
	}

	public TransportRoute(guid routeId, String receiver, String protocol, String address, int priority, boolean active) {
		this.routeId = routeId;
		this.receiver = receiver;
		this.protocol = protocol;
		this.address = address;
		this.priority = priority;
		this.active = active;
	}

	public guid getRouteId() {
		return routeId;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getTransportUrl() {
		return IeUtil.getUrl(getProtocol(), getAddress());
	}

	public String getReceiver() {
		return receiver;
	}

	public String getProtocol() {
		return protocol;
	}

	public String getAddress() {
		return address;
	}

}
