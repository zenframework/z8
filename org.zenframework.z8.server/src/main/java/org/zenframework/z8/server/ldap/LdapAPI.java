package org.zenframework.z8.server.ldap;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

public class LdapAPI {

	private static interface Extractor<T> {
		T extract(Attributes attributes) throws NamingException;
	}

	private final InitialDirContext context;

	public LdapAPI(String ldapUrl, String principalName, String credentials) {
		Hashtable<String, String> environment = new Hashtable<>();
		environment.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		environment.put(Context.PROVIDER_URL, ldapUrl);
		environment.put(Context.SECURITY_AUTHENTICATION, "simple");
		environment.put(Context.SECURITY_PRINCIPAL, principalName);
		environment.put(Context.SECURITY_CREDENTIALS, credentials);
		environment.put(Context.REFERRAL, "follow");

		try {
			context = new InitialDirContext(environment);
		} catch(NamingException e) {
			throw new RuntimeException(e);
		}
	}

	public void close() {
		try {
			context.close();
		} catch(NamingException ignored) {}
	}

	public LdapUser getUser(String searchBase, String searchFilter) {
		List<LdapUser> users = getUsers(searchBase, searchFilter);
		if (users.isEmpty())
			return null;
		if (users.size() > 1)
			throw new RuntimeException("LDAP multiple users found [" + searchBase + ", " + searchFilter + "]: " + users);
		return users.get(0);
	}

	public List<LdapUser> getUsers(String searchBase, String searchFilter) {
		return search(searchBase, searchFilter, new Extractor<LdapUser>() {
			@Override
			public LdapUser extract(Attributes attributes) throws NamingException {
				return extractUser(attributes);
			}
		});
	}

	public List<Map<String, String>> getGroups(String searchBase, String searchFilter) {
		return search(searchBase, searchFilter, new Extractor<Map<String, String>>() {
			@Override
			public Map<String, String> extract(Attributes attributes) throws NamingException {
				return extractParameters(attributes);
			}
		});
	}

	private <T> List<T> search(String searchBase, String searchFilter, Extractor<T> extractor) {
		SearchControls controls = new SearchControls();
		controls.setSearchScope(SearchControls.SUBTREE_SCOPE);

		try {
			NamingEnumeration<SearchResult> namingEnumeration = context.search(searchBase, searchFilter, controls);
			List<T> result = new ArrayList<T>();
			while (namingEnumeration.hasMore())
				result.add(extractor.extract(namingEnumeration.next().getAttributes()));
			return result;
		} catch (NamingException e) {
			throw new RuntimeException(e);
		}
	}

	private LdapUser extractUser(Attributes attributes) throws NamingException {
		LdapUser ldapUser = new LdapUser();
		long ldapTimeStamp = Long.parseLong((String)attributes.get("accountExpires").get());
		Boolean locked = Boolean.FALSE;

		if(ldapTimeStamp > 0) {
			Date accountExpiredDate = LdapAPI.ldap2Date(ldapTimeStamp);
			if(accountExpiredDate.before(new Date()))
				locked = Boolean.TRUE;
		}

		ldapUser.setLocked(locked);
		ldapUser.getParameters().put("parameters", Boolean.TRUE.toString());
		ldapUser.getParameters().put("locked", locked.toString());
		ldapUser.setLogin(LdapAPI.getAttributeValue(attributes.get("sAMAccountname")));

		Attribute groups = attributes.get("memberof");

		if (groups != null) {
			String[] groupNames = new String[groups.size()];
			for (int i = 0; i < groups.size(); i++)
				groupNames[i] = (String)groups.get(i);
			ldapUser.setMemberOf(groupNames);
		}

		ldapUser.getParameters().putAll(extractParameters(attributes));

		return ldapUser;
	}

	private static Map<String, String> extractParameters(Attributes attributes) throws NamingException {
		Map<String, String> parameters = new HashMap<>();
		NamingEnumeration<String> keyNamesEnum = attributes.getIDs();

		while (keyNamesEnum.hasMore()) {
			String keyName = keyNamesEnum.next();
			Attribute attribute = attributes.get(keyName);
			if(attribute.size() == 1)
				parameters.put(keyName, getAttributeValue(attribute));
		}

		return parameters;
	}

	private static String getAttributeValue(Attribute attribute) {
		if (attribute == null)
			return null;

		try {
			return attribute.get() instanceof String ? (String)attribute.get() : null;
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}

	private static Date ldap2Date(long ldapTimeStamp) {
		return new Date(ldapTimeStamp / 10000 - +11644473600000L);
	}
}
