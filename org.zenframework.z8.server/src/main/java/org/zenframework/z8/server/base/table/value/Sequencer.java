package org.zenframework.z8.server.base.table.value;

import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.base.table.system.Sequences;
import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.expressions.Operation;
import org.zenframework.z8.server.db.sql.expressions.Rel;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.types.sql.sql_string;

public class Sequencer extends OBJECT {
    final static public String ProcedureName = "NextInSequence";

    public static class CLASS<T extends Sequencer> extends OBJECT.CLASS<T> {
        public CLASS(IObject container) {
            super(container);
            setJavaClass(Sequencer.class);
            setAttribute(Native, Sequencer.class.getCanonicalName());
        }

        @Override
        public Object newObject(IObject container) {
            return new Sequencer(container);
        }
    }

    private static final Map<String, Object> syncs = new HashMap<String, Object>();

    private String key = null;

    private integer defaultValue = new integer(1);
    private integer increment = new integer(1);

    public Sequencer(IObject container) {
        super(container);
    }

    static public void reset(String key) {
        Sequences sequences = new Sequences.CLASS<Sequences>().get();

        SqlToken where = new Rel(sequences.id.get(), Operation.Eq, new sql_string(key.toLowerCase()));
        sequences.destroy(where);
    }

    static private long next(String key, long defaultValue, long increment) {
        synchronized (getSync(key)) {
            Connection connection = ConnectionManager.get();

            String id = "id" + Integer.toString(key.hashCode()).replace('-', '_');

            Sequences sequences = new Sequences.CLASS<Sequences>().get();
            IntegerField valueField = sequences.value.get();
            StringField idField = sequences.id.get();
            TextField descriptionField = sequences.description.get();

            SqlToken where = new Rel(sequences.id.get(), Operation.Eq, new sql_string(id));

            long result = defaultValue;

            try {
                connection.beginTransaction();

                if (sequences.readFirst(where)) {
                    result = valueField.integer().get() + Math.max(increment, 1);

                    valueField.set(new integer(result));
                    sequences.update(sequences.recordId());
                } else {
                    idField.set(new string(id));
                    valueField.set(new integer(result));
                    descriptionField.set(new string(key));
                    sequences.create();
                }

                connection.commit();
            } catch (Throwable e) {
                connection.rollback();
                throw new RuntimeException(e);
            }

            return result;
        }
    }

    private synchronized static Object getSync(String key) {
        Object sync = syncs.get(key);
        if (sync == null) {
            sync = new Object();
            syncs.put(key, sync);
        }
        return sync;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public long next() {
        return next(key, defaultValue.get(), increment.get());
    }

    public void reset() {
        reset(key);
    }

    public integer z8_next() {
        return z8_next(new string(key), new integer(1), new integer(1));
    }

    public integer z8_next(integer defaultValue) {
        return z8_next(new string(key), defaultValue, new integer(1));
    }

    public integer z8_next(integer defaultValue, integer increment) {
        return z8_next(new string(key), defaultValue, increment);
    }

    static public integer z8_next(string key) {
        return z8_next(key, new integer(1));
    }

    static public integer z8_next(string key, integer defaultValue) {
        return z8_next(key, new integer(defaultValue.getInt()), new integer(1));
    }

    static public integer z8_next(string key, integer defaultValue, integer increment) {
        return new integer(next(key.get(), defaultValue.get(), increment.get()));
    }

    public void z8_reset() {
        reset(key);
    }

    static public void z8_reset(string key) {
        reset(key.get());
    }
}
