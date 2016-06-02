package org.zenframework.z8.server.security;

import org.zenframework.z8.server.types.guid;

public enum SecurityGroup {
    Users(guids.Users),
    Administrators(guids.Administrators);

    static public class strings {
        static public final String Users = "SecurityGroup.users";
        static public final String Administrators = "SecurityGroup.administrators";
    }

    static class guids {
        static protected final guid Users = new guid("421A8413-A8EC-4DAB-9235-9A5FF83341C5");
        static protected final guid Administrators = new guid("DC08CA72-C668-412F-91B7-022F1C82AC09");
    }

    private guid id = null;

    SecurityGroup(guid id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id.toString();
    }

    public guid guid() {
        return id;
    }

    static public SecurityGroup fromGuid(guid guid) {
        if(guids.Users.equals(guid)) {
            return SecurityGroup.Users;
        }
        else if(guids.Administrators.equals(guid)) {
            return SecurityGroup.Administrators;
        }
        else {
            throw new RuntimeException("Unknown security group: '" + guid + "'");
        }
    }

}
