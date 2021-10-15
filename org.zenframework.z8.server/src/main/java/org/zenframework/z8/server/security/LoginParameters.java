package org.zenframework.z8.server.security;

import org.zenframework.z8.server.types.guid;

public class LoginParameters {

	private guid userId;
	private String login;
	private String address;
	private String schema;
	private String firstName;
	private String lastName;
	private String email;

	public LoginParameters() {}

	public LoginParameters(guid id) {
		this.userId = id;
	}

	public LoginParameters(String login) {
		this.login = login;
	}

	public guid getUserId() {
		return userId;
	}

	public LoginParameters setUserId(guid id) {
		this.userId = id;
		return this;
	}

	public String getLogin() {
		return login;
	}

	public LoginParameters setLogin(String login) {
		this.login = login;
		return this;
	}

	public String getAddress() {
		return address;
	}

	public LoginParameters setAddress(String address) {
		this.address = address;
		return this;
	}

	public String getSchema() {
		return schema;
	}

	public LoginParameters setSchema(String schema) {
		this.schema = schema;
		return this;
	}
	
	public String getFirstName() {
		return firstName;
	}
	
	public LoginParameters setFirstName(String firstName) {
		this.firstName = firstName;
		return this;
	}
	
	public String getLastName() {
		return lastName;
	}
	
	public LoginParameters setLastName(String lastName) {
		this.lastName = lastName;
		return this;
	}
	
	public String getEmail() {
		return email;
	}
	
	public LoginParameters setEmail(String email) {
		this.email = email;
		return this;
	}
}
