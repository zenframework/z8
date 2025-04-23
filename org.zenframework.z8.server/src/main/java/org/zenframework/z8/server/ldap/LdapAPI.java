package org.zenframework.z8.server.ldap;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.json.parser.JsonPath;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.ssl.TrustedSSLSocketFactory;

public class LdapAPI {

	private static interface Extractor<T> {
		T extract(SearchResult result) throws NamingException;
	}

	private static final String LDAPS = "ldaps";

	private static final JsonPath PATH_BASE_DN = new JsonPath("baseDn");

	private static final JsonPath PATH_USERS_BASE_DN = new JsonPath("users/baseDn");
	private static final JsonPath PATH_USERS_LOGIN_FIELD = new JsonPath("users/loginField");
	private static final JsonPath PATH_USERS_GROUP_FIELD = new JsonPath("users/groupField"); // deprecated
	private static final JsonPath PATH_USERS_MEMBER_OF_FIELD = new JsonPath("users/groupField");
	private static final JsonPath PATH_USERS_EXPIRES_FIELD = new JsonPath("users/expiresField");
	private static final JsonPath PATH_USERS_FIELDS = new JsonPath("users/fields");
	private static final JsonPath PATH_USERS_FILTER = new JsonPath("users/filter");

	private static final JsonPath PATH_GROUPS_BASE_DN = new JsonPath("groups/baseDn");
	private static final JsonPath PATH_GROUPS_NAME_FIELD = new JsonPath("groups/nameField");
	private static final JsonPath PATH_GROUPS_MEMBER_FIELD = new JsonPath("groups/memberField");
	private static final JsonPath PATH_GROUPS_FIELDS = new JsonPath("groups/fields");
	private static final JsonPath PATH_GROUPS_FILTER = new JsonPath("groups/filter");

	private static final String DEFAULT_USERS_LOGIN_FIELD = "sAMAccountname";
	private static final String DEFAULT_USERS_MEMBER_OF_FIELD = "memberOf";
	private static final String DEFAULT_USERS_EXPIRES_FIELD = "accountExpires";
	private static final String DEFAULT_USERS_FILTER = "(objectCategory=Person)";

	private static final String DEFAULT_GROUPS_NAME_FIELD = "sAMAccountname";
	private static final String DEFAULT_GROUPS_MEMBER_FIELD = "";
	private static final String DEFAULT_GROUPS_FILTER = "(objectCategory=Group)";

	private final InitialDirContext context;

	private String baseDn;

	private String usersBaseDn;
	private String usersLoginField;
	private String usersMemberOfField;
	private String usersExpiresField;
	private Map<String, String> usersFields;
	private String usersFilter;
	private String userFilter;

	private String groupsBaseDn;
	private String groupsNameField;
	private String groupsMemberField;
	private Map<String, String> groupsFields;
	private String groupsFilter;
	private String groupFilter;

	private final Map<String, LdapUser> usersCache = new HashMap<String, LdapUser>();
	private final Map<String, LdapGroup> groupsCache = new HashMap<String, LdapGroup>();

