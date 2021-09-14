package org.zenframework.z8.server.security;

import org.zenframework.z8.server.types.guid;

public class LoginParameters {

	private guid id;
	private String login;
	private String address;
	private String schema;

	public LoginParameters() {}

	public LoginParameters(guid id) {
		this.id = id;
	}

	public LoginParameters(String login) {
		this.login = login;
	}

	public guid getId() {
		return id;
	}

	public void setId(guid id) {
		this.id = id;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

}
