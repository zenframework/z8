package org.zenframework.z8.server.security;

import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.runtime.RLinkedHashMap;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;

public class UserInfo extends OBJECT {
    public static class CLASS<T extends UserInfo> extends OBJECT.CLASS<T> {
        public CLASS() {
            this(null);
        }

        public CLASS(IObject container) {
            super(container);
            setJavaClass(UserInfo.class);
            setAttribute(Native, UserInfo.class.getCanonicalName());
        }

        @Override
        public Object newObject(IObject container) {
            return new UserInfo(container);
        }
    }

    public guid id = new guid();
    public string name = new string();
    public guid company = new guid();

    public string description = new string();
    public string phone = new string();
    public string email = new string();

    public SecurityGroup securityGroup = SecurityGroup.Users;
    
    public RLinkedHashMap<string, primary> parameters = new RLinkedHashMap<string, primary>();

    public UserInfo(IObject container) {
        super(container);
    }

    public void initialize(IUser user) {
        if(user == null)
            user = User.system();

        setValues(user);
    }

    private void setValues(IUser user) {
        if(user == null)
            user = User.system();

        id = new guid(user.id());
        name = new string(user.name());

        description = new string(user.description());
        phone = new string(user.phone());
        email = new string(user.email());

        securityGroup = user.securityGroup();
        parameters.putAll(user.parameters());
    }
}
