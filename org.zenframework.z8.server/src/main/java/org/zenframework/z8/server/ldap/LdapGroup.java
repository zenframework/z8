package org.zenframework.z8.server.ldap;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LdapGroup {
	private String name;
	private String domainName;

	private final Set<String> members = new HashSet<String>();
	private Map<String, String> parameters = new HashMap<String, String>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	public Set<String> getMembers() {
		return members;
	}

	public void setMembers(Collection<String> members) {
		this.members.clear();
		this.members.addAll(members);
	}

	public void addMember(String member) {
		members.add(member);
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	public void setParameter(String name, String value) {
		parameters.put(name, value);
	}
}
