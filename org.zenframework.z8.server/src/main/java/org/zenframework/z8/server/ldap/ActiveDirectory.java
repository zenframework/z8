package org.zenframework.z8.server.ldap;

import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.exceptions.AccessDeniedException;
import org.zenframework.z8.server.logs.Trace;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import java.util.*;

public class ActiveDirectory {
    static public final String ldapParametersPrefix = "ldap_";
    private final DirContext context;

    public ActiveDirectory() throws NamingException {
        context = createConnection(
                ServerConfig.ldapUrl(),
                ServerConfig.principalName(),
                ServerConfig.credentials());
    }

    public InitialDirContext createConnection(String ldapUrl, String principalName, String credentials) throws NamingException {
        Hashtable<String, String> environment = new Hashtable<>();
        environment.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        environment.put(Context.PROVIDER_URL, ldapUrl);
        environment.put(Context.SECURITY_AUTHENTICATION, "simple");
        environment.put(Context.SECURITY_PRINCIPAL, principalName);
        environment.put(Context.SECURITY_CREDENTIALS, credentials);
        environment.put(Context.REFERRAL, "follow");
        return new InitialDirContext(environment);
    }

    public LdapUser searchUser(String searchBase, String searchFilter) throws NamingException {
        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        controls.setCountLimit(1);

        NamingEnumeration<SearchResult> namingEnumeration = context.search(searchBase, searchFilter, controls);

        if (namingEnumeration.hasMore()) {
            Attributes attributes = namingEnumeration.next().getAttributes();
            LdapUser ldapUser = extractUser(attributes);
            ldapUser.getParameters().putAll(extractLdapParameters(attributes));
            if (ldapUser.isLocked()) {
                throw new AccessDeniedException();
            }
            return ldapUser;
        } else {
            Trace.logEvent(String.format("Active directory returned empty result. Query details: %s", searchFilter));
            throw new AccessDeniedException();
        }
    }

    public List<LdapUser> getUsers(String searchBase, String searchFilter) throws NamingException {
        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        NamingEnumeration<SearchResult> namingEnumeration = context.search(searchBase, searchFilter, controls);

        List<LdapUser> result = new ArrayList<>();
        while (namingEnumeration.hasMore()) {
            Attributes attributes = namingEnumeration.next().getAttributes();
            LdapUser ldapUser = extractUser(attributes);
            ldapUser.getParameters().putAll(extractLdapParameters(attributes));
            result.add(ldapUser);
        }
        return result;
    }

    public List<Map<String, String>> getGroups(String searchBase, String searchFilter) throws NamingException {
        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        NamingEnumeration<SearchResult> namingEnumeration = context.search(searchBase, searchFilter, controls);

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
            Date accountExpiredDate = ActiveDirectory.ldap2Date(ldapTimeStamp);
            if (accountExpiredDate.before(new Date())) {
                locked = Boolean.TRUE;
            }
        }
        ldapUser.setLocked(locked);
        ldapUser.getParameters().put(ActiveDirectory.ldapParametersPrefix + "parameters", Boolean.TRUE.toString());
        ldapUser.getParameters().put(ActiveDirectory.ldapParametersPrefix + "locked", locked.toString());
        ldapUser.setLogin(ActiveDirectory.getAttributeValue(attributes.get("sAMAccountname")));
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
                parameters.put(ActiveDirectory.ldapParametersPrefix + keyName, ActiveDirectory.getAttributeValue(attribute));
            }
        }
        return parameters;
    }

    private static String getAttributeValue(Attribute attribute) {
        if (attribute == null) {
            return null;
        } else {
            try {
                return (String) attribute.get();
            } catch (NamingException ignored) {
                return null;
            }
        }
    }

    private static Date ldap2Date(long ldapTimeStamp) { ;
        return new Date(ldapTimeStamp / 10000 - + 11644473600000L);
    }

    public void close() {
        try {
            context.close();
        } catch (NamingException ignored){}
    }
}
