package org.zenframework.z8.server.ldap;

import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.exceptions.AccessDeniedException;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import java.io.Closeable;
import java.util.*;

public class LdapAPI implements Closeable {
	static public final String ldapParametersPrefix = "ldap_";
	private final Connection connection;
	
	public static class Connection {
		private final DirContext context;
		
		public Connection() {
			context = createConnection(
					ServerConfig.ldapUrl(),
					ServerConfig.principalName(),
					ServerConfig.credentials());
		}

		public Connection(String ldapUrl, String principalName, String credentials) {
			context = createConnection(ldapUrl, principalName, credentials);
		}
		
		protected InitialDirContext createConnection(String ldapUrl, String principalName, String credentials) {
			Hashtable<String, String> environment = new Hashtable<>();
			environment.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
			environment.put(Context.PROVIDER_URL, ldapUrl);
			environment.put(Context.SECURITY_AUTHENTICATION, "simple");
			environment.put(Context.SECURITY_PRINCIPAL, principalName);
			environment.put(Context.SECURITY_CREDENTIALS, credentials);
			environment.put(Context.REFERRAL, "follow");
			try {
				return new InitialDirContext(environment);
			} catch (NamingException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public LdapAPI(Connection connection) {
		this.connection = connection;
	}
	
	public static LdapUser getUser(Connection connection, String login) {
		try (LdapAPI activeDirectory = new LdapAPI(connection)) {
			return activeDirectory.searchUser(ServerConfig.searchBase(), String.format(ServerConfig.searchUserFilter(), login));
		} catch (NamingException e) {
			ApplicationServer.getMonitor().error(e);
			return null;
		}
	}

	public static LdapUser getUser(Connection connection, String searchBase, String searchFilter) {
		try (LdapAPI activeDirectory = new LdapAPI(connection)) {
			return activeDirectory.searchUser(searchBase, searchFilter);
		} catch (NamingException e) {
			ApplicationServer.getMonitor().error(e);
			return null;
		}
	}
	
	public static List<LdapUser> getUsers(Connection connection, String searchBase, String searchFilter) {
		try (LdapAPI activeDirectory = new LdapAPI(connection)) {
			return activeDirectory.users(searchBase, searchFilter);
		} catch (NamingException e) {
			ApplicationServer.getMonitor().error(e);
			return new ArrayList<>();
		}
	}

	public static List<Map<String, String>> getGroups(Connection connection, String searchBase, String searchFilter) {
		try (LdapAPI activeDirectory = new LdapAPI(connection)) {
			return activeDirectory.groups(searchBase, searchFilter);
		} catch (NamingException e) {
			ApplicationServer.getMonitor().error(e);
			return new ArrayList<>();
		}
	}

	public static boolean isUserExist(Connection connection, String login) {
		LdapUser ldapUser = LdapAPI.getUser(connection, login);
		if (ldapUser != null && ldapUser.isLocked()) {
			throw new AccessDeniedException();
		}
		return ldapUser != null;
	}

	private LdapUser searchUser(String searchBase, String searchFilter) throws NamingException {
		SearchControls controls = new SearchControls();
		controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		controls.setCountLimit(1);

		NamingEnumeration<SearchResult> namingEnumeration = connection.context.search(searchBase, searchFilter, controls);

		if (namingEnumeration.hasMore()) {
			Attributes attributes = namingEnumeration.next().getAttributes();
			LdapUser ldapUser = extractUser(attributes);
			ldapUser.getParameters().putAll(extractLdapParameters(attributes));
			return ldapUser;
		} else {
			ApplicationServer.getMonitor().info(String.format("Active directory returned empty result. Query details: %s", searchFilter));
			return null;
		}
	}

	private List<LdapUser> users(String searchBase, String searchFilter) throws NamingException {
		SearchControls controls = new SearchControls();
		controls.setSearchScope(SearchControls.SUBTREE_SCOPE);

		NamingEnumeration<SearchResult> namingEnumeration = connection.context.search(searchBase, searchFilter, controls);

		List<LdapUser> result = new ArrayList<>();
		while (namingEnumeration.hasMore()) {
			Attributes attributes = namingEnumeration.next().getAttributes();
			LdapUser ldapUser = extractUser(attributes);
			ldapUser.getParameters().putAll(extractLdapParameters(attributes));
			result.add(ldapUser);
		}
		return result;
	}

	private List<Map<String, String>> groups(String searchBase, String searchFilter) throws NamingException {
		SearchControls controls = new SearchControls();
		controls.setSearchScope(SearchControls.SUBTREE_SCOPE);

		NamingEnumeration<SearchResult> namingEnumeration = connection.context.search(searchBase, searchFilter, controls);

		List<Map<String, String>> result = new ArrayList<>();
		while (namingEnumeration.hasMore()) {
			Attributes attributes = namingEnumeration.next().getAttributes();
			result.add(extractLdapParameters(attributes));
		}
		return result;
	}

	private LdapUser extractUser(Attributes attributes) throws NamingException {
		LdapUser ldapUser = new LdapUser();
		long ldapTimeStamp = Long.parseLong((String) attributes.get("accountExpires").get());
		Boolean locked = Boolean.FALSE;
		if (ldapTimeStamp > 0) {
			Date accountExpiredDate = LdapAPI.ldap2Date(ldapTimeStamp);
			if (accountExpiredDate.before(new Date())) {
				locked = Boolean.TRUE;
			}
		}
		ldapUser.setLocked(locked);
		ldapUser.getParameters().put(LdapAPI.ldapParametersPrefix + "parameters", Boolean.TRUE.toString());
		ldapUser.getParameters().put(LdapAPI.ldapParametersPrefix + "locked", locked.toString());
		ldapUser.setLogin(LdapAPI.getAttributeValue(attributes.get("sAMAccountname")));
		Attribute groups = attributes.get("memberof");
		if (groups != null) {
			String[] groupNames = new String[groups.size()];
			for (int i = 0; i < groups.size(); i++) {
				groupNames[i] = (String) groups.get(i);
			}
			ldapUser.setMemberOf(groupNames);
		}
		return ldapUser;
	}
		
	private Map<String, String>  extractLdapParameters(Attributes attributes) throws NamingException {
		Map<String, String> parameters = new HashMap<>();
		NamingEnumeration<String> keyNamesEnum = attributes.getIDs();
		while (keyNamesEnum.hasMore()) {
			String keyName = keyNamesEnum.next();
			Attribute attribute = attributes.get(keyName);
			// grab only single attrs
			if (attribute.size() == 1){
				parameters.put(LdapAPI.ldapParametersPrefix + keyName, LdapAPI.getAttributeValue(attribute));
			}
		}
		return parameters;
	}

	private static String getAttributeValue(Attribute attribute) {
		if (attribute == null) {
			return null;
		} else {
			try {
				return attribute.get() instanceof String ? (String) attribute.get() : null;
			} catch (Exception ignored) {
				ApplicationServer.getMonitor().info(String.format("Failed to extract value from: %s", attribute.getID()));
				return null;
			}
		}
	}

	private static Date ldap2Date(long ldapTimeStamp) { ;
		return new Date(ldapTimeStamp / 10000 - + 11644473600000L);
	}

	@Override
	public void close() {
		try {
			connection.context.close();
		} catch (NamingException ignored){}
	}
}
