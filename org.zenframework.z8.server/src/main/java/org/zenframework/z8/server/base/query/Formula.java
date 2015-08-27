package org.zenframework.z8.server.base.query;

import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.types.sql.sql_primary;

public class Formula extends OBJECT {
    public static class CLASS<T extends Formula> extends OBJECT.CLASS<T> {
        public CLASS() {
            this(null);
        }

        public CLASS(IObject container) {
            super(container);
            setJavaClass(Formula.class);
            setAttribute(Native, Formula.class.getCanonicalName());
        }

        @Override
        public Object newObject(IObject container) {
            return new Formula(container);
        }
    }

    public Field.CLASS<? extends Field> field;
    public sql_primary formula;

    protected Formula(IObject container) {
        super(container);
    }

    static public Formula.CLASS<? extends Formula> z8_create(Field.CLASS<? extends Field> field, sql_primary method) {
        Formula.CLASS<Formula> formula = new Formula.CLASS<Formula>();
        formula.get().field = field;
        formula.get().formula = method;
        return formula;
    }
}
