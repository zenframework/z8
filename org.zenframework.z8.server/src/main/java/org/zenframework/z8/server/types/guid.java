package org.zenframework.z8.server.types;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.UUID;

import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.exceptions.UnsupportedException;
import org.zenframework.z8.server.types.sql.sql_guid;

public class guid extends primary implements Serializable {
    private static final long serialVersionUID = 57247032014966596L;

    private static final UUID nullValue = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private UUID m_value;

    static final public guid NULL = new guid() {
        private static final long serialVersionUID = -7903861384935876679L;

        @Override
        public void set(String guid) {
            throw new UnsupportedException();
        }
    };

    public guid() {
        m_value = nullValue;
    }

    public guid(guid guid) {
        set(guid != null ? guid.m_value : nullValue);
    }
    
    public guid(UUID guid) {
        set(guid != null ? guid : nullValue);
    }

    public guid(String guid) {
        if (guid != null) {
            set(guid);
        } else {
            set(nullValue);
        }
    }

    public guid(byte[] data) {
        BigInteger ui = new BigInteger(data);
        set(ui.toString(16));
    }

    public boolean isNull() {
        return equals(guid.NULL);
    }

    static public guid create() {
        return new guid(UUID.randomUUID());
    }
    
    static public guid create(long n1, long n2) {
        return new guid(new UUID(n1, n2));
    }

    static public guid z8_create() {
        return create();
    }
    
    static public guid z8_create(integer n1, integer n2) {
        return create(n1.get(), n2.get());
    }

    @Override
    public guid defaultValue() {
        return new guid();
    }

    @Override
    public String toString() {
        return m_value.toString().toUpperCase();
    }

    public String toString(boolean useDelimiter) {
        return (useDelimiter ? m_value.toString() : m_value.toString().replace("-", "")).toUpperCase();
    }

    public UUID get() {
        return m_value;
    }

    public void set(UUID value) {
        m_value = value;
    }

    public void set(guid value) {
        set(value != null ? value.m_value : null);
    }

    public void set(String value) {
        if(value == null || value.trim().equals("") || value.trim().equals("0")) {
            m_value = nullValue;
        }
        else {
            if(value.length() == 32) {
                value = value.substring(0, 8) + "-" + value.substring(8, 12) + "-" + value.substring(12, 16) + "-"
                        + value.substring(16, 20) + "-" + value.substring(20, 32);
            }
            m_value = UUID.fromString(value);
        }
    }

    @Override
    public FieldType type() {
        return FieldType.Guid;
    }

    @Override
    public String toDbConstant(DatabaseVendor dbtype) {
        switch(dbtype) {
        case Oracle:
            return "HEXTORAW('" + toString(false) + "')";
        case Postgres:
        case SqlServer:
        default:
            return "'" + toString(true) + "'";
        }
    }

    @Override
    public String toDbString(DatabaseVendor dbtype) {
        switch(dbtype) {
        case Oracle:
            return toString(false);
        default:
            return toString(true);
        }
    }

    @Override
    public int hashCode() {
        return m_value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof guid) {
            return operatorEqu((guid)obj).get();
        }
        return false;
    }

    public sql_guid sql_guid() {
        return new sql_guid(this);
    }

    public void operatorAssign(guid value) {
        set(value);
    }

    public bool operatorEqu(guid x) {
        return new bool(m_value.equals(x.m_value));
    }

    public bool operatorNotEqu(guid x) {
        return new bool(!m_value.equals(x.m_value));
    }

    static public guid z8_parse(string string) {
        return new guid(string != null ? string.get() : null);
    }
}
