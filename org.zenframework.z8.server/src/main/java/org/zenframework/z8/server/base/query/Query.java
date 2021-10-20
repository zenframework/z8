package org.zenframework.z8.server.base.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.zenframework.z8.server.base.form.Control;
import org.zenframework.z8.server.base.form.action.Action;
import org.zenframework.z8.server.base.form.report.Report;
import org.zenframework.z8.server.base.table.value.Expression;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.FileField;
import org.zenframework.z8.server.base.table.value.ILink;
import org.zenframework.z8.server.base.table.value.Link;
import org.zenframework.z8.server.base.view.filter.Filter;
import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.db.Insert;
import org.zenframework.z8.server.db.Select;
import org.zenframework.z8.server.db.Update;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.expressions.And;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.request.actions.ActionConfig;
import org.zenframework.z8.server.request.actions.CopyAction;
import org.zenframework.z8.server.request.actions.DestroyAction;
import org.zenframework.z8.server.request.actions.ReadAction;
import org.zenframework.z8.server.request.actions.UpdateAction;
import org.zenframework.z8.server.runtime.IClass;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.security.IAccess;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.types.sql.sql_bool;

public class Query extends OBJECT {
	static public int DefaultStart = 0;
	static public int DefaultLimit = 200;

	static public class strings {
		public final static String ReadError = "Query.readError";
	}

	public static class CLASS<T extends Query> extends OBJECT.CLASS<T> {

		public CLASS(IObject container) {
			super(container);
			setJavaClass(Query.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new Query(container);
		}
	}

	public integer priority = null;
	public bool readOnly = bool.False;
	public integer colCount = new integer(4);

	public Field.CLASS<? extends Field> period;

	public integer limit = null;
	public bool totals = bool.False;

	public RCollection<Control.CLASS<? extends Control>> controls = new RCollection<Control.CLASS<? extends Control>>();
	public RCollection<Field.CLASS<? extends Field>> names = new RCollection<Field.CLASS<? extends Field>>();
	public RCollection<Field.CLASS<? extends Field>> columns = new RCollection<Field.CLASS<? extends Field>>();
	public RCollection<Field.CLASS<? extends Field>> quickFilters = new RCollection<Field.CLASS<? extends Field>>();
	public RCollection<Field.CLASS<? extends Field>> filterFields = new RCollection<Field.CLASS<? extends Field>>();
	public RCollection<Field.CLASS<? extends Field>> extraFields = new RCollection<Field.CLASS<? extends Field>>();
	public RCollection<Field.CLASS<? extends Field>> sortFields = new RCollection<Field.CLASS<? extends Field>>();
	public RCollection<Field.CLASS<? extends Field>> groupFields = new RCollection<Field.CLASS<? extends Field>>();
	public RCollection<Action.CLASS<? extends Action>> actions = new RCollection<Action.CLASS<? extends Action>>();
	public RCollection<Report.CLASS<? extends Report>> reports = new RCollection<Report.CLASS<? extends Report>>();

	public RCollection<Link.CLASS<? extends Link>> aggregateBy = new RCollection<Link.CLASS<? extends Link>>();
	public RCollection<Field.CLASS<? extends Field>> groupBy = new RCollection<Field.CLASS<? extends Field>>();
	private int groupingDisabled = 0;
	
	private Collection<Field.CLASS<Field>> dataFields;
	private Collection<Field.CLASS<Field>> primaryFields;
	private Collection<Field.CLASS<Field>> links;

	private Collection<Field> selectFields;

	private String alias;
	private SqlToken where;
	private SqlToken having;
	private SqlToken scope;

	protected Select cursor;
	public ReadLock readLock = ReadLock.None;
	private boolean transactive = true;

	private IAccess access;
	private Connection connection;

	private Field parentKey = null;
	private boolean parentKeyFound = false;

	private Field lockKey = null;
	private boolean lockKeyFound = false;

	protected Query() {
		this(null);
	}

	@Override
	public String displayName() {
		String name = super.displayName();
		return name == null || name.isEmpty() ? name() : name;
	}

	protected Query(IObject container) {
		super(container);
	}

	public boolean equals(Query query) {
		return this == query;
	}

	public IAccess access() {
		return access != null ? access : (access = ApplicationServer.getUser().privileges().getTableAccess(this));
	}

	private void initFields() {
		for(Field field : getPrimaryFields()) {
			if(field.changed())
				continue;

			primary value = field.getDefault();
			if(value != null && !value.equals(field.getDefaultValue()))
				field.set(value);
		}
	}

	public void onNew() {
		if(ApplicationServer.userEventsEnabled())
			z8_onNew();

		initFields();
	}

	public void onCopyAction(guid recordId) {
		if(ApplicationServer.userEventsEnabled())
			z8_onCopyAction(recordId);
	}

