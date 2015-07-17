package org.zenframework.z8.server.base.table.system;

import java.util.LinkedHashMap;

import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.exception;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;

public class Entries extends Table {
    final static public String TableName = "SystemEntries";

    static public class names {
        public final static String Granted = "Granted";
    }

    static public class strings {
        public final static String Title = "Entries.title";
        public final static String Id = "Entries.javaClass";
        public final static String Name = "Entries.name";
        public final static String Granted = "Entries.granted";
    }

    public static class CLASS<T extends Entries> extends Table.CLASS<T> {
        public CLASS() {
            this(null);
        }

        public CLASS(IObject container) {
            super(container);
            setJavaClass(Entries.class);
            setName(TableName);
            setDisplayName(Resources.get(strings.Title));
        }

        @Override
        public Object newObject(IObject container) {
            return new Entries(container);
        }
    }

    public Entries(IObject container) {
        super(container);

        id.setDisplayName(Resources.get(strings.Id));
        name.setDisplayName(Resources.get(strings.Name));
    }

    @Override
    public void constructor2() {
        super.constructor2();

        readOnly.set(true);

        id.get().visible = new bool(false);
        id.get().length.set(1024);
        id.get().visible = new bool(false);

        id1.get().visible = new bool(false);

        name.get().length.set(1024);

        description.get().visible = new bool(false);

        {
            LinkedHashMap<IField, primary> record = new LinkedHashMap<IField, primary>();
            record.put(name.get(), new string(Resources.get(SystemTools.strings.Title)));
            record.put(id.get(), new string(SystemTools.class.getCanonicalName()));
            addRecord(SystemTools.Id, record);
        }
    }

    @Override
    public void z8_destroy(guid id) {
        if(id.equals(SystemTools.Id.guid())) {
            throw new exception("Unable to delete builtin system entrypoint !");
        }

        super.z8_destroy(id);
    }

}
