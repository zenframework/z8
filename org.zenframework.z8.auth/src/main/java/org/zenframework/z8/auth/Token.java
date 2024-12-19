package org.zenframework.z8.auth;

public class Token {
	private String domain;
	private String login;
	private long createdAt = System.currentTimeMillis();
	
	public String getDomain() {
		return domain;
	}
	
	public void setDomain(String domain) {
		this.domain = domain;
	}
	
	public String getLogin() {
		return login;
	}
	
	public void setLogin(String login) {
		this.login = login;
	}
	
	public long getCreatedAt() {
		return createdAt;
	}
}