	public void onCopy(guid recordId) {
		if(ApplicationServer.userEventsEnabled())
			z8_onCopy(recordId);
	}

	public void onReadAction() {
		if(ApplicationServer.userEventsEnabled())
			z8_onReadAction();
	}

	public void beforeRead() {
		if(ApplicationServer.userEventsEnabled())
			z8_beforeRead();
	}

	public void afterRead() {
		if(ApplicationServer.userEventsEnabled())
			z8_afterRead();
	}

	public void onCreateAction(guid recordId) {
		if(ApplicationServer.userEventsEnabled())
			z8_onCreateAction(recordId);
	}

	public void beforeCreate(guid recordId) {
		if(ApplicationServer.userEventsEnabled())
			z8_beforeCreate(recordId);
	}

	public void afterCreate(guid recordId) {
		if(ApplicationServer.userEventsEnabled())
			z8_afterCreate(recordId);
	}

	public void onUpdateAction(guid recordId) {
		if(ApplicationServer.userEventsEnabled())
			z8_onUpdateAction(recordId);
	}

	public void beforeUpdate(guid recordId) {
		if(ApplicationServer.userEventsEnabled())
			z8_beforeUpdate(recordId);
	}

	public void afterUpdate(guid recordId) {
		if(ApplicationServer.userEventsEnabled())
			z8_afterUpdate(recordId);
	}

	public void onDestroyAction(guid recordId) {
		if(ApplicationServer.userEventsEnabled())
			z8_onDestroyAction(recordId);
	}

	public void beforeDestroy(guid recordId) {
		if(ApplicationServer.userEventsEnabled())
			z8_beforeDestroy(recordId);
	}

	public void afterDestroy(guid recordId) {
		if(ApplicationServer.userEventsEnabled())
			z8_afterDestroy(recordId);
	}

	public void writeReportMeta(JsonWriter writer, String name, Collection<Field> fields) {
		writer.startObject();
		writer.writeProperty(Json.displayName, displayName());
		writer.writeProperty(Json.columnHeader, columnHeader());
		writer.writeProperty(Json.name, name);
		writer.writeProperty(Json.request, classId());
		writer.startArray(Json.fields);

		for(Field field : fields) {
			writer.startObject();
			writer.writeProperty(Json.displayName, field.displayName());
			writer.writeProperty(Json.id, field.id());
			writer.writeProperty(Json.type, field.type().toString());
			writer.finishObject();
		}

		writer.finishArray();
		writer.finishObject();
	}

	public guid recordId() {
		return (guid)primaryKey().get();
	}

	public ReadLock getReadLock() {
		return readLock;
	}

	public void setReadLock(ReadLock readLock) {
		this.readLock = readLock;
	}

	public boolean isTransactive() {
		return transactive;
	}

	public void setTransactive(boolean transactive) {
		this.transactive = transactive;
	}

