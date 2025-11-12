package org.zenframework.z8.server.ldap;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
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
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.json.parser.JsonPath;
import org.zenframework.z8.server.ssl.TrustedSSLSocketFactory;

public class LdapAPI {

	static private final String LDAPS = "ldaps";

	private static final JsonPath PATH_BASE_DN = new JsonPath("baseDn");

	private static final JsonPath PATH_USERS_BASE_DN = new JsonPath("users/baseDn");
	private static final JsonPath PATH_USERS_LOGIN_FIELD = new JsonPath("users/loginField");
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
	private static final String DEFAULT_USERS_MEMBER_OF_FIELD = null; // "memberOf";
	private static final String DEFAULT_USERS_EXPIRES_FIELD = "accountExpires";
	private static final String DEFAULT_USERS_FILTER = "(objectCategory=Person)";

	private static final String DEFAULT_GROUPS_NAME_FIELD = "sAMAccountname";
	private static final String DEFAULT_GROUPS_MEMBER_FIELD = null; // "member";
	private static final String DEFAULT_GROUPS_FILTER = "(objectCategory=Group)";

	private final int pageSize;

	private LdapContext context;

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

	private final Hashtable<String, String> environment = new Hashtable<String, String>();

	private final Map<String, LdapUser> usersCache = new HashMap<String, LdapUser>();
	private final Map<String, LdapGroup> groupsCache = new HashMap<String, LdapGroup>();

	public LdapAPI(String ldapUrl, String principalName, String credentials) {
		environment.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		environment.put(Context.PROVIDER_URL, ldapUrl);
		environment.put(Context.SECURITY_AUTHENTICATION, "simple");
		environment.put(Context.SECURITY_PRINCIPAL, principalName);
		environment.put(Context.SECURITY_CREDENTIALS, credentials);
		environment.put(Context.REFERRAL, "follow");
		environment.put("com.sun.jndi.ldap.connect.timeout", Integer.toString(ServerConfig.ldapConnectTimeout()));

		if (ldapUrl.trim().toLowerCase().startsWith(LDAPS) && ServerConfig.ldapSslTrusted())
			environment.put("java.naming.ldap.factory.socket", TrustedSSLSocketFactory.class.getName());

		pageSize = ServerConfig.ldapReadPageSize();

		open();
	}

	public void setConfig(String config) {
		setConfig(config != null && !config.isEmpty() ? new JsonObject(config) : null);
	}

	public void setConfig(JsonObject config) {
		if (config == null || config.isEmpty())
			return;

		baseDn = PATH_BASE_DN.evaluate(config, "");

		usersBaseDn = PATH_USERS_BASE_DN.evaluate(config, "");
		usersBaseDn += (!baseDn.isEmpty() && !usersBaseDn.isEmpty() ? "," : "") + baseDn;
		usersLoginField = PATH_USERS_LOGIN_FIELD.evaluate(config, DEFAULT_USERS_LOGIN_FIELD);
		usersMemberOfField = PATH_USERS_MEMBER_OF_FIELD.evaluate(config, DEFAULT_USERS_MEMBER_OF_FIELD);
		usersExpiresField = PATH_USERS_EXPIRES_FIELD.evaluate(config, DEFAULT_USERS_EXPIRES_FIELD);
		usersFields = PATH_USERS_FIELDS.evaluate(config, new HashMap<String, String>());
		usersFilter = PATH_USERS_FILTER.evaluate(config, DEFAULT_USERS_FILTER);
		userFilter = "(&" + usersFilter + "(" + usersLoginField + "={0}))";

		groupsBaseDn = PATH_GROUPS_BASE_DN.evaluate(config, null);

		if (groupsBaseDn != null)
			groupsBaseDn += (!baseDn.isEmpty() && !groupsBaseDn.isEmpty() ? "," : "") + baseDn;

		groupsNameField = PATH_GROUPS_NAME_FIELD.evaluate(config, DEFAULT_GROUPS_NAME_FIELD);
		groupsMemberField = PATH_GROUPS_MEMBER_FIELD.evaluate(config, DEFAULT_GROUPS_MEMBER_FIELD);
		groupsFields = PATH_GROUPS_FIELDS.evaluate(config, new HashMap<String, String>());
		groupsFilter = PATH_GROUPS_FILTER.evaluate(config, DEFAULT_GROUPS_FILTER);
		groupFilter = "(&" + groupsFilter + "(" + groupsNameField + "={0}))";
	}

