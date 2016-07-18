package org.zenframework.z8.server.base.table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.Expression;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.base.table.value.Link;
import org.zenframework.z8.server.db.generator.IForeignKey;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.runtime.RLinkedHashMap;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.primary;

public class TableBase extends Query implements ITable {
    public static class CLASS<T extends TableBase> extends Query.CLASS<T> {
        public CLASS() {
            this(null);
        }

        public CLASS(IObject container) {
            super(container);
            setJavaClass(TableBase.class);
        }

        @Override
        public Object newObject(IObject container) {
            return new TableBase(container);
        }
    }

    protected RCollection<Link.CLASS<? extends Link>> links = new RCollection<Link.CLASS<? extends Link>>();

    private List<Map<IField, primary>> staticRecords = new ArrayList<Map<IField, primary>>();

    public TableBase(IObject container) {
        super(container);
    }

    @Override
    public String displayName() {
        String name = super.displayName();
        return name == null || name.isEmpty() ? name() : name;
    }

    @Override
    public Collection<Map<IField, primary>> getStaticRecords() {
        initStaticRecords();
        return staticRecords;
    }

    @Override
    public Collection<IForeignKey> getForeignKeys() {
        LinkedHashSet<IForeignKey> foreignKeys = new LinkedHashSet<IForeignKey>();
        for(Link.CLASS<? extends Link> link : links) {
            if(link.foreignKey())
                foreignKeys.add(link.get());
        }
        return foreignKeys;
    }

    @Override
    public Collection<IField> getIndices() {
        List<IField> result = new ArrayList<IField>();

        for(Field field : getDataFields()) {
            if((field.indexed() || !field.indexFields.isEmpty()) && !field.unique() && !(field instanceof Link)) {
                result.add(field);
            }
        }

        return result;
    }

    @Override
    public Collection<IField> getUniqueIndices() {
        List<IField> result = new ArrayList<IField>();

        for(Field field : getDataFields()) {
            if(field.unique() && !(field instanceof Link)) {
                result.add(field);
            }
        }

        return result;
    }

    @Override
    public void initStaticRecords() {}

    public void addRecord(guid key, Map<IField, primary> values) {
        for (Map<IField, primary> record : staticRecords) {
            if (key.equals(record.get(primaryKey())))
                return;
        }
        values.put(primaryKey(), key);
        staticRecords.add(values);
    }

    final protected void internalAddRecord(guid key, Map<IField, primary> values) {
        addRecord(key, values);
    }

    public List<Field> getTableFields() {
        List<Field> fields = new ArrayList<Field>();

        for(Field.CLASS<? extends Field> field : dataFields()) {
            if(!(field instanceof Expression.CLASS)) {
                fields.add(field.get());
            }
        }
        return fields;
    }

    public void z8_addRecord(guid recordId, RLinkedHashMap<Field.CLASS<? extends Field>, primary> values) {
        Map<IField, primary> vals = new HashMap<IField, primary>();
        for (Map.Entry<Field.CLASS<? extends Field>, primary> entry : values.entrySet()) {
            vals.put(entry.getKey().get(), entry.getValue());
        }
        addRecord(recordId, vals);
    }

}
