package org.zenframework.z8.server.security;

import java.util.HashMap;

import org.zenframework.z8.server.types.guid;

public class LoginParameters extends HashMap<String, String> {
	private static final long serialVersionUID = 1L;

	private static final String UserId = "userId";
	private static final String Login = "login";
	private static final String Address = "address";
	private static final String Schema = "schema";
	private static final String FirstName = "firstName";
	private static final String LastName = "lastName";
	private static final String Email = "email";
	private static final String Company = "company";
	private static final String Position = "position";

	private guid userId = null;

	public LoginParameters() {}

	public LoginParameters(guid userId) {
		setUserId(userId);
	}

	public guid getUserId() {
		return userId;
	}

	public LoginParameters setUserId(guid userId) {
		this.userId = userId;
		put(UserId, userId.toString());
		return this;
	}

	public String getLogin() {
		return get(Login);
	}

	public LoginParameters setLogin(String login) {
		put(Login, login);
		return this;
	}

	public String getAddress() {
		return get(Address);
	}

	public LoginParameters setAddress(String address) {
		put(Address, address);
		return this;
	}

	public String getSchema() {
		return get(Schema);
	}

	public LoginParameters setSchema(String schema) {
		put(Schema, schema);
		return this;
	}

	public String getFirstName() {
		return get(FirstName);
	}

	public LoginParameters setFirstName(String firstName) {
		put(FirstName, firstName);
		return this;
	}

	public String getLastName() {
		return get(LastName);
	}

	public LoginParameters setLastName(String lastName) {
		put(LastName, lastName);
		return this;
	}

	public String getEmail() {
		return get(Email);
	}

	public LoginParameters setEmail(String email) {
		put(Email, email);
		return this;
	}

	public String getCompany() {
		return get(Company);
	}

	public LoginParameters setCompany(String company) {
		put(Company, company);
		return this;
	}

	public String getPosition() {
		return get(Position);
	}

	public LoginParameters setPosition(String position) {
		put(Position, position);
		return this;
	}
}
