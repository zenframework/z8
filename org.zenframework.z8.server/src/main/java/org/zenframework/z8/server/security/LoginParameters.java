package org.zenframework.z8.server.security;

import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.types.guid;

public class LoginParameters {
	private static final String Login = "login";
	private static final String UserId = "userId";
	private static final String Trusted = "trusted";
	private static final String Address = "address";
	private static final String Schema = "schema";
	private static final String FirstName = "firstName";
	private static final String LastName = "lastName";
	private static final String Email = "email";
	private static final String Company = "company";
	private static final String Position = "position";

	private final Map<String, String> parameters = new HashMap<String, String>();

	private guid userId;
	private String login;
	private boolean trusted;

	public LoginParameters(String login) {
		setLogin(login);
	}

	public LoginParameters(guid userId) {
		setUserId(userId);
	}

	public boolean isSystem() {
		return login == null;
	}

	public guid getUserId() {
		return userId;
	}

	public LoginParameters setUserId(guid userId) {
		this.userId = userId;
		return this;
	}

	public String getLogin() {
		return login;
	}

	public LoginParameters setLogin(String login) {
		this.login = login;
		return this;
	}

	public boolean isTrusted() {
		return trusted;
	}

	public LoginParameters setTrusted(boolean trusted) {
		this.trusted = trusted;
		return this;
	}

	public String getAddress() {
		return parameters.get(Address);
	}

	public LoginParameters setAddress(String address) {
		parameters.put(Address, address);
		return this;
	}

	public String getSchema() {
		return parameters.get(Schema);
	}

	public LoginParameters setSchema(String schema) {
		parameters.put(Schema, schema);
		return this;
	}

	public String getFirstName() {
		return parameters.get(FirstName);
	}

	public LoginParameters setFirstName(String firstName) {
		parameters.put(FirstName, firstName);
		return this;
	}

	public String getLastName() {
		return parameters.get(LastName);
	}

	public LoginParameters setLastName(String lastName) {
		parameters.put(LastName, lastName);
		return this;
	}

	public String getEmail() {
		return parameters.get(Email);
	}

	public LoginParameters setEmail(String email) {
		parameters.put(Email, email);
		return this;
	}

	public String getCompany() {
		return parameters.get(Company);
	}

	public LoginParameters setCompany(String company) {
		parameters.put(Company, company);
		return this;
	}

	public String getPosition() {
		return parameters.get(Position);
	}

	public LoginParameters setPosition(String position) {
		parameters.put(Position, position);
		return this;
	}

	public Map<String, String> toMap() {
		Map<String, String> map = new HashMap<String, String>();
		if (login != null)
			map.put(Login, login);
		if (userId != null)
			map.put(UserId, userId.toString());
		map.put(Trusted, Boolean.toString(trusted));
		return map;
	}
}
