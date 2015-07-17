package org.zenframework.z8.server.security;

import java.io.Serializable;

import org.zenframework.z8.server.types.guid;

public enum SecurityGroup implements Serializable {
    Users(names.Users),
    Administrators(names.Administrators);

    static public class strings {
        static public final String Users = "SecurityGroup.users";
        static public final String Administrators = "SecurityGroup.administrators";
    }

    class names {
        static protected final String Users = "421A8413-A8EC-4DAB-9235-9A5FF83341C5";
        static protected final String Administrators = "DC08CA72-C668-412F-91B7-022F1C82AC09";
    }

    private String fName = null;

    SecurityGroup(String name) {
        fName = name;
    }

    @Override
    public String toString() {
        return fName;
    }

    public guid guid() {
        return new guid(fName);
    }

    static public SecurityGroup fromGuid(guid guid) {
        return fromString(guid.toString());
    }

    static public SecurityGroup fromString(String string) {
        if(names.Users.equals(string)) {
            return SecurityGroup.Users;
        }
        else if(names.Administrators.equals(string)) {
            return SecurityGroup.Administrators;
        }
        else {
            throw new RuntimeException("Unknown security group: '" + string + "'");
        }
    }
}
