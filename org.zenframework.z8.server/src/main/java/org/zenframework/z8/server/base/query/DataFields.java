package org.zenframework.z8.server.base.query;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.runtime.CLASS;
import org.zenframework.z8.server.runtime.RCollection;

public class DataFields extends RCollection<Field.CLASS<? extends Field>> {
    private static final long serialVersionUID = -434029387095730997L;

    Query owner = null;

    public DataFields(Query owner) {
        super(true);

        this.owner = owner;
    }

    @Override
    public boolean add(Field.CLASS<? extends Field> field) {
        field.get(CLASS.Constructor2).setOwner(this.owner);
        return super.add(field);
    }

    @Override
    public void add(int index, Field.CLASS<? extends Field> field) {
        field.get(CLASS.Constructor2).setOwner(this.owner);
        super.add(index, field);
    }

    @Override
    public boolean addAll(int index, Collection<? extends Field.CLASS<? extends Field>> fields) {
        for(Field.CLASS<? extends Field> field : fields) {
            field.get(CLASS.Constructor2).setOwner(this.owner);
        }

        return super.addAll(index, fields);
    }
}
