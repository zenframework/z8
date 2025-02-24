package org.zenframework.z8.server.ldap;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LdapUser {
	private String login;
	private String domainName;
	private boolean locked;

	private final Set<String> memberOf = new HashSet<String>();
	private Map<String, String> parameters = new HashMap<String, String>();

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	public boolean isLocked() {
		return locked;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	public Set<String> getMemberOf() {
		return memberOf;
	}

	public void setMemberOf(Collection<String> memberOf) {
		this.memberOf.clear();
		this.memberOf.addAll(memberOf);
	}

	public void addMemberOf(String memberOf) {
		this.memberOf.add(memberOf);
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	public String getParameter(String name) {
		return parameters.get(name);
	}

	public void setParameter(String name, String value) {
		parameters.put(name, value);
	}
}