	public LdapAPI(String ldapUrl, String principalName, String credentials, String config) {
		Hashtable<String, String> environment = new Hashtable<>();
		environment.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		environment.put(Context.PROVIDER_URL, ldapUrl);
		environment.put(Context.SECURITY_AUTHENTICATION, "simple");
		environment.put(Context.SECURITY_PRINCIPAL, principalName);
		environment.put(Context.SECURITY_CREDENTIALS, credentials);
		environment.put(Context.REFERRAL, "follow");
		environment.put("com.sun.jndi.ldap.connect.timeout", "3000");

		if (ldapUrl.trim().toLowerCase().startsWith(LDAPS) && ServerConfig.ldapSslTrusted())
			environment.put("java.naming.ldap.factory.socket", TrustedSSLSocketFactory.class.getName());

		String msg = "LDAP: Connecting to " + ldapUrl + ", user '" + principalName + "': ";

		try {
			context = new InitialDirContext(environment);
			Trace.logEvent(msg + "OK");
		} catch(NamingException e) {
			Trace.logEvent(msg + "Error");
			throw new RuntimeException(e);
		}

		if (config == null || config.isEmpty())
			return;

		JsonObject json = new JsonObject(config);

		baseDn = PATH_BASE_DN.evaluate(json, "").trim();

		usersBaseDn = PATH_USERS_BASE_DN.evaluate(json, "").trim();
		usersBaseDn += (!baseDn.isEmpty() && !usersBaseDn.isEmpty() ? "," : "") + baseDn;
		usersLoginField = PATH_USERS_LOGIN_FIELD.evaluate(json, DEFAULT_USERS_LOGIN_FIELD).trim();
		usersMemberOfField = PATH_USERS_MEMBER_OF_FIELD.evaluate(json,
				PATH_USERS_GROUP_FIELD.evaluate(json, DEFAULT_USERS_MEMBER_OF_FIELD)).trim();
		usersExpiresField = PATH_USERS_EXPIRES_FIELD.evaluate(json, DEFAULT_USERS_EXPIRES_FIELD).trim();
		usersFields = PATH_USERS_FIELDS.evaluate(json, new HashMap<String, String>());
		usersFilter = PATH_USERS_FILTER.evaluate(json, DEFAULT_USERS_FILTER).trim();
		userFilter = "(&" + usersFilter + "(" + usersLoginField + "={0}))";

		groupsBaseDn = PATH_GROUPS_BASE_DN.evaluate(json, "").trim();
		groupsBaseDn += (!baseDn.isEmpty() && !groupsBaseDn.isEmpty() ? "," : "") + baseDn;
		groupsNameField = PATH_GROUPS_NAME_FIELD.evaluate(json, DEFAULT_GROUPS_NAME_FIELD).trim();
		groupsMemberField = PATH_GROUPS_MEMBER_FIELD.evaluate(json, DEFAULT_GROUPS_MEMBER_FIELD).trim();
		groupsFields = PATH_GROUPS_FIELDS.evaluate(json, new HashMap<String, String>());
		groupsFilter = PATH_GROUPS_FILTER.evaluate(json, DEFAULT_GROUPS_FILTER).trim();
		groupFilter = "(&" + groupsFilter + "(" + groupsNameField + "={0}))";
	}

	public void close() {
		try {
			context.close();
		} catch(NamingException ignored) {}
	}

	public LdapUser getUser(String login) {
		LdapUser user = usersCache.get(login);
		if (user != null)
			return user;
		String searchFilter = MessageFormat.format(userFilter, login);
		return getSingleEntry(getUsers(usersBaseDn, searchFilter, true), usersBaseDn, searchFilter);
	}

	public LdapUser getUser(String searchBase, String searchFilter) {
		return getSingleEntry(getUsers(searchBase, searchFilter, false), searchBase, searchFilter);
	}

	public Collection<LdapUser> getUsers() {
		if (usersCache.isEmpty()) {
			for (LdapUser user : getUsers(usersBaseDn, usersFilter, true))
				usersCache.put(user.getLogin(), user);
		}
		return usersCache.values();
	}

	public Collection<LdapUser> getUsers(String searchBase, String searchFilter) {
		return getUsers(searchBase, searchFilter, false);
	}

	public Collection<LdapUser> getUsers(String searchBase, String searchFilter, boolean useGroupsCache) {
		return search(searchBase, searchFilter, new Extractor<LdapUser>() {
			@Override
			public LdapUser extract(SearchResult result) throws NamingException {
				return extractUser(result, useGroupsCache);
			}
		});
	}

	public LdapGroup getGroup(String groupName) {
		LdapGroup group = groupsCache.get(groupName);
		if (group != null)
			return group;
		return getGroup(groupsBaseDn, MessageFormat.format(groupFilter, groupName));
	}

	public LdapGroup getGroup(String searchBase, String searchFilter) {
		return getSingleEntry(getGroups(searchBase, searchFilter), searchBase, searchFilter);
	}

	public Collection<LdapGroup> getGroups() {
		if (groupsCache.isEmpty()) {
			for (LdapGroup group : getGroups(groupsBaseDn, groupsFilter))
				groupsCache.put(group.getName(), group);
		}
		return groupsCache.values();
	}