	public void open() {
		if (context != null)
			return;

		try {
			context = new InitialLdapContext(environment, null);
		} catch(NamingException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean isOpened() {
		return context != null;
	}

	public void close() {
		try {
			context.close();
		} catch(NamingException ignored) {} finally {
			context = null;
		}
	}

	public LdapUser getUser(String login) {
		LdapUser user = usersCache.get(login);

		if (user != null)
			return user;

		return getUser(usersBaseDn, MessageFormat.format(userFilter, login));
	}

	public LdapUser getUser(String searchBase, String searchFilter) {
		return getSingleEntry(getUsers(searchBase, searchFilter, 0), searchBase, searchFilter);
	}

	public Collection<LdapUser> getUsers() {
		if (usersCache.isEmpty()) {
			for (LdapUser user : getUsers(usersBaseDn, usersFilter, pageSize))
				usersCache.put(user.getLogin(), user);
		}

		return usersCache.values();
	}

	public Collection<LdapUser> getUsers(String searchBase, String searchFilter) {
		return getUsers(searchBase, searchFilter, pageSize);
	}

	public Collection<LdapUser> getUsers(String searchBase, String searchFilter, int pageSize) {
		return search(searchBase, searchFilter, pageSize, new Extractor<LdapUser>() {
			@Override
			public LdapUser extract(SearchResult result) throws NamingException {
				return extractUser(result);
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
		return getGroups(searchBase, searchFilter, pageSize);
	}

	public Collection<LdapGroup> getGroups(String searchBase, String searchFilter, int pageSize) {
		return search(searchBase, searchFilter, pageSize, new Extractor<LdapGroup>() {
			@Override
			public LdapGroup extract(SearchResult result) throws NamingException {
				return extractGroup(result);
			}
		});
	}

	private <T> Collection<T> search(String searchBase, String searchFilter, int pageSize, Extractor<T> extractor) {
		if (searchBase == null)
			return Collections.emptyList();

		Collection<T> result = new HashSet<>();

		SearchControls controls = new SearchControls();
		controls.setSearchScope(SearchControls.SUBTREE_SCOPE);

		try {
			NamingEnumeration<SearchResult> enumeration = pageSize == 0 ? context.search(searchBase, searchFilter, controls) : new LdapSearchEnumeration(context, searchBase, searchFilter, pageSize, controls);
			while (enumeration.hasMore())
				result.add(extractor.extract(enumeration.next()));
		} catch (NamingException e) {
			throw new RuntimeException(e);
		}

		return result;
	}

	private LdapUser extractUser(SearchResult result) throws NamingException {
		Attributes attributes = result.getAttributes();

		LdapUser ldapUser = new LdapUser();
		ldapUser.setDomainName(result.getNameInNamespace());
		ldapUser.setLogin(LdapAPI.getAttributeValue(attributes.get(usersLoginField)));

		Attribute expires = attributes.get(usersExpiresField);
		long ldapTimeStamp = expires != null ? Long.parseLong((String) expires.get()) : -1L;

		ldapUser.setLocked(ldapTimeStamp > 0 && LdapAPI.ldap2Date(ldapTimeStamp).before(new Date()));

		Attribute memberOf = usersMemberOfField != null ? attributes.get(usersMemberOfField) : null;

		if (memberOf != null) {
			for (int i = 0; i < memberOf.size(); i++)
				ldapUser.addMemberOf((String) memberOf.get(i));
		} else {
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

		Attribute members = groupsMemberField != null ? attributes.get(groupsMemberField) : null;

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
