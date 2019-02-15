package org.zenframework.z8.server.base.table.value;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashSet;

import org.zenframework.z8.server.base.form.Control;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.Select;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SortDirection;
import org.zenframework.z8.server.db.sql.SqlConst;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.db.sql.expressions.UnaryNot;
import org.zenframework.z8.server.db.sql.functions.InVector;
import org.zenframework.z8.server.db.sql.functions.IsNull;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.security.IAccess;
import org.zenframework.z8.server.types.binary;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.datespan;
import org.zenframework.z8.server.types.decimal;
import org.zenframework.z8.server.types.geometry;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.types.sql.sql_bool;

abstract public class Field extends Control implements IField {
	public static class strings {
		public final static String ReadException = "Field.readException";
	}

	public static class CLASS<T extends Field> extends Control.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(Field.class);
		}
		
		@Override
		public String keyString() {
			IObject owner = getOwner();
			if(owner == null)
				return name();
			String name = owner.name();
			return (name != null ? name + "." : "") + name();
		}
	}

	public integer length;

	public string format;
	public integer width;
	public integer minWidth;

	public SortDirection sortDirection = SortDirection.Asc;
	public Aggregation aggregation = Aggregation.None;
	public bool totals = bool.True;

	public bool indexed;
	public bool unique;

	public primary defaultValue;

	public Field.CLASS<? extends Field> valueFrom;
	public Field.CLASS<? extends Field> valueFor;

	private primary originalValue;
	private boolean changed = false;

	private Collection<ILink> path;
	private boolean rightJoined = false;

	private Sequencer sequencer;

	private Select cursor;
	private boolean wasNull = true;
	private boolean writeNulls = true;

	public int position = -1;

	private IAccess access;

	private bool isPrimaryKey;
	private bool isParentKey;
	public RCollection<Field.CLASS<? extends Field>> columns = new RCollection<Field.CLASS<? extends Field>>();

	private boolean isWritingMeta;

	protected Field(IObject container) {
		super(container);
	}

	@Override
	public String displayName() {
		String name = super.displayName();

		if(name != null && !name.isEmpty())
			return name;

		name = name();
		return name == null || name.isEmpty() ? id() : name;
	}

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
		return defaultValue;
	}

	public primary getDefault() {
		return defaultValue;
	}

	public void setDefault(primary value) {
		this.defaultValue = value;
	}

	public boolean isAggregated() {
		return aggregation != Aggregation.None;
	}

	public Aggregation getAggregation() {
		return aggregation;
	}

	@Override
	public Query owner() {
		IObject owner = getOwner();
		return owner instanceof Query ? (Query)owner : null;
	}

	@Override
	public IAccess access() {
		return access != null ? access : (access = ApplicationServer.getUser().privileges().getFieldAccess(this));
	}

	public final void setCursor(Select cursor) {
		this.cursor = cursor;
	}

	public String qualifiedName() {
		return (getContainer() != null ? "[" + getContainer().displayName() + "]" : "") + displayName();
	}

	@Override
	public boolean isPrimaryKey() {
		if(isPrimaryKey == null)
			isPrimaryKey = new bool(hasAttribute(IObject.PrimaryKey));
		return isPrimaryKey.get();
	}

	@Override
	public boolean isParentKey() {
		if(isParentKey == null) {
			String value = getAttribute(IObject.ParentKey);
			isParentKey = new bool(value != null && !value.equalsIgnoreCase("false"));
		}
		return isParentKey.get();
	}

	@Override
	public boolean isExpression() {
		return false;
	}

	public boolean isDataField() {
		return !isPrimaryKey() && !isParentKey();
	}

	public void setPath(Collection<ILink> path) {
		this.path = path;
	}

	public boolean isRightJoined() {
		return rightJoined;
	}

	public void setRightJoined(boolean rightJoined) {
		this.rightJoined = rightJoined;
	}

	public String format(DatabaseVendor vendor, FormatOptions options) {
		String alias = options.getFieldAlias(this);

		if(alias == null)
			return owner().getAlias() + '.' + vendor.quote(name());

		return alias;
	}

	public SqlConst toSqlConst() {
		return new SqlConst(get());
	}

	@Override
	public boolean wasNull() {
		return wasNull;
	}

	public void setWasNull(boolean wasNull) {
		this.wasNull= wasNull;
	}

	protected Select getCursor() {
		return cursor;
	}

	protected primary read() throws SQLException {
		if(cursor.isGrouped() && aggregation == Aggregation.Array)
			return cursor.get(this, FieldType.String);
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
	abstract public primary parse(String value);

	@SuppressWarnings("rawtypes")
	public RCollection z8_array() {
		return array((string)internalGet());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public RCollection array(string json) {
		JsonArray jsonArray = new JsonArray(json.get());

		RCollection result = new RCollection();
		for(int i = 0; i < jsonArray.length(); i++)
			result.add(parse(jsonArray.getString(i)));

		return result;
	}

	@Override
	public void set(primary value) {
		if(!changed) {
			originalValue = this.defaultValue;
			changed = true;
		}
		setDefault(value);
	}

	@Override
	public boolean changed() {
		return changed;
	}

	public void reset() {
		if(changed) {
			defaultValue = originalValue;
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
	public Collection<Field> fields() {
		Collection<Field> result = new LinkedHashSet<Field>();
		result.add(this);
		if(valueFrom != null)
			result.add(valueFrom.get());
		return result;
	}

	@Override
	public FieldType metaType() {
		return type();
	}

	private boolean hasReadOnlyLinks(Collection<ILink> links) {
		for(ILink link : links) {
			if(((Field)link).readOnly())
				return true;
		}
		return false;
	}

	private boolean hasRequiredLinks(Collection<ILink> links) {
		for(ILink link : links) {
			if(((Field)link).required())
				return true;
		}
		return false;
	}

	@Override
	public void writeMeta(JsonWriter writer, Query query, Query context) {
		if(isWritingMeta)
			return;

		isWritingMeta = true;

		writer.writeProperty(Json.type, metaType().toString());
		writer.writeProperty(Json.format, format, new string());
		writer.writeProperty(Json.length, length, new integer(0));

		writer.writeProperty(Json.width, width, new integer(0));

		if(aggregation != Aggregation.None)
			writer.writeProperty(Json.aggregation, aggregation.toString());

		boolean readOnly = false;
		boolean required = false;

		bool wasReadOnly = this.readOnly;
		bool wasRequired = this.required;

		if(path == null || !path.isEmpty() && path.iterator().next().owner() != query)
			path = query.getPath(this);

		if(path != null && !path.isEmpty()) {
			ILink[] links = path.toArray(new ILink[0]);

			ILink firstLink = links[0];
			ILink lastLink = links[links.length - 1];

			Field linkField = (Field)firstLink;

			access = firstLink.access();

			Query linkQuery = firstLink.getQuery();

			writer.writeProperty(Json.isCombobox, true);
			writer.writeControls(Json.columns, CLASS.asList(columns), query, context);

			writer.startObject(Json.query);
			writer.writeProperty(Json.id, context.classId());
			writer.writeProperty(Json.name, linkQuery.id());
			writer.writeProperty(Json.primaryKey, linkQuery.primaryKey().id());
			writer.writeProperty(Json.parentKey, linkQuery.parentKey() != null ? linkQuery.parentKey().id() : null);
			writer.writeProperty(Json.lockKey, linkQuery.lockKey().id());
			writer.writeSort(linkQuery.sortFields());
			writer.finishObject();

			writer.startObject(Json.link);
			writer.writeProperty(Json.name, firstLink.id());
			writer.writeProperty(Json.owner, lastLink.id());
			writer.writeProperty(Json.isBackward, firstLink instanceof IJoin);
			writer.writeProperty(Json.primaryKey, linkQuery.primaryKey().id());
			if(firstLink.isParentKey()) {
				writer.writeProperty(Json.isParentKey, true);
				writer.startArray(Json.parentKeys);
				for(Field parentKey : linkQuery.parentKeys())
					writer.write(parentKey.id());
				writer.finishArray();
			}
			writer.finishObject();

			writer.writeSort(firstLink.getQuery().sortFields());

			readOnly = hasReadOnlyLinks(path) || !linkField.access().write() || readOnly();
			required = !readOnly && (hasRequiredLinks(path) || required());

			this.readOnly = new bool(readOnly);
			this.required = new bool(required);
		} else {
			readOnly = readOnly() || isExpression() || !access().write();
			required = !readOnly && required();

			this.readOnly = new bool(readOnly() || readOnly);
			this.required = new bool(required() || required);

			writer.writeProperty(Json.isText, true);

			if(query.primaryKey() == this)
				writer.writeProperty(Json.isPrimaryKey, true);
		}

		if(valueFrom != null)
			writer.writeProperty(Json.valueFrom, valueFrom.get().id());

		if(valueFor != null)
			writer.writeProperty(Json.valueFor, valueFor.get().id());


		super.writeMeta(writer, query, context);

		this.readOnly = wasReadOnly;
		this.required = wasRequired;

		isWritingMeta = false;
	}

	public void setWriteNulls(boolean writeNulls) {
		this.writeNulls = writeNulls;
	}

	protected primary getNullValue() {
		return getDefault();
	}

	public void writeData(JsonWriter writer) {
		primary value = get();
		if((isPrimaryKey() || access().read()) && (!wasNull() || writeNulls))
			writer.writeProperty(id(), wasNull() ? getNullValue() : value);
	}

	public binary binary() {
		return (binary)get();
	}

	public bool bool() {
		return (bool)get();
	}

	public geometry geometry() {
		return (geometry)get();
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

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public sql_bool inVector(Collection values) {
		return new sql_bool(new InVector(this, (Collection<primary>)values));
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public sql_bool notInVector(Collection values) {
		return new sql_bool(new UnaryNot(new InVector(this, (Collection<primary>)values)));
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

	public geometry z8_geometry() {
		return geometry();
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

	public bool z8_wasNull() {
		return new bool(wasNull());
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

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public sql_bool z8_inVector(RCollection values) {
		return inVector((Collection<primary>)values);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public sql_bool z8_notInVector(RCollection values) {
		return notInVector((Collection<primary>)values);
	}

	@Override
	public int controlSum() {
		String name = name() + " " + sqlType(DatabaseVendor.Postgres);
		return Math.abs(name.hashCode());
	}
}
