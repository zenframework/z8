package org.zenframework.z8.server.base.table.system.view;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.system.UserEntries;
import org.zenframework.z8.server.base.table.system.Users;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.security.SecurityGroup;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.integer;

public class UserEntriesView extends Query {
    static public class names {
        public final static String UserEntries = "UserEntries";
    }
    
    static public class strings {
        public final static String Title = "UserEntryPoints.title";
    }

    public static class CLASS<T extends UserEntriesView> extends Query.CLASS<T> {
        public CLASS(IObject container) {
            super(container);
            setJavaClass(UserEntriesView.class);
            setDisplayName(Resources.get(strings.Title));
        }

        @Override
        public Object newObject(IObject container) {
            return new UserEntriesView(container);
        }
    }

    public UserEntries.CLASS<UserEntries> userEntries = new UserEntries.CLASS<UserEntries>(this);

    public UserEntriesView(IObject container) {
        super(container);
    }

    @Override
    public void constructor2() {
        super.constructor2();

        userEntries.setIndex("userEntries");
        
        Users.CLASS<Users> users = userEntries.get().users;

        queries.add(userEntries);

        model = users;
        model.setDisplayName(Resources.get(strings.Title));

        registerFormField(users.get().name);
        registerFormField(users.get().description);

        if(getUser().securityGroup() == SecurityGroup.Administrators) {
            registerFormField(users.get().password);
        }
        else {
            users.get().readOnly.set(true);
            readOnly.set(true);
        }

        registerFormField(users.get().phone);
        registerFormField(users.get().email);
        registerFormField(users.get().blocked);

        registerFormField(users.get().securityGroups.get().name);

        registerFormField(userEntries.get().entries.get().id);
        registerFormField(userEntries.get().entries.get().name);
        registerFormField(userEntries.get().position);

        userEntries.get().entries.get().name.get().stretch = new bool(false);
        userEntries.get().entries.get().name.get().width = new integer(50);
        userEntries.get().position.get().width = new integer(15);

        users.get().sortFields.add(users.get().name);

        userEntries.get().sortFields.add(userEntries.get().position);
    }
}
