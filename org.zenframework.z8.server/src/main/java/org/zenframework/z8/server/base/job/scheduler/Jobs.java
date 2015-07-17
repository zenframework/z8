package org.zenframework.z8.server.base.job.scheduler;

import java.util.LinkedHashMap;

import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.ie.TransportProcedure;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;

public class Jobs extends Table {
    final static public String TableName = "SystemJobs";

    static public class names {}

    static public class strings {
        public final static String Title = "Jobs.title";
    }

    public static class CLASS<T extends Jobs> extends Table.CLASS<T> {
        public CLASS() {
            this(null);
        }

        public CLASS(IObject container) {
            super(container);
            setJavaClass(Jobs.class);
            setName(TableName);
            setDisplayName(Resources.get(Jobs.strings.Title));
        }

        @Override
        public Object newObject(IObject container) {
            return new Jobs(container);
        }
    }

    public Jobs(IObject container) {
        super(container);
    }

    @Override
    public void constructor2() {
        super.constructor2();
        id.get().length.set(256);
        name.get().length.set(256);
        {
            LinkedHashMap<IField, primary> record = new LinkedHashMap<IField, primary>();
            record.put(name.get(), new string(TransportProcedure.class.getSimpleName()));
            record.put(id.get(), new string(TransportProcedure.class.getCanonicalName()));
            addRecord(TransportProcedure.PROCEDURE_ID, record);
        }
    }

}
