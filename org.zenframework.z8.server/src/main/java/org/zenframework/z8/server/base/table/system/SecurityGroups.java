package org.zenframework.z8.server.base.table.system;

import java.util.LinkedHashMap;

import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.security.SecurityGroup;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;

public class SecurityGroups extends Table {
    final static public String TableName = "SystemSecurityGroups";

    static public class strings {
        public final static String Title = "SecurityGroups.title";
        public final static String Name = "SecurityGroups.name";
    }

    public static class CLASS<T extends SecurityGroups> extends Table.CLASS<T> {
        public CLASS(IObject _container) {
            super(_container);
            setJavaClass(SecurityGroups.class);
            setName(SecurityGroups.TableName);
            setDisplayName(Resources.get(strings.Title));
        }

        @Override
        public Object newObject(IObject container) {
            return new SecurityGroups(container);
        }
    }

    public SecurityGroups(IObject container) {
        super(container);
    }

    @Override
    public void constructor2() {
        super.constructor2();

        name.setDisplayName(Resources.get(strings.Name));

        id.get().visible = new bool(false);
        id1.get().visible = new bool(false);

        name.get().length = new integer(50);

        {
            LinkedHashMap<IField, primary> values = new LinkedHashMap<IField, primary>();
            values.put(name.get(), new string(Resources.get(SecurityGroup.strings.Administrators)));
            values.put(description.get(), new string(Resources.get(SecurityGroup.strings.Administrators)));
            addRecord(SecurityGroup.Administrators.guid(), values);
        }
        {
            LinkedHashMap<IField, primary> values = new LinkedHashMap<IField, primary>();
            values.put(name.get(), new string(Resources.get(SecurityGroup.strings.Users)));
            values.put(description.get(), new string(Resources.get(SecurityGroup.strings.Users)));
            addRecord(SecurityGroup.Users.guid(), values);
        }
    }
}
