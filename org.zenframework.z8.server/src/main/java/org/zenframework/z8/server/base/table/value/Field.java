package org.zenframework.z8.server.base.table.value;

import java.sql.SQLException;
import java.util.Collection;

import org.zenframework.z8.server.base.form.Control;
import org.zenframework.z8.server.base.form.FieldGroup;
import org.zenframework.z8.server.base.model.sql.Select;
import org.zenframework.z8.server.base.query.Formula;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SortDirection;
import org.zenframework.z8.server.db.sql.SqlConst;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.db.sql.functions.IsNull;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.runtime.RLinkedHashMap;
import org.zenframework.z8.server.types.binary;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.datespan;
import org.zenframework.z8.server.types.datetime;
import org.zenframework.z8.server.types.decimal;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.types.sql.sql_bool;
import org.zenframework.z8.server.types.sql.sql_primary;

abstract public class Field extends Control implements IValue, IField {
    public static class strings {
        public final static String ReadException = "Field.readException";
    }

    public static class CLASS<T extends Field> extends Control.CLASS<T> {
        public CLASS(IObject container) {
            super(container);
            setJavaClass(Field.class);
            setAttribute(Native, Field.class.getCanonicalName());
        }
    }

    public bool system = new bool(false);
    public bool visible = new bool(true);
    public bool hidden = new bool(false);
    public bool readOnly = new bool(false);
    public bool selectable = new bool(true);
    public string format = new string();
    public integer length = new integer();

    public integer width = new integer();
    public integer column = new integer();
    public integer columnWidth = new integer();
    public integer labelWidth = new integer();
    public bool stretch = new bool(true);

    public bool anchor = new bool(false);
    public FollowPolicy anchorPolicy = FollowPolicy.Default;

    public SortDirection sortDirection = SortDirection.Asc;
    public Aggregation aggregation = Aggregation.None;

    public Query.CLASS<? extends Query> editWith = null;

    public bool required = new bool(false);
    public bool indexed = new bool(false);
    public bool unique = new bool(false);

    public RCollection<Field.CLASS<? extends Field>> indexFields = new RCollection<Field.CLASS<? extends Field>>(true);

    public RCollection<Formula.CLASS<? extends Formula>> evaluations = new RCollection<Formula.CLASS<? extends Formula>>(
            true);

    public RCollection<Field.CLASS<? extends Field>> dependencies = new RCollection<Field.CLASS<? extends Field>>(true);
    public RCollection<Field.CLASS<? extends Field>> columns = new RCollection<Field.CLASS<? extends Field>>(true);
    public RLinkedHashMap<guid, RCollection<Control.CLASS<? extends Control>>> fieldsToShow = new RLinkedHashMap<guid, RCollection<Control.CLASS<? extends Control>>>();

    private primary value = null;
    private primary originalValue = null;
    private boolean changed = false;

    private Sequencer sequencer = null;

    private Select cursor = null;
    private Query owner = null;

    public int position = -1;
    
    protected Field(IObject container) {
        super(container);
    }

    /*
        public void operatorAssign(RField.CLASS<? extends RField> field)
    	{
    		set(field.get().get());
    	}
    */

    @SuppressWarnings("unchecked")
    public Field.CLASS<Field> operatorAssign(primary value) {
        set(value);
        return (Field.CLASS<Field>) getCLASS();
    }

    @Override
    public String toString() {
        return get().toString();
    }

    @Override
    public String toDebugString() {
        return super.toString();
    }

    @Override
    final public primary getDefaultValue() {
        return value;
    }

    public primary getDefault() {
        return value;
    }

    public void setDefault(primary value) {
        this.value = value;
    }

    public boolean isAggregated() {
        return aggregation != Aggregation.None;
    }
    
    public Aggregation getAggregation() {
        return aggregation;
    }

    public void setOwner(Query owner) {
        this.owner = owner;
    }

    public Query getOwner() {
        return owner;
    }

    public Collection<Field.CLASS<? extends Field>> columns() {
        return columns;
    }

    public Collection<Field> getColumns() {
        return CLASS.asList(columns());
    }

    public final void setCursor(Select cursor) {
        this.cursor = cursor;
    }

    public String qualifiedName() {
        return (getContainer() != null ? "[" + getContainer().displayName() + "]" : "") + displayName();
    }

    public boolean isPrimaryKey() {
        return hasAttribute(IObject.PrimaryKey);
    }

    public boolean isParentKey() {
        return hasAttribute(IObject.ParentKey);
    }

    public boolean isDataField() {
        return !isPrimaryKey() && !isParentKey();
    }

    public String format(DatabaseVendor vendor, FormatOptions options) {
        String alias = options.getFieldAlias(this);

        if(alias == null) {
            Query data = getOwner();
            return data.getAlias() + '.' + vendor.quote(name());
        }

        return alias;
    }

    public SqlConst toSqlConst() {
        return new SqlConst(get());
    }

    @Override
    public boolean isNull() {
        return get() == null;
    }

    protected Select getCursor() {
        return cursor;
    }

    protected primary read() throws SQLException {
        return cursor.get(this);
    }

