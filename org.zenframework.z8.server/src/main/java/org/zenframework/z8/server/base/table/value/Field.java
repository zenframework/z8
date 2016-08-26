package org.zenframework.z8.server.base.table.value;

import java.sql.SQLException;
import java.util.Collection;

import org.zenframework.z8.server.base.form.Control;
import org.zenframework.z8.server.base.model.sql.Select;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SortDirection;
import org.zenframework.z8.server.db.sql.SqlConst;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.db.sql.functions.IsNull;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.binary;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.datespan;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.decimal;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.types.sql.sql_bool;

abstract public class Field extends Control implements IValue, IField {
	public static class strings {
		public final static String ReadException = "Field.readException";
	}

	public static class CLASS<T extends Field> extends Control.CLASS<T> {

		public CLASS(IObject container) {
			super(container);
			setJavaClass(Field.class);
		}
	}

	public bool visible = null;
	public string format = null;
	public integer length = null;

	public integer width = null;
	public integer column = null;
	public integer columnWidth = null;
	public integer labelWidth = null;
	public bool stretch = null;

	public bool anchor = null;
	public FollowPolicy anchorPolicy = FollowPolicy.Default;

	public SortDirection sortDirection = SortDirection.Asc;
	public Aggregation aggregation = Aggregation.None;

	public Query.CLASS<? extends Query> editWith = null;

	public bool readOnly = null;
	public bool selectable = null;
	public bool required = null;
	
	public bool indexed = null;
	public bool unique = null;

	public RCollection<Field.CLASS<? extends Field>> indexFields = new RCollection<Field.CLASS<? extends Field>>();

	public RCollection<Field.CLASS<? extends Field>> columns = new RCollection<Field.CLASS<? extends Field>>();

	private primary value = null;
	private primary originalValue = null;
	private boolean changed = false;

	private Sequencer sequencer = null;

	private Select cursor = null;

	public int position = -1;

	protected Field(IObject container) {
		super(container);
	}

	/*
	 * public void operatorAssign(RField.CLASS<? extends RField> field) {
	 * set(field.get().get()); }
	 */

	@SuppressWarnings("unchecked")
	public Field.CLASS<Field> operatorAssign(primary value) {
		set(value);
		return (Field.CLASS<Field>)getCLASS();
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

	@Override
	public Query owner() {
		return (Query)getOwner();
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
			Query data = owner();
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
		} catch(SQLException e) {
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
		return width != null ? width.getInt() : 0;
	}

	public boolean indexed() {
		return indexed != null ? indexed.get() : false;
	}

	public boolean unique() {
		return unique != null ? unique.get() : false;
	}

	public boolean readOnly() {
		return readOnly != null ? readOnly.get() : false;
	}

	public boolean required() {
		return required != null ? required.get() : false;
	}

	public boolean selectable() {
		return selectable != null ? selectable.get() : true;
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
	public void writeMeta(JsonWriter writer) {
		super.writeMeta(writer);

		writer.writeProperty(Json.serverType, type().toString());
		writer.writeProperty(Json.visible, visible, new bool(true));
		writer.writeProperty(Json.format, format, new string());
		writer.writeProperty(Json.length, length, new integer(0));
		writer.writeProperty(Json.anchor, anchor, new bool(false));
		writer.writeProperty(Json.anchorPolicy, anchorPolicy.toString());

		writer.writeProperty(Json.width, width, new integer(0));
		writer.writeProperty(Json.column, column, new integer(0));
		writer.writeProperty(Json.columnWidth, columnWidth, new integer(0));
		writer.writeProperty(Json.labelWidth, labelWidth, new integer(0));
		writer.writeProperty(Json.stretch, stretch, new bool(true));

		if(aggregation != Aggregation.None)
			writer.writeProperty(Json.aggregation, aggregation.toString());
	}

	public void writeData(JsonWriter writer) {
		writer.writeProperty('"' + id() + '"', get());
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

	public bool z8_isChanged() {
		return new bool(changed());
	}

	public Sequencer.CLASS<? extends Sequencer> z8_getSequencer() {
		return (Sequencer.CLASS<?>)getSequencer().getCLASS();
	}

	public sql_bool z8_sqlIsNull() {
		return new sql_bool(new IsNull(new SqlField(this)));
	}

	public int controlSum() {
		String name = name() + " " + sqlType(DatabaseVendor.Postgres);
		return Math.abs(name.hashCode());
	}

}
