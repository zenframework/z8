package org.zenframework.z8.server.base.table.system;

import org.zenframework.z8.server.base.query.ReadLock;
import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.IntegerField;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.security.SecurityGroup;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.integer;

public class Sequences extends Table {
    final static public String TableName = "SystemSequences";

    static public class names {
        public final static String Value = "Value";
    }

    static public class strings {
        public final static String Title = "Sequences.title";
        public final static String Value = "Sequences.value";
    }

    public static class CLASS<T extends Sequences> extends Table.CLASS<T> {
        public CLASS() {
            this(null);
        }

        public CLASS(IObject container) {
            super(container);
            setJavaClass(Sequences.class);
            setName(TableName);
            setDisplayName(Resources.get(strings.Title));
        }

        @Override
        public Object newObject(IObject container) {
            return new Sequences(container);
        }
    }

    public IntegerField.CLASS<IntegerField> value = new IntegerField.CLASS<IntegerField>(this);

    public Sequences(IObject container) {
        super(container);
    }

    @Override
    public void constructor2() {
        super.constructor2();

        value.setName(names.Value);
        value.setIndex("value");
        value.setDisplayName(Resources.get(strings.Value));

        readOnly.set(getUser().securityGroup() != SecurityGroup.Administrators);

        registerDataField(value);

        id.get().indexed.set(true);

        id1.get().visible = new bool(false);
        name.get().visible = new bool(false);

        id1.get().width = new integer(15);

        description.get().width = new integer(100);

        sortFields.add(description);
        
        readLock = ReadLock.Update;
    }
}
