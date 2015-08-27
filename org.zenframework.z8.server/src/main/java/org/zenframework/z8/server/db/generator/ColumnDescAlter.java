package org.zenframework.z8.server.db.generator;

import org.zenframework.z8.server.base.table.value.Field;

class ColumnDescAlter {
    Field field;
    FieldAction action;
    boolean nullable;

    ColumnDescAlter(Field field, FieldAction action, boolean nullable) {
        this.field = field;
        this.action = action;
        this.nullable = nullable;
    }
}
