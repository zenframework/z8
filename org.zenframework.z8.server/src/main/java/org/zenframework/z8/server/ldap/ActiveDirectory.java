package org.zenframework.z8.server.ldap;

import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.exceptions.AccessDeniedException;
import org.zenframework.z8.server.logs.Trace;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import java.util.Date;
import java.util.Hashtable;

public class ActiveDirectory {
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
        return new InitialDirContext(environment);
    }

    public LdapUser searchUser(String searchBase, String searchFilter) throws NamingException {
        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        controls.setCountLimit(1);

        NamingEnumeration<SearchResult> namingEnumeration = context.search(searchBase, searchFilter, controls);

        if (namingEnumeration.hasMore()) {
            Attributes attributes = namingEnumeration.next().getAttributes();
            return extractUser(attributes);
        } else {
            Trace.logEvent(String.format("Active directory returned empty result. Query details: %s", searchFilter));
            throw new AccessDeniedException();
        }
    }

    private LdapUser extractUser(Attributes attributes) throws NamingException {
        long ldapTimeStamp = Long.parseLong((String) attributes.get("accountExpires").get());
        if (ldapTimeStamp > 0) {
            Date accountExpiredDate = ActiveDirectory.ldap2Date(ldapTimeStamp);
            if (accountExpiredDate.before(new Date())) {
                throw new AccessDeniedException();
            }
        }
        LdapUser ldapUser = new LdapUser();
        ldapUser.setEmail((String) attributes.get("mail").get());
        ldapUser.setLogin((String) attributes.get("sAMAccountname").get());
        ldapUser.setFullName((String) attributes.get("cn").get());
        Attribute groups = attributes.get("memberof");
        String[] groupNames = new String[groups.size()];
        for (int i = 0; i < groups.size(); i++) {
            groupNames[i] = (String) groups.get(i);
        }
        ldapUser.setMemberOf(groupNames);

        NamingEnumeration<String> keyNamesEnum = attributes.getIDs();
        while (keyNamesEnum.hasMore()) {
            String keyName = keyNamesEnum.next();
            Attribute attribute = attributes.get(keyName);
            // grab only single attrs
            if (attribute.size() == 1){
                ldapUser.getParameters().put(keyName, (String) attribute.get());
            }
        }
        return ldapUser;
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
