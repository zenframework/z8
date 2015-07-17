package org.zenframework.z8.server.db.sql.functions.datetime;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IValue;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlConst;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.datetime;
import org.zenframework.z8.server.types.primary;

public class SystemTime extends SqlToken {
    public final static String systemTimeFunc = "SysDate()";

    private boolean withTime = false;

    public SystemTime(boolean _withTime) {
        withTime = _withTime;
    }

    @Override
    public void collectFields(Collection<IValue> fields) {}

    @Override
    public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
        assert (false);

        primary d = withTime ? new datetime() : new date();
        return new SqlConst(d).format(vendor, options, logicalContext);
    }

    @Override
    public FieldType type() {
        return (withTime ? FieldType.Datetime : FieldType.Date);
    }
}