	public Connection getConnection() {
		return connection != null ? connection : ConnectionManager.get();
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	public boolean hasRecord(guid recordId) {
		try {
			saveState();
			return readRecord(recordId, Arrays.asList(primaryKey()));
		} finally {
			restoreState();
		}
	}

	public boolean hasRecord(SqlToken where) {
		return readFirst(Arrays.asList(primaryKey()), where);
	}

	public RCollection<guid> findRecords(SqlToken where) {
		RCollection<guid> records = new RCollection<guid>();
		read(Arrays.asList(primaryKey()), where);
		while(next())
			records.add(primaryKey().guid());
		return records;
	}

	public int count() {
		return count(null);
	}

	public int count(SqlToken where) {
		try {
			ConnectionManager.get().flush();

			saveState();
			ReadAction action = new ReadAction(this);
			action.addFilter(where);
			return action.getCounter().count();
		} finally {
			restoreState();
		}
	}

	public boolean aggregate() {
		return aggregate(null, null);
	}

	public boolean aggregate(Collection<Field> fields) {
		return aggregate(fields, null);
	}

	public boolean aggregate(SqlToken where) {
		return aggregate(null, where);
	}

	public boolean aggregate(Collection<Field> fields, SqlToken where) {
		ReadAction action = new ReadAction(this, fields);
		action.addFilter(where);

		if(cursor != null)
			cursor.close();

		cursor = action.getTotals();

		return cursor.next();
	}

	private Collection<Field> getInsertFields(guid recordId) {
		Collection<Field> myFields = new ArrayList<Field>();
		Collection<Field> fields = getPrimaryFields();

		for(Field field : fields) {
			if(field.owner() != this)
				continue;

			if(field.isPrimaryKey() && !field.changed())
				field.set(recordId);

			myFields.add(field);
		}

		return myFields;
	}

	public int executeUpdate(guid recordId) {
		return Update.create(this, getChangedFields(), recordId).execute();
	}

	private void executeInsert(Collection<Field> fields) {
		Insert.create(this, fields).execute();
	}

	public guid insert(guid recordId) {
		Collection<Field> fields = getInsertFields(recordId);

		beforeCreate(recordId);
		executeInsert(fields);
		afterCreate(recordId);

		Field primaryKey = primaryKey();
		recordId = primaryKey != null ? primaryKey.guid() : recordId;

		for(Field field : fields)
			field.reset();

		return recordId;
	}

	public guid create() {
		guid id = guid.create();
		return create(id);
	}

	public guid create(guid recordId) {
		onNew();
		return insert(recordId);
	}

	public guid copy(guid recordId) {
		return CopyAction.run(this, recordId);
	}

	public Collection<Field> read(Collection<Field> fields) {
		return read(fields, -1);
	}

	public Collection<Field>  read(Collection<Field> fields, int limit) {
		return read(fields, 0, limit);
	}

	public Collection<Field>  read(Collection<Field> fields, int start, int limit) {
		return read(fields, null, start, limit);
	}

	public boolean readFirst(Collection<Field> fields) {
		read(fields, 1);
		return next();
	}

	public Collection<Field> read(Collection<Field> fields, SqlToken where) {
		return read(fields, where, -1);
	}

	public Collection<Field> read(Collection<Field> fields, SqlToken where, int limit) {
		return read(fields, where, 0, limit);
	}

	public Collection<Field> read(Collection<Field> fields, SqlToken where, int start, int limit) {
		return read(fields, null, null, where, null, start, limit);
	}

	public boolean readFirst(Collection<Field> fields, SqlToken where) {
		read(fields, null, null, where, null, 0, 1);
		return next();
	}

	public void read(Collection<Field> fields, Collection<Field> sortFields, SqlToken where) {
		read(fields, sortFields, where, -1);
	}

	public void read(Collection<Field> fields, Collection<Field> sortFields, SqlToken where, int limit) {
		read(fields, sortFields, where, 0, limit);
	}

	public void read(Collection<Field> fields, Collection<Field> sortFields, SqlToken where, int start, int limit) {
		read(fields, sortFields, null, where, null, start, limit);
	}

	public boolean readFirst(Collection<Field> fields, Collection<Field> sortFields, SqlToken where) {
		read(fields, sortFields, null, where, null);
		return next();
	}

	public void sort(Collection<Field> sortFields, SqlToken where) {
		read(sortFields, where, -1);
	}

	public void sort(Collection<Field> sortFields, SqlToken where, int limit) {
		read(sortFields, where, 0, limit);
	}

	public void sort(Collection<Field> sortFields, SqlToken where, int start, int limit) {
		read(null, sortFields, where);
	}

	public void sort(Collection<Field> fields, Collection<Field> sortFields, SqlToken where) {
		read(fields, null, sortFields, where, null);
	}

	public void group(Collection<Field> groupFields, SqlToken where) {
		group(groupFields, where, -1);
	}

	public void group(Collection<Field> groupFields, SqlToken where, int limit) {
		group(null, groupFields, where, 0, limit);
	}

	public void group(Collection<Field> groupFields, SqlToken where, int start, int limit) {
		group(null, groupFields, where, start, limit);
	}

	public void group(Collection<Field> fields, Collection<Field> groupFields) {
		group(fields, groupFields, -1);
	}

	public void group(Collection<Field> fields, Collection<Field> groupFields, int limit) {
		group(fields, groupFields, 0, limit);
	}

	public void group(Collection<Field> fields, Collection<Field> groupFields, int start, int limit) {
		group(fields, groupFields, null, start, limit);
	}

	public void group(Collection<Field> fields, Collection<Field> groupFields, SqlToken where) {
		group(fields, groupFields, where, -1);
	}

	public void group(Collection<Field> fields, Collection<Field> groupFields, SqlToken where, SqlToken having) {
		read(fields, null, groupFields, where, having);
	}

	public void group(Collection<Field> fields, Collection<Field> groupFields, SqlToken where, int limit) {
		group(fields, groupFields, where, 0, limit);
	}

	public void group(Collection<Field> fields, Collection<Field> groupFields, SqlToken where, int start, int limit) {
		read(fields, null, groupFields, where, null, start, limit);
	}

	public boolean readRecord(guid id, Collection<Field> fields) {
		ReadAction action = new ReadAction(this, fields, id);

		if(cursor != null)
			cursor.close();

		cursor = action.getCursor();

		if(!cursor.next()) {
			cursor.close();
			cursor = null;
			return false;
		}

		return true;
	}

	protected boolean readFirst(Collection<Field> fields, Collection<Field> sortFields, Collection<Field> groupFields, SqlToken where, SqlToken having) {
		read(fields, sortFields, groupFields, where, having);
		return next();
	}

	protected void read(Collection<Field> fields, Collection<Field> sortFields, Collection<Field> groupFields, SqlToken where, SqlToken having) {
		read(fields, sortFields, groupFields, where, having, 0, -1);
	}

	protected Collection<Field> read(Collection<Field> fields, Collection<Field> sortFields, Collection<Field> groupFields, SqlToken where, SqlToken having, int start, int limit) {
		ActionConfig config = new ActionConfig();
		config.query = this;
		config.fields = fields;
		config.sortFields = sortFields;
		config.groupBy = groupFields;

		ReadAction action = new ReadAction(config);
		action.addFilter(where);
		action.addGroupFilter(having);

		action.setLimit(limit);
		action.setStart(start);

		if(cursor != null)
			cursor.close();

		cursor = action.getCursor();
		return cursor.getFields();
	}

	public Collection<Field> getChangedFields() {
		Collection<Field> fields = new ArrayList<Field>();

		for(Field.CLASS<? extends Field> field : primaryFields()) {
			if(field.hasInstance() && field.get().changed())
				fields.add(field.get());
		}

		return fields;
	}

	public int update(guid id) {
		return UpdateAction.run(this, id, true);
	}

	public void updateOrCreate(guid id) {
		if(UpdateAction.run(this, id, false) == 0)
			create(id);
		else
			resetChangedFields();
	}

	private void resetChangedFields() {
		Collection<Field> changedFields = getChangedFields();
		for(Field field : changedFields)
			field.reset();
	}

	public int update(SqlToken where) {
		Collection<Field> fields = new ArrayList<Field>();
		fields.add(primaryKey());

		read(fields, where);

		int result = 0;

		while(next()) {
			guid id = recordId();
			result += UpdateAction.run(this, id, false);
		}

		resetChangedFields();

		return result;
	}

	public int destroy(guid id) {
		return DestroyAction.run(this, id);
	}

	public int destroy(SqlToken where) {
		Collection<Field> fields = new ArrayList<Field>();
		fields.add(primaryKey());

		disableGrouping();
		read(fields, where);
		enableGrouping();

		int result = 0;

		while(next()) {
			guid id = recordId();
			result += DestroyAction.run(this, id);
		}

		return result;
	}

	private boolean readRecord(guid id, RCollection<Field.CLASS<Field>> fieldClasses) {
		Collection<Field> fields = CLASS.asList(fieldClasses);

		fields = fields.isEmpty() ? null : fields;

		return readRecord(id, fields);
	}

	private void read1(RCollection<Field.CLASS<Field>> fieldClasses, RCollection<Field.CLASS<Field>> sortClasses, RCollection<Field.CLASS<Field>> groupClasses, SqlToken where, SqlToken having) {
		Collection<Field> fields = CLASS.asList(fieldClasses);
		Collection<Field> sortFields = CLASS.asList(sortClasses);
		Collection<Field> groupFields = CLASS.asList(groupClasses);

		fields = fields.isEmpty() ? null : fields;
		sortFields = sortFields.isEmpty() ? null : sortFields;
		groupFields = groupFields.isEmpty() ? null : groupFields;

		read(fields, sortFields, groupFields, where, having);
	}

	public boolean next() {
		if(cursor == null)
			throw new RuntimeException("Method Query.read() should be called before Query.next()");

		return cursor.next();
	}

	public boolean isAfterLast() {
		if(cursor == null)
			throw new RuntimeException("Method Query.read() should be called before Query.isAfterLast()");

		return cursor.isAfterLast();
	}

	private List<State> states = new ArrayList<State>();

	private static class FieldState {
		private Field field;
		private primary value;
		private boolean changed;

		public FieldState(Field field) {
			this.field = field;

			changed = field.changed();

			if(changed) {
				value = field.get();
				field.reset();
			}
		}

		public void restore() {
			if(changed)
				field.set(value);
		}
	}

	private static class State {
		private Select cursor;
		private Collection<FieldState> fieldStates = new ArrayList<FieldState>();

		public State(Collection<Field.CLASS<Field>> fields, Select cursor) {
			this.cursor = cursor;

			for(Field.CLASS<Field> field : fields)
				fieldStates.add(field.hasInstance() ? new FieldState(field.get()) : null);
		}

		public Select cursor() {
			return cursor;
		}

		public void restore() {
			for(FieldState state : fieldStates)
				if(state != null)
					state.restore();
		}
	}

	public void saveState() {
		states.add(new State(dataFields(), cursor));

		if(cursor != null)
			cursor.saveState();

		cursor = null;
	}

	public void restoreState() {
		if(cursor != null)
			cursor.close();

		State state = states.remove(states.size() - 1);

		cursor = state.cursor();

		if(cursor != null)
			cursor.restoreState();

		state.restore();
	}

	public Collection<Field> selectFields() {
		return selectFields;
	}

	public void setSelectFields(Collection<Field> selectFields) {
		this.selectFields = selectFields;
	}

	@SuppressWarnings("unchecked")
	public Collection<Field.CLASS<Field>> dataFields() {
		if(dataFields != null)
			return dataFields;

		dataFields = new LinkedHashSet<Field.CLASS<Field>>(20);

		for(IClass<? extends IObject> member : members()) {
			if(member instanceof Field.CLASS) {
				member.setOwner(this);
				dataFields.add((Field.CLASS<Field>)member);
			}
		}

		return dataFields;
	}

	public Collection<Field.CLASS<Field>> primaryFields() {
		if(primaryFields != null)
			return primaryFields;

		primaryFields = new LinkedHashSet<Field.CLASS<Field>>(20);

		for(Field.CLASS<Field> field : dataFields()) {
			if(!(field instanceof Expression.CLASS))
				primaryFields.add(field);
		}

		return primaryFields;
	}

	public Collection<Field> getDataFields() {
		return CLASS.asList(dataFields());
	}

	public Collection<Field> getPrimaryFields() {
		return CLASS.asList(primaryFields());
	}

	public Collection<Field> fields() {
		Set<Field> result = new LinkedHashSet<Field>(50);

		for(Control control : CLASS.asList(controls))
			result.addAll(control.fields());

		result.addAll(names());
		result.addAll(columns());
		result.addAll(extraFields());

		return result;
	}

	public Collection<Field> columns() {
		return CLASS.asList(columns);
	}

	public Collection<Field> names() {
		return CLASS.asList(names);
	}

	public Collection<Field> extraFields() {
		return CLASS.asList(extraFields);
	}

	public Collection<Field> quickFilters() {
		return CLASS.asList(quickFilters);
	}

	public Collection<Field> filterFields() {
		return CLASS.asList(filterFields);
	}

	public Collection<Field> sortFields() {
		return CLASS.asList(sortFields);
	}

	public Collection<Field> groupFields() {
		return CLASS.asList(groupFields);
	}

	private Collection<Control.CLASS<? extends Control>> defaultControls() {
		Collection<Control.CLASS<? extends Control>> result = new ArrayList<Control.CLASS<? extends Control>>();
		for(Field.CLASS<Field> field : dataFields()) {
			if(!field.system())
				result.add(field);
		}
		return result;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Collection<Control> controls() {
		return CLASS.asList(controls.isEmpty() ? (Collection)defaultControls() : controls);
	}

	public Collection<Field> attachments() {
		Collection<Field> result = new ArrayList<Field>();
		for(Field.CLASS<Field> field : dataFields()) {
			if(field instanceof FileField.CLASS)
				result.add(field.get());
		}
		return result;
	}

	public Field periodKey() {
		return period != null ? period.get() : null;
	}

	public void registerControl(Control.CLASS<?> control) {
		controls.add(control);
	}

	final public SqlToken scope() {
		return scope == null ? z8_scope() : scope;
	}

	final public SqlToken where() {
		return where == null ? z8_where() : where;
	}

	final public void setWhere(SqlToken where) {
		this.where = where;
	}

	final public void setWhere(String json) {
		setWhere(new Filter(json, this).where());
	}

	final public void setWhere(Collection<String> json) {
		setWhere(new Filter(json, this).where());
	}

	final public void addWhere(String json) {
		addWhere(new Filter(json, this).where());
	}

	final public void addWhere(Collection<String> json) {
		addWhere(new Filter(json, this).where());
	}

	final public void addWhere(SqlToken where) {
		if(where != null)
			this.where = new And(where(), where);
	}

	final public SqlToken having() {
		return having == null ? z8_having() : having;
	}

	final public void setHaving(SqlToken having) {
		this.having = having;
	}

	final public void addHaving(String json) {
		addHaving(new Filter(json, this).where());
	}

	final public void addHaving(Collection<String> json) {
		addHaving(new Filter(json, this).where());
	}

	final public void addHaving(SqlToken having) {
		if(having != null)
			this.having = new And(having(), having);
	}

	private boolean isGroupingEnabled() {
		return groupingDisabled == 0;
	}

	private void enableGrouping() {
		groupingDisabled = Math.max(0, groupingDisabled - 1);
	}

	private void disableGrouping() {
		groupingDisabled++;
	}

	public boolean isGrouped() {
		return isGroupingEnabled() && (!groupBy.isEmpty() || !aggregateBy.isEmpty());
	}

	public Collection<Link> getAggregateByFields() {
		return isGroupingEnabled() ? CLASS.asList(aggregateBy) : new ArrayList<Link>();
	}

	public Collection<Field> getGroupByFields() {
		return isGroupingEnabled() ? CLASS.asList(groupBy) : new ArrayList<Field>();
	}

	public Field primaryKey() {
		return null;
	}

	public Field parentKey() {
		if (!parentKeyFound) {
			for (Field.CLASS<Field> field : dataFields()) {
				if (field.get().isParentKey()) {
					parentKey = field.get();
					break;
				}
			}
			parentKeyFound = true;
		}
		return parentKey;
	}

	public Field[] parentKeys() {
		return new Field[0];
	}

	public boolean hasPrimaryKey() {
		return primaryKey() != null;
	}

	public Field lockKey() {
		if (!lockKeyFound) {
			for (Field.CLASS<Field> field : dataFields()) {
				if (field.get().isLockKey()) {
					lockKey = field.get();
					break;
				}
			}
			lockKeyFound = true;
		}
		return lockKey;
	}

	public String getAlias() {
		if(alias == null) {
			String id = id().isEmpty() ? name() : id();
			alias = "T" + Math.abs(id.hashCode());
		}
		return alias;
	}

	public int limit() {
		return limit == null ? DefaultLimit : limit.getInt();
	}

	public int start() {
		return DefaultStart;
	}

	@Override
	public String toString() {
		return id();
	}

	public Query owner() {
		IObject owner = getOwner();
		return owner instanceof Query ? (Query)owner : null;
	}

	public Collection<Field.CLASS<Field>> getLinks() {
		if(links == null) {
			links = new ArrayList<Field.CLASS<Field>>(10);

			for(Field.CLASS<Field> field : dataFields()) {
				if(field.instanceOf(ILink.class))
					links.add(field);
			}
		}

		return links;
	}

	private static Pattern pattern = Pattern.compile("\\.");

	private String[] parseId(String id) {
		String myId = id();

		if(!id.startsWith(myId))
			return null;

		if(id.equals(myId))
			return new String[0];

		id = id.substring(myId.length());

		if(!myId.isEmpty() && id.charAt(0) != '.')
			return null;

		String[] ids = pattern.split(id);
		return Arrays.copyOfRange(ids, myId.isEmpty() ? 0 : 1, ids.length);
	}

	private class MemberSearch {
		public IObject member;
		public Collection<IObject> path = new ArrayList<IObject>(10);
	}

	private MemberSearch findMember(String id) {
		String[] path = parseId(id);

		if(path == null)
			return null;

		MemberSearch result = new MemberSearch();
		result.member = this;

		if(path.length > 0 && path[path.length - 1].isEmpty())
			return result;

		for(String name : path) {
			IClass<? extends IObject> memberCls = result.member.getMember(name);
			if(memberCls == null)
				return null;
			IObject member = memberCls.get();
			result.member = member;
			result.path.add(member);
		}

		return result;
	}

	public Query findQueryById(String id) {
		MemberSearch result = findMember(id);
		return result != null && result.member instanceof Query ? (Query)result.member : null;
	}

	public Field findFieldById(String id) {
		MemberSearch result = findMember(id);
		return result != null && result.member instanceof Field ? (Field)result.member : null;
	}

	public Action findActionById(String id) {
		MemberSearch result = findMember(id);
		return result != null && result.member instanceof Action ? (Action)result.member : null;
	}

	public Report findReportById(String id) {
		MemberSearch result = findMember(id);
		return result != null && result.member instanceof Report ? (Report)result.member : null;
	}

	public ILink getLinkTo(Query query) {
		for(OBJECT.CLASS<?> cls : getLinks()) {
			ILink link = (ILink)cls.get();
			if(link.query() == query.getCLASS())
				return link;
		}
		return null;
	}

	public Collection<ILink> getPath(Query query) {
		if(query == null)
			return null;

		MemberSearch result = findMember(query.id());

		if(result == null || !(result.member instanceof Query))
			return null;

		Collection<ILink> path = new ArrayList<ILink>(10);

		Query current = this;

		for(IObject object : result.path) {
			if(object instanceof Query) {
				query = (Query)object;
				ILink link = current.getLinkTo(query);
				if(link != null)
					path.add(link);
				current = query;
			}
		}

		return path;
	}

	public Collection<ILink> getPath(Field field) {
		return getPath(field.owner());
	}
	
	public Field getFieldByIndex(String index) {
		for(Field.CLASS<? extends Field> field : dataFields()) {
			if(index.equals(field.index()))
				return field.get();
		}
		return null;
	}

	public Field getFieldById(String id) {
		for(Field.CLASS<? extends Field> field : dataFields()) {
			if(id.equals(field.id()))
				return field.get();
		}
		return null;
	}

	public Field getFieldByName(String name) {
		for(Field.CLASS<? extends Field> field : dataFields()) {
			if(name.equals(field.name()))
				return field.get();
		}
		return null;
	}

	public Collection<Action> actions() {
		return CLASS.asList(actions);
	}

	public Action getAction(String id) {
		for(Action action : actions()) {
			if(action.id().equals(id))
				return action;
		}
		return null;
	}

	public Collection<Report> reports() {
		return CLASS.asList(reports);
	}

	public Report getReport(String id) {
		for(Report report : reports()) {
			if(report.id().equals(id))
				return report;
		}
		return null;
	}

	public int priority() {
		return priority != null ? priority.getInt() : 0;
	}

	public boolean readOnly() {
		return readOnly.get() || isGrouped();
	}

	public void writeMeta(JsonWriter writer, Query context) {
		writer.writeProperty(Json.isQuery, true);

		writer.writeProperty(Json.id, id());
		writer.writeProperty(Json.icon, icon());
		writer.writeProperty(Json.ui, ui());
		writer.writeProperty(Json.presentation, presentation());
		writer.writeProperty(Json.text, displayName());
		writer.writeProperty(Json.sourceCode, sourceCodeLocation());

		writer.writeControls(Json.controls, controls(), this, context);
		writer.writeControls(Json.columns, columns(), this, context);
		writer.writeControls(Json.nameFields, names(), this, context);
		writer.writeControls(Json.quickFilters, quickFilters(), this, context);
		writer.writeControls(Json.filterFields, filterFields(), this, context);

		writer.writeControls(Json.fields, selectFields(), this, context);

		writer.writeActions(actions(), this, context);
		writer.writeReports(reports());
		writer.writeAccess(access());

		writeKeys(writer, selectFields());

		writer.writeProperty(Json.readOnly, isGrouped() || readOnly());

		writer.writeProperty(Json.limit, limit);
		writer.writeProperty(Json.totals, totals);
		writer.writeProperty(Json.colCount, colCount);
	}

	private void writeKeys(JsonWriter writer, Collection<Field> fields) {
		Field primaryKey = primaryKey();
		if(primaryKey != null && fields.contains(primaryKey))
			writer.writeProperty(Json.primaryKey, primaryKey.id());

		Field lockKey = lockKey();
		if(lockKey != null && fields.contains(lockKey))
			writer.writeProperty(Json.lockKey, lockKey.id());

		Field period = periodKey();
		if(period != null)
			writer.writeProperty(Json.periodKey, period.id());

		Field parentKey = parentKey();
		if(parentKey != null && fields.contains(parentKey))
			writer.writeProperty(Json.parentKey, parentKey().id());
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////

	public sql_bool z8_having() {
		return sql_bool.True;
	}

	public sql_bool z8_scope() {
		return sql_bool.True;
	}

	public sql_bool z8_where() {
		return sql_bool.True;
	}

	public void z8_addWhere(sql_bool where) {
		addWhere(where);
	}

	public void z8_addWhere(string json) {
		addWhere(json.get());
	}

	public void z8_addWhere(RCollection<String> json) {
		addWhere(json);
	}

	public void z8_setWhere(sql_bool where) {
		setWhere(where);
	}

	public void z8_setWhere(string json) {
		if(json != null)
			setWhere(json.get());
	}

	public void z8_setWhere(RCollection<string> json) {
		setWhere(string.unwrap(json));
	}

	public void z8_addHaving(sql_bool having) {
		addHaving(having);
	}

	public void z8_addHaving(string json) {
		if(json != null)
			addHaving(json.get());
	}

	public void z8_addHaving(RCollection<String> json) {
		addHaving(json);
	}

	public bool z8_hasRecord(guid recordId) {
		return new bool(hasRecord(recordId));
	}

	public bool z8_hasRecord(sql_bool where) {
		return new bool(hasRecord(where));
	}

	public RCollection<guid> z8_findRecords(sql_bool where) {
		return findRecords(where);
	}

	public integer z8_count() {
		return z8_count(null);
	}

	public integer z8_count(sql_bool where) {
		return new integer(count(where));
	}

	public bool z8_aggregate(RCollection<Field.CLASS<Field>> fieldClasses) {
		return z8_aggregate(fieldClasses, null);
	}

	public bool z8_aggregate(RCollection<Field.CLASS<Field>> fieldClasses, sql_bool where) {
		Collection<Field> fields = CLASS.asList(fieldClasses);
		return new bool(aggregate(fields, where));
	}

	public guid z8_recordId() {
		return recordId();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public RCollection z8_fields() {
		return new RCollection(dataFields());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public RCollection z8_primaryFields() {
		return new RCollection(primaryFields());
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Field.CLASS<? extends Field> z8_getFieldByIndex(string index) {
		return (Field.CLASS)getFieldByIndex(index.get()).getCLASS();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Field.CLASS<? extends Field> z8_getFieldById(string id) {
		return (Field.CLASS)getFieldById(id.get()).getCLASS();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Field.CLASS<? extends Field> z8_getFieldByName(string name) {
		return (Field.CLASS)getFieldByName(name.get()).getCLASS();
	}

	@SuppressWarnings("unchecked")
	public Field.CLASS<Field> z8_findFieldById(string id) {
		return (Field.CLASS<Field>) findFieldById(id.get()).getCLASS();
	}

	public guid z8_create() {
		return create();
	}

	public guid z8_create(guid recordId) {
		return create(recordId);
	}

	public guid z8_copy(guid recordId) {
		return copy(recordId);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public bool z8_readRecord(guid id, RCollection fieldClasses) {
		return new bool(readRecord(id, (RCollection<Field.CLASS<Field>>)fieldClasses));
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void z8_read(RCollection fieldClasses) {
		read1(fieldClasses, null, null, null, null);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public bool z8_readFirst(RCollection fieldClasses) {
		read1(fieldClasses, null, null, null, null);
		return new bool(next());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void z8_read(RCollection fieldClasses, RCollection sortClasses) {
		read1(fieldClasses, sortClasses, null, null, null);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public bool z8_readFirst(RCollection fieldClasses, RCollection sortClasses) {
		read1(fieldClasses, sortClasses, null, null, null);
		return new bool(next());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void z8_read(RCollection fieldClasses, sql_bool where) {
		read1(fieldClasses, null, null, where, null);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public bool z8_readFirst(RCollection fieldClasses, sql_bool where) {
		read1(fieldClasses, null, null, where, null);
		return new bool(next());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void z8_read(RCollection fieldClasses, RCollection sortClasses, sql_bool where) {
		read1(fieldClasses, sortClasses, null, where, null);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public bool z8_readFirst(RCollection fieldClasses, RCollection sortClasses, sql_bool where) {
		read1(fieldClasses, sortClasses, null, where, null);
		return new bool(next());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void z8_sort(RCollection sortClasses) {
		read1(null, sortClasses, null, null, null);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void z8_sort(RCollection sortClasses, sql_bool where) {
		read1(null, sortClasses, null, where, null);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void z8_sort(RCollection sortClasses, RCollection fieldClasses) {
		read1(fieldClasses, sortClasses, null, null, null);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void z8_sort(RCollection sortClasses, RCollection fieldClasses, sql_bool where) {
		read1(fieldClasses, sortClasses, null, where, null);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void z8_group(RCollection groupByClasses) {
		read1(null, null, groupByClasses, null, null);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void z8_group(RCollection groupByClasses, sql_bool where) {
		read1(null, null, groupByClasses, where, null);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void z8_group(RCollection groupByClasses, RCollection fieldClasses) {
		read1(fieldClasses, null, groupByClasses, null, null);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void z8_group(RCollection groupByClasses, RCollection fieldClasses, sql_bool where) {
		read1(fieldClasses, null, groupByClasses, where, null);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void z8_group(RCollection groupByClasses, RCollection fieldClasses, sql_bool where, sql_bool having) {
		read1(fieldClasses, null, groupByClasses, where, having);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void z8_group(RCollection groupClasses, RCollection fieldClasses, RCollection sortClasses) {
		read1(fieldClasses, sortClasses, groupClasses, null, null);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void z8_group(RCollection groupClasses, RCollection fieldClasses, RCollection sortClasses, sql_bool where) {
		read1(fieldClasses, sortClasses, groupClasses, where, null);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void z8_group(RCollection groupClasses, RCollection fieldClasses, RCollection sortClasses, sql_bool where, sql_bool having) {
		read1(fieldClasses, sortClasses, groupClasses, where, having);
	}

	public integer z8_update(guid id) {
		return new integer(update(id));
	}

	public integer z8_update(sql_bool where) {
		return new integer(update(where));
	}

	public integer z8_destroy(guid id) {
		return new integer(destroy(id));
	}

	public integer z8_destroy(sql_bool where) {
		return new integer(destroy(where));
	}

	public bool z8_next() {
		return new bool(next());
	}

	public void z8_onNew() {
	}

	public void z8_onCopyAction(guid recordId) {
	}

	public void z8_onCopy(guid recordId) {
	}

	public void z8_onReadAction() {
	}

	public void z8_beforeRead() {
	}

	public void z8_afterRead() {
	}

	public void z8_onCreateAction(guid recordId) {
	}

	public void z8_beforeCreate(guid recordId) {
	}

	public void z8_afterCreate(guid recordId) {
	}

	public void z8_onUpdateAction(guid recordId) {
	}

	public void z8_beforeUpdate(guid recordId) {
	}

	public void z8_afterUpdate(guid recordId) {
	}

	public void z8_onDestroyAction(guid recordId) {
	}

	public void z8_beforeDestroy(guid recordId) {
	}

	public void z8_afterDestroy(guid recordId) {
	}

	@SuppressWarnings("rawtypes")
	public void z8_onReport(string report, RCollection recordIds) {
	}

	public void z8_setConnection(org.zenframework.z8.server.base.sql.Connection.CLASS<? extends org.zenframework.z8.server.base.sql.Connection> connectionCls) {
		setConnection(connectionCls.get().connection);
	}
}
