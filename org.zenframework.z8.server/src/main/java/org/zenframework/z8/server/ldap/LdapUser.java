package org.zenframework.z8.server.ldap;

import java.util.HashMap;
import java.util.Map;

public class LdapUser {
    private String login;
    private String[] memberOf;
    private boolean locked;
    private Map<String, String> parameters;

    public LdapUser() {
        parameters = new HashMap<>();
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String[] getMemberOf() {
        return memberOf;
    }

    public void setMemberOf(String[] memberOf) {
        this.memberOf = memberOf;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }
}