    protected primary internalGet() {
        try {
            return (changed || cursor == null || cursor.isClosed()) ? getDefaultValue() : read();
        }
        catch(SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    abstract public primary get();

    @Override
    public void set(primary value) {
        if(!changed) {
            originalValue = this.value;
            changed = true;
        }
        setDefault(primary.clone(value));
    }

    @Override
    public boolean changed() {
        return changed;
    }

    public void reset() {
        if(changed) {
            value = originalValue;
            changed = false;
        }
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public int scale() {
        return 0;
    }

    public int width() {
        return width.getInt();
    }

    public Collection<Formula.CLASS<? extends Formula>> evaluations() {
        return evaluations;
    }

    public Collection<Formula> getEvaluations() {
        return CLASS.asList(evaluations());
    }

    public Sequencer getSequencer() {
        if(sequencer == null) {
            Sequencer.CLASS<Sequencer> cls = new Sequencer.CLASS<Sequencer>(this);
            sequencer = cls.get();

            String containerName = getContainer().getAttribute(Name);

            if(containerName == null) {
                containerName = getContainer().classId();
            }

            sequencer.setKey(containerName + '.' + name());
        }
        return sequencer;
    }

    @Override
    public void writeMeta(JsonObject writer) {
        super.writeMeta(writer);

        writer.put(Json.serverType, type().toString());
        writer.put(Json.visible, visible);
        writer.put(Json.hidden, hidden);
        writer.put(Json.format, format);
        writer.put(Json.length, length);
        writer.put(Json.anchor, anchor);
        writer.put(Json.anchorPolicy, anchorPolicy.toString());

        writer.put(Json.width, width);
        writer.put(Json.column, column);
        writer.put(Json.columnWidth, columnWidth);
        writer.put(Json.labelWidth, labelWidth);
        writer.put(Json.stretch, stretch);

        if(aggregation != Aggregation.None)
            writer.put(Json.aggregation, aggregation.toString());

        if(!evaluations.isEmpty()) {
            JsonArray evalsArr = new JsonArray();

            for(Formula formula : getEvaluations()) {
                JsonObject evalObj = new JsonObject();

                evalObj.put(Json.field, formula.field.id());
                evalObj.put(Json.formula, formula.formula.formula());

                Collection<IValue> fields = formula.formula.getUsedFields();

                JsonArray fieldsArr = new JsonArray();
                for(IValue field : fields) {
                    fieldsArr.put(field.id());
                }
                evalObj.put(Json.fields, fieldsArr);

                evalsArr.put(evalObj);
            }

            writer.put(Json.evaluations, evalsArr);
        }

        if(!dependencies.isEmpty()) {
            JsonArray depsArr = new JsonArray();

            for(Field.CLASS<?> cls : dependencies) {
                depsArr.put(cls.id());
            }

            writer.put(Json.dependencies, depsArr);
        }

        if(!fieldsToShow.isEmpty()) {
            JsonObject fieldsObj = new JsonObject();

            for(guid key : fieldsToShow.keySet()) {
                JsonArray arr = new JsonArray();

                for(Control.CLASS<?> field : fieldsToShow.get(key)) {
                    if(field.instanceOf(FieldGroup.class)) {
                        FieldGroup group = (FieldGroup)field.get();

                        for(Control.CLASS<?> groupField : group.controls) {
                            arr.put(groupField.id());
                        }
                    }

                    arr.put(field.id());
                }

                fieldsObj.put(key.toString(), arr);
            }

            writer.put(Json.fieldsToShow, fieldsObj);
        }
    }

    public void writeData(JsonObject writer) {
        writer.put(id(), get());
    }

    public sql_primary formula() {
        return null;
    }

    public binary binary() {
        return (binary)get();
    }

    public bool bool() {
        return (bool)get();
    }

    public guid guid() {
        return (guid)get();
    }

    public date date() {
        return (date)get();
    }

    public datetime datetime() {
        return (datetime)get();
    }

    public datespan datespan() {
        return (datespan)get();
    }

    public decimal decimal() {
        return (decimal)get();
    }

    public integer integer() {
        return (integer)get();
    }

    public string string() {
        return (string)get();
    }

    public FieldType z8_getType() {
        return type();
    }

    public string z8_getTypeName() {
        return new string(type().toString());
    }

    @Override
    public string z8_toString() {
        return new string(toString());
    }

    public primary z8_primary() {
        return get();
    }

    public binary z8_binary() {
        return binary();
    }

    public bool z8_bool() {
        return bool();
    }

    public guid z8_guid() {
        return guid();
    }

    public date z8_date() {
        return date();
    }

    public datetime z8_datetime() {
        return datetime();
    }

    public datespan z8_datespan() {
        return datespan();
    }

    public decimal z8_decimal() {
        return decimal();
    }

    public integer z8_int() {
        return integer();
    }

    public string z8_string() {
        return string();
    }

    public void z8_setDefault(primary value) {
        setDefault(value);
    }

    public bool z8_isNull() {
        return new bool(isNull());
    }

    public Sequencer.CLASS<? extends Sequencer> z8_getSequencer() {
        return (Sequencer.CLASS<?>)getSequencer().getCLASS();
    }

    public sql_bool z8_sqlIsNull() {
        return new sql_bool(new IsNull(new SqlField(this)));
    }
}