	public Collection<LdapGroup> getGroups(String searchBase, String searchFilter) {
		return search(searchBase, searchFilter, new Extractor<LdapGroup>() {
			@Override
			public LdapGroup extract(SearchResult result) throws NamingException {
				return extractGroup(result);
			}
		});
	}

	private <T> Collection<T> search(String searchBase, String searchFilter, Extractor<T> extractor) {
		SearchControls controls = new SearchControls();
		controls.setSearchScope(SearchControls.SUBTREE_SCOPE);

		String msg = "LDAP: Search '" + searchBase + "', filter '" + searchFilter + "': ";

		try {
			NamingEnumeration<SearchResult> namingEnumeration = context.search(searchBase, searchFilter, controls);
			Collection<T> result = new HashSet<T>();
			while (namingEnumeration.hasMore())
				result.add(extractor.extract(namingEnumeration.next()));
			Trace.logEvent(msg + "OK");
			return result;
		} catch (NamingException e) {
			Trace.logEvent(msg + "Error");
			throw new RuntimeException(e);
		}
	}

	private LdapUser extractUser(SearchResult result, boolean useGroupsCache) throws NamingException {
		Attributes attributes = result.getAttributes();

		LdapUser ldapUser = new LdapUser();
		ldapUser.setDomainName(result.getNameInNamespace());
		ldapUser.setLogin(LdapAPI.getAttributeValue(attributes.get(usersLoginField)));

		Attribute expires = attributes.get(usersExpiresField);
		long ldapTimeStamp = expires != null ? Long.parseLong((String) expires.get()) : -1L;

		ldapUser.setLocked(ldapTimeStamp > 0 && LdapAPI.ldap2Date(ldapTimeStamp).before(new Date()));

		Attribute memberOf = attributes.get(usersMemberOfField);

		if (memberOf != null) {
			for (int i = 0; i < memberOf.size(); i++)
				ldapUser.addMemberOf((String) memberOf.get(i));
		} else if (useGroupsCache) {
			for (LdapGroup group : getGroups())
				if (group.getMembers().contains(ldapUser.getDomainName()))
					ldapUser.addMemberOf(group.getDomainName());
		}

		ldapUser.getParameters().putAll(extractParameters(attributes, usersFields));

		return ldapUser;
	}

	private LdapGroup extractGroup(SearchResult result) throws NamingException {
		Attributes attributes = result.getAttributes();

		LdapGroup ldapGroup = new LdapGroup();
		ldapGroup.setDomainName(result.getNameInNamespace());
		ldapGroup.setName(LdapAPI.getAttributeValue(attributes.get(groupsNameField)));

		Attribute members = attributes.get(groupsMemberField);

		if (members != null) {
			for (int i = 0; i < members.size(); i++)
				ldapGroup.addMember((String) members.get(i));
		}

		ldapGroup.getParameters().putAll(extractParameters(attributes, groupsFields));

		return ldapGroup;
	}

	private static Map<String, String> extractParameters(Attributes attributes, Map<String, String> fields) throws NamingException {
		Map<String, String> parameters = new HashMap<>();

		for (Map.Entry<String, String> entry : fields.entrySet()) {
			Attribute attribute = attributes.get(entry.getValue());
			if (attribute != null && attribute.size() == 1)
				parameters.put(entry.getKey(), getAttributeValue(attribute));
		}

		return parameters;
	}

	private static String getAttributeValue(Attribute attribute) {
		if (attribute == null)
			return null;

		try {
			return attribute.get() instanceof String ? (String) attribute.get() : null;
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}

	private static Date ldap2Date(long ldapTimeStamp) {
		return new Date(ldapTimeStamp / 10000 - +11644473600000L);
	}

	private static <T> T getSingleEntry(Collection<T> c, String base, String filter) {
		if (c.isEmpty())
			return null;
		if (c.size() > 1)
			throw new RuntimeException(MessageFormat.format("LDAP multiple users found [{0}, {1}]: {2}", base, filter, c));
		return c.iterator().next();
	}
}
