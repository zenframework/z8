package org.zenframework.z8.server.base.query;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.zenframework.z8.server.base.Command;
import org.zenframework.z8.server.base.file.Folders;
import org.zenframework.z8.server.base.form.Control;
import org.zenframework.z8.server.base.form.Section;
import org.zenframework.z8.server.base.form.TabControl;
import org.zenframework.z8.server.base.model.actions.ActionParameters;
import org.zenframework.z8.server.base.model.actions.CopyAction;
import org.zenframework.z8.server.base.model.actions.DestroyAction;
import org.zenframework.z8.server.base.model.actions.NewAction;
import org.zenframework.z8.server.base.model.actions.ReadAction;
import org.zenframework.z8.server.base.model.actions.UpdateAction;
import org.zenframework.z8.server.base.model.command.ICommand;
import org.zenframework.z8.server.base.model.sql.Insert;
import org.zenframework.z8.server.base.model.sql.Select;
import org.zenframework.z8.server.base.table.value.AttachmentField;
import org.zenframework.z8.server.base.table.value.Expression;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.base.table.value.ILink;
import org.zenframework.z8.server.base.table.value.Link;
import org.zenframework.z8.server.base.table.value.LinkExpression;
import org.zenframework.z8.server.base.view.filter.Filter;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.expressions.And;
import org.zenframework.z8.server.db.sql.expressions.True;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.reports.BirtFileReader;
import org.zenframework.z8.server.request.Loader;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.security.IAccess;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.exception;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.types.sql.sql_bool;

public class Query extends OBJECT {
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

	public bool readOnly = bool.False;
	public integer columnCount = new integer(4);
	public bool totals = bool.False;

	public RCollection<Query.CLASS<? extends Query>> queries = new RCollection<Query.CLASS<? extends Query>>(true);

	public RCollection<Field.CLASS<? extends Field>> sortFields = new RCollection<Field.CLASS<? extends Field>>();
	public RCollection<Field.CLASS<? extends Field>> groupFields = new RCollection<Field.CLASS<? extends Field>>();

	public Field.CLASS<? extends Field> searchId;
	public RCollection<Field.CLASS<? extends Field>> searchFields = new RCollection<Field.CLASS<? extends Field>>();

	public DataFields dataFields = new DataFields(this);
	public RCollection<Control.CLASS<? extends Control>> formFields = new RCollection<Control.CLASS<? extends Control>>();
	public RCollection<Field.CLASS<? extends Field>> nameFields = new RCollection<Field.CLASS<? extends Field>>();

	public RCollection<Field.CLASS<? extends Field>> columns = new RCollection<Field.CLASS<? extends Field>>();
	public RCollection<Field.CLASS<? extends Field>> quickFilters = new RCollection<Field.CLASS<? extends Field>>();

	public RCollection<Command.CLASS<? extends Command>> commands = new RCollection<Command.CLASS<? extends Command>>();

	public RCollection<Link.CLASS<? extends Link>> aggregateBy = new RCollection<Link.CLASS<? extends Link>>();
	public RCollection<Field.CLASS<? extends Field>> groupBy = new RCollection<Field.CLASS<? extends Field>>();

	public Field.CLASS<? extends Field> attachments;
	public Field.CLASS<? extends Field> period;

	private Collection<OBJECT.CLASS<? extends OBJECT>> links;

	private String alias;
	private SqlToken where;
	private SqlToken having;

	protected Select cursor;
	protected ReadLock readLock = ReadLock.None;
	private boolean transactive = false;

	private IAccess access;

	protected Query() {
		this(null);
	}

	protected Query(IObject container) {
		super(container);
	}

	@Override
	public void setContainer(IObject container) {
		super.setContainer(container);

		for(Field.CLASS<? extends Field> cls : dataFields)
			cls.resetId();

		for(Control.CLASS<? extends Control> cls : formFields)
			cls.resetId();

		for(Query.CLASS<? extends Query> cls : queries)
			cls.resetId();
	}

	public boolean equals(Query query) {
		return this == query;
	}

	public IAccess access() {
		return access != null ? access : (access = ApplicationServer.getUser().privileges().getAccess(this));
	}

	public void onNew(guid recordId, guid parentId) {
		if(ApplicationServer.events())
			z8_onNew(recordId, parentId);
	}

	public void onCopy() {
		if(ApplicationServer.events())
			z8_onCopy();
	}

	public void beforeRead(guid parentId) {
		if(ApplicationServer.events())
			z8_beforeRead(parentId);
	}

	public void afterRead(guid parentId) {
		if(ApplicationServer.events())
			z8_afterRead(parentId);
	}

	public void beforeCreate(guid recordId, guid parentId) {
		if(ApplicationServer.events())
			z8_beforeCreate(recordId, parentId);
	}

	public void afterCreate(guid recordId, guid parentId) {
		if(ApplicationServer.events())
			z8_afterCreate(recordId, parentId);
/*
		if(hasAttribute(IObject.SearchIndex) && !searchFields.isEmpty())
			SearchEngine.INSTANCE.updateRecord(this, recordId.toString());
*/
	}

	public void beforeUpdate(guid recordId) {
		if(ApplicationServer.events())
			z8_beforeUpdate(recordId);
	}

	public void afterUpdate(guid recordId) {
/*
		Collection<Field> changedFields = getChangedFields();

		if(hasAttribute(IObject.SearchIndex) && !searchFields.isEmpty()) {
			for(Field field : changedFields) {
				if(searchFields.contains(field.getCLASS())) {
					SearchEngine.INSTANCE.updateRecord(this, recordId.toString());
					break;
				}
			}
		}
*/
		if(ApplicationServer.events())
			z8_afterUpdate(recordId);
	}

	public void beforeDestroy(guid recordId) {
		if(ApplicationServer.events())
			z8_beforeDestroy(recordId);
	}

	public void afterDestroy(guid recordId) {
		if(ApplicationServer.events())
			z8_afterDestroy(recordId);
/*
		if(hasAttribute(IObject.SearchIndex) && !searchFields.isEmpty())
			SearchEngine.INSTANCE.deleteRecord(this, recordId.toString());
*/
	}

	@SuppressWarnings("unchecked")
	public void onCommand(Command command, Collection<guid> recordIds) {
		RCollection<guid> ids = getGuidCollection(recordIds);
		z8_onCommand((Command.CLASS<? extends Command>)command.getCLASS(), ids);
	}

	public Collection<Query> onReport(String report, Collection<guid> recordIds) {
		Collection<Query> queries = getReportQueries(report, recordIds);

		if(queries.isEmpty())
			queries.add(this);

		for(Query query : queries)
			onReport(query, report, recordIds);

		return queries;
	}

	private Collection<Query> getReportQueries(String report, Collection<guid> recordIds) {
		File reportFile = FileUtils.getFile(Folders.Base, Folders.Reports, report);

		BirtFileReader birtXMLReader = new BirtFileReader(reportFile);

		Collection<Query> result = new ArrayList<Query>();

		Collection<String> classNames = birtXMLReader.getDataSets();

		for(String className : classNames) {
			className = className.split(";")[0];
			result.add((Query)Loader.getInstance(className));
		}

		return result;
	}

	private void onReport(Query query, String report, Collection<guid> recordIds) {
		query.callOnReport(report, recordIds);
	}

	private void callOnReport(String report, Collection<guid> recordIds) {
		RCollection<guid> ids = getGuidCollection(recordIds);
		z8_onReport(new string(report), ids);
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

	private guid getParentId() {
		Field parentKey = parentKey();

		if(parentKey != null)
			return parentKey.changed() ? parentKey.guid() : guid.Null;

		return null;
	}

	private Collection<Field> getInsertFields(guid recordId, guid parentId) {
		Collection<Field> myFields = new ArrayList<Field>();
		Collection<Field> fields = getPrimaryFields();

		for(Field field : fields) {
			if(field.owner() != this)
				continue;

			if(field.isPrimaryKey() && !field.changed())
				field.set(recordId);

			if(field.isParentKey() && parentId != null && !field.changed())
				field.set(parentId);

			myFields.add(field);
		}

		return myFields;
	}

	private void executeInsert(Collection<Field> fields) {
		Insert insert = new Insert(this, fields);
		insert.execute();
	}

	public guid insert(guid recordId, guid parentId) {
		Collection<Field> fields = getInsertFields(recordId, parentId);

		beforeCreate(recordId, parentId);
		executeInsert(fields);
		afterCreate(recordId, parentId);

		recordId = primaryKey().guid();

		for(Field field : fields)
			field.reset();

		return recordId;
	}

	public guid create() {
		guid id = guid.create();
		return create(id, getParentId());
	}

	public guid create(guid recordId) {
		return create(recordId, guid.Null);
	}

	public guid create(guid recordId, guid parentId) {
		NewAction.run(this, recordId, parentId);
		return insert(recordId, parentId);
	}

	public guid copy(guid recordId) {
		guid parentId = getParentId();
		guid newRecordId = CopyAction.run(this, recordId, parentId);
		return insert(newRecordId, parentId);
	}

	public void read(Collection<Field> fields) {
		read(fields, -1);
	}

	public void read(Collection<Field> fields, int limit) {
		read(fields, -1, limit);
	}

	public void read(Collection<Field> fields, int start, int limit) {
		read(fields, null, start, limit);
	}

	public boolean readFirst(Collection<Field> fields) {
		read(fields, 1);
		return next();
	}

	public void read(Collection<Field> fields, SqlToken where) {
		read(fields, where, -1);
	}

	public void read(Collection<Field> fields, SqlToken where, int limit) {
		read(fields, where, -1, limit);
	}

	public void read(Collection<Field> fields, SqlToken where, int start, int limit) {
		read(fields, null, null, where, null, start, limit);
	}

	public boolean readFirst(Collection<Field> fields, SqlToken where) {
		read(fields, null, null, where, null, 0, 1);
		return next();
	}

	public void read(Collection<Field> fields, Collection<Field> sortFields, SqlToken where) {
		read(fields, sortFields, where, -1);
	}

	public void read(Collection<Field> fields, Collection<Field> sortFields, SqlToken where, int limit) {
		read(fields, sortFields, where, -1, limit);
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
		read(sortFields, where, -1, limit);
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
		group(null, groupFields, where, -1, limit);
	}

	public void group(Collection<Field> groupFields, SqlToken where, int start, int limit) {
		group(null, groupFields, where, start, limit);
	}

	public void group(Collection<Field> fields, Collection<Field> groupFields) {
		group(fields, groupFields, -1);
	}

	public void group(Collection<Field> fields, Collection<Field> groupFields, int limit) {
		group(fields, groupFields, -1, limit);
	}

	public void group(Collection<Field> fields, Collection<Field> groupFields, int start, int limit) {
		group(fields, groupFields, null, start, limit);
	}

	public void group(Collection<Field> fields, Collection<Field> groupFields, SqlToken where) {
		read(fields, groupFields, where, -1);
	}

	public void group(Collection<Field> fields, Collection<Field> groupFields, SqlToken where, SqlToken having) {
		read(fields, null, groupFields, where, having);
	}

	public void group(Collection<Field> fields, Collection<Field> groupFields, SqlToken where, int limit) {
		read(fields, groupFields, where, -1, limit);
	}

	public void group(Collection<Field> fields, Collection<Field> groupFields, SqlToken where, int start, int limit) {
		read(fields, null, groupFields, where, null, start, limit);
	}

	public boolean readRecord(guid id, Collection<Field> fields) {
		ReadAction action = new ReadAction(this, fields, id);
		action.addFilter(where);

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
		read(fields, sortFields, groupFields, where, having, -1, -1);
	}

	protected void read(Collection<Field> fields, Collection<Field> sortFields, Collection<Field> groupFields, SqlToken where, SqlToken having, int start, int limit) {
		ActionParameters parameters = new ActionParameters();
		parameters.query = this;
		parameters.fields = fields;
		parameters.sortFields = sortFields;
		parameters.groupBy = groupFields;

		ReadAction action = new ReadAction(parameters);
		action.addFilter(where);
		action.addGroupFilter(having);

		action.setLimit(limit);
		action.setStart(start);

		if(cursor != null)
			cursor.close();

		cursor = action.getCursor();
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

		read(fields, where);

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

		public State(Collection<Field> fields, Select cursor) {
			this.cursor = cursor;

			for(Field field : fields)
				fieldStates.add(new FieldState(field));
		}

		public Select cursor() {
			return cursor;
		}

		public void restore() {
			for(FieldState state : fieldStates)
				state.restore();
		}
	}

	public void saveState() {
		states.add(new State(getDataFields(), cursor));

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

	public Collection<Field.CLASS<? extends Field>> dataFields() {
		return dataFields;
	}

	public Collection<Field.CLASS<? extends Field>> primaryFields() {
		Collection<Field.CLASS<? extends Field>> fields = new ArrayList<Field.CLASS<? extends Field>>(50);

		for(Field.CLASS<? extends Field> field : dataFields()) {
			if(!(field instanceof Expression.CLASS))
				fields.add(field);
		}

		return fields;
	}

	public Collection<Field.CLASS<? extends Field>> formFields() {
		Set<Field.CLASS<? extends Field>> result = new LinkedHashSet<Field.CLASS<? extends Field>>(50);

		for(Control.CLASS<? extends Control> field : formFields) {
			if(field instanceof Section.CLASS)
				result.addAll(((Section)field.get()).fields());
			else if(field instanceof TabControl.CLASS)
				result.addAll(((TabControl)field.get()).fields());
			else if(field instanceof Field.CLASS)
				result.add((Field.CLASS<?>)field);
		}

		result.addAll(nameFields());
		result.addAll(columns());

		return result.isEmpty() ? dataFields() : result;
	}

	public Collection<Field.CLASS<? extends Field>> columns() {
		return columns;
	}

	public Collection<Field.CLASS<? extends Field>> nameFields() {
		return nameFields;
	}

	public Collection<Field.CLASS<? extends Field>> quickFilters() {
		return quickFilters;
	}

	public Collection<Field.CLASS<? extends Field>> sortFields() {
		return sortFields;
	}

	public Collection<Field.CLASS<? extends Field>> groupFields() {
		return groupFields;
	}

	private Collection<Control.CLASS<? extends Control>> defaultControls() {
		Collection<Control.CLASS<? extends Control>> result = new ArrayList<Control.CLASS<? extends Control>>();
		for(Field.CLASS<? extends Field> field : dataFields) {
			if(!field.system())
				result.add(field);
		}
		return result;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Collection<Control.CLASS<? extends Control>> controls() {
		return formFields.isEmpty() ? (Collection)defaultControls() : formFields;
	}

	public Collection<Query.CLASS<? extends Query>> queries() {
		return (Collection<Query.CLASS<? extends Query>>)queries;
	}

	public Collection<Query> getQueries() {
		return CLASS.asList(queries());
	}

	public Collection<Field> getDataFields() {
		return CLASS.asList(dataFields());
	}

	public Collection<Field> getPrimaryFields() {
		return CLASS.asList(primaryFields());
	}

	public Collection<Field> getFormFields() {
		return CLASS.asList(formFields());
	}

	public Collection<Field> getColumns() {
		return CLASS.asList(columns());
	}

	public Collection<Field> getNameFields() {
		return CLASS.asList(nameFields());
	}

	public Collection<Field> getQuickFilters() {
		return CLASS.asList(quickFilters());
	}

	public Collection<Field> getSortFields() {
		return CLASS.asList(sortFields());
	}

	public Collection<Field> getGroupFields() {
		return CLASS.asList(groupFields());
	}

	public Collection<Control> getControls() {
		return CLASS.asList(controls());
	}

	public Collection<Field> getSearchFields() {
		return CLASS.asList(searchFields);
	}

	public Collection<Field> getAttachments() {
		Collection<Field> result = new ArrayList<Field>();
		for(Field field : getDataFields()) {
			if(field instanceof AttachmentField)
				result.add(field);
		}
		return result;
	}

	public Field attachmentKey() {
		return attachments != null ? attachments.get() : null;
	}

	public Field periodKey() {
		return period != null ? period.get() : null;
	}

	public void registerDataField(Field.CLASS<?> field) {
		dataFields.add(field);
	}

	public void unregisterDataField(Field.CLASS<?> field) {
		dataFields.remove(field);
	}

	public void registerFormField(Control.CLASS<?> control) {
		formFields.add(control);
	}

	public void unregisterFormField(Control.CLASS<?> control) {
		formFields.remove(control);
	}

	final public SqlToken having() {
		if(having == null)
			having = z8_having();
		return having;
	}

	final public SqlToken where() {
		if(where == null)
			where = z8_where();
		return where;
	}

	final public void addWhere(String json) {
		addWhere(new Filter(json, this).where());
	}

	final public void addWhere(Collection<String> json) {
		addWhere(new Filter(json, this).where());
	}

	final public void addWhere(SqlToken where) {
		this.where = new And(where(), where);
	}

	final public void setWhere(String json) {
		setWhere(new Filter(json, this).where());
	}

	final public void setWhere(Collection<String> json) {
		setWhere(new Filter(json, this).where());
	}

	final public void setWhere(SqlToken where) {
		this.where = where;
	}

	public Collection<Link> getAggregateByFields() {
		return CLASS.asList(aggregateBy);
	}

	public Collection<Field> getGroupByFields() {
		return CLASS.asList(groupBy);
	}

	private RCollection<guid> getGuidCollection(Collection<guid> guids) {
		RCollection<guid> result = new RCollection<guid>();

		for(guid id : guids)
			result.add(id);

		return result;
	}

	public Field primaryKey() {
		return null;
	}

	public Field parentKey() {
		return null;
	}

	public Field[] parentKeys() {
		return new Field[0];
	}

	public boolean hasPrimaryKey() {
		return primaryKey() != null;
	}

	public Field lockKey() {
		return null;
	}

	public String getAlias() {
		if(alias == null) {
			String id = id().isEmpty() ? name() : id();
			alias = "T" + Math.abs(id.hashCode());
		}
		return alias;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void onInitialized() {
		super.onInitialized();

		for(Query.CLASS<? extends Query> query : queries())
			query.addReference((Query.CLASS)getCLASS());
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////
	// IQuery

	@Override
	public String toString() {
		return id();
	}

	public int getOwnersCount() {
		return owners().size();
	}

	@Override
	public Query getOwner() {
		return getOwnersCount() == 1 ? owners().iterator().next().get() : null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Collection<Query.CLASS<Query>> owners() {
		return (Collection)getCLASS().getReferences();
	}

	public Collection<Query> getOwners() {
		Collection<Query> result = new ArrayList<Query>();

		for(Query.CLASS<Query> owner : owners())
			result.add(owner.get());

		return result;
	}

	public Collection<OBJECT.CLASS<? extends OBJECT>> getLinks() {
		if(links == null) {
			links = new ArrayList<OBJECT.CLASS<? extends OBJECT>>(10);

			for(Field.CLASS<? extends Field> field : dataFields()) {
				if(field instanceof Link.CLASS || field instanceof LinkExpression.CLASS)
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

	private Query getQueryById(String id) {
		for(Query.CLASS<? extends Query> query : queries()) {
			if(query.id().startsWith(id()) && query.getIndex().equals(id))
				return query.get();
		}
		return null;
	}

	private Query getMatchedQuery(String id) {
		for(Query.CLASS<? extends Query> query : queries()) {
			if(id.startsWith(query.id()))
				return query.get();
		}

		return null;
	}

	private class FindQueryResult {
		public Query query = null;
		public Collection<Query> route = new ArrayList<Query>(10);
	}

	private FindQueryResult findQueryById(String id, boolean ignoreLast) {
		FindQueryResult result = new FindQueryResult();

		result.query = this;

		String[] path = parseId(id);

		if(path == null) {
			result.query = getMatchedQuery(id);

			if(result.query == null)
				return null;

			result.route.add(result.query);
			path = result.query.parseId(id);
		}

		int count = path.length - (ignoreLast ? 1 : 0);

		if(path.length == 0 || count == 0)
			return result;

		for(int i = 0; i < count; i++) {
			result.query = result.query.getQueryById(path[i]);
			result.route.add(result.query);

			if(result.query == null)
				return null;
		}

		return result;
	}

	public Query findQueryById(String id) {
		FindQueryResult result = findQueryById(id, false);
		return result != null ? result.query : null;
	}

	public Query findQueryByFieldId(String id) {
		FindQueryResult result = findQueryById(id, true);
		return result != null ? result.query : null;
	}

	public Collection<Query> getRoute(Query query) {
		FindQueryResult result = findQueryById(query.id(), false);
		return result != null ? result.route : null;
	}

	private Collection<ILink> getRouteByOwners(Query query) {
		List<ILink> path = new ArrayList<ILink>();

		if(query == this)
			return path;

		Query owner = null;

		int ownersCount = query.getOwnersCount();

		if(ownersCount > 1) {
			for(Query o : query.getOwners()) {
				if(!getRouteByOwners(o).isEmpty() || this == o) {
					owner = o;
					break;
				}
			}
		} else if(ownersCount == 1)
			owner = query.getOwner();

		Query rootQuery = this;

		while(owner != null && query != this && query != rootQuery) {
			Query root = owner;

			while(query != root) {
				ILink link = owner.getLinkTo(query);

				if(link != null) {
					path.add(0, link);
					query = owner;
				} else
					break;
			}

			query = owner;

			if(query.getOwnersCount() > 1) {
				Collection<ILink> pathFromTop = rootQuery.getRouteByOwners(query);
				pathFromTop.addAll(path);
				return pathFromTop;
			}

			owner = owner.getOwner();
		}

		return owner == null ? new ArrayList<ILink>() : path;
	}

	public Collection<Query> getRoute(Field field) {
		FindQueryResult result = findQueryById(field.id(), true);
		return result != null ? result.route : null;
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
		Collection<Query> route = getRoute(query);

		if(route == null)
			return getRouteByOwners(query);

		Collection<ILink> path = new ArrayList<ILink>(10);

		Query current = this;

		for(Query q : route) {
			if(current != q) {
				ILink link = current.getLinkTo(q);

				if(link != null)
					path.add(link);
			}
			current = q;
		}
		return path;
	}

	public Collection<ILink> getPath(Field field) {
		Collection<Query> route = getRoute(field);

		if(route == null)
			return getRouteByOwners(field.owner());

		Collection<ILink> path = new ArrayList<ILink>(10);

		Query current = this;

		for(Query query : route) {
			if(query != current) {
				ILink link = current.getLinkTo(query);

				if(link != null)
					path.add(link);
			}
			current = query;
		}

		return path;
	}

	public Field findFieldById(String id) {
		FindQueryResult result = findQueryById(id, true);
		return result != null ? result.query.getFieldById(id) : null;
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

	private boolean isReachableVia(Query query, Field field) {
		if(field instanceof Expression) {
			Expression expression = (Expression)field;
			SqlToken token = expression.expression();

			Collection<IField> values = token.getUsedFields();

			for(IField value : values) {
				if(query.findFieldById(value.id()) == null)
					return false;
			}
		}

		return query.findFieldById(field.id()) != null;
	}

	public Collection<Field> getFieldsVia(Query query) {
		if(this == query)
			return getFormFields();

		Collection<Field> result = new ArrayList<Field>(50);

		for(Field field : getFormFields()) {
			if(isReachableVia(query, field))
				result.add(field);
		}

		return result;
	}

	public Collection<Field> getReachableFields(Collection<Field> fields) {
		Collection<Field> result = new ArrayList<Field>(50);

		for(Field field : fields) {
			if(findFieldById(field.id()) != null)
				result.add(field);
		}

		return result;
	}

	public Collection<Command> commands() {
		return CLASS.asList(commands);
	}

	public Collection<Command> getCommands() {
		return commands();
	}

	public Command getCommand(String id) {
		for(Command command : getCommands()) {
			if(command.id().equals(id))
				return command;
		}

		return null;
	}

	private boolean readOnly() {
		if(readOnly.get())
			return true;
		return !access().write();
	}

/*
	private String getSearchValue(Field field) {
		if(field instanceof AttachmentField) {
			Collection<file> files = file.parse(field.string().get());

			String result = "";

			for(file file : files)
				result += (result.isEmpty() ? "" : " ") + file.name;

			return result;
		}
		return field.get().toString();
	}
*/
	public String getRecordFullText() {
/*
		Collection<Field> searchFields = getSearchFields();
*/
		String result = "";
/*
		for(Field field : searchFields) {
			if(field.type() != FieldType.Guid)
				result += (result.isEmpty() ? "" : " ") + getSearchValue(field);
		}
*/
		return result;
	}

	public Field getSearchId() {
		return searchId != null ? searchId.get() : primaryKey();
	}

	public void writeMeta(JsonWriter writer, Collection<Field> fields) {
		writer.writeProperty(Json.isQuery, true);

		writer.writeProperty(Json.id, id());
		writer.writeProperty(Json.icon, icon());
		writer.writeProperty(Json.form, form());
		writer.writeProperty(Json.sourceCode, sourceCodeLocation());

		writer.writeControls(Json.fields, fields, this);
		writer.writeControls(Json.controls, getControls(), this);
		writer.writeControls(Json.columns, getColumns(), this);
		writer.writeControls(Json.nameFields, getNameFields(), this);
		writer.writeControls(Json.quickFilters, getQuickFilters(), this);

		writeKeys(writer, fields);
		writeCommands(writer);

		// visuals
		writer.writeProperty(Json.text, displayName());

		boolean isGrouped = !getGroupByFields().isEmpty() || !getAggregateByFields().isEmpty();
		writer.writeProperty(Json.readOnly, isGrouped ? true : readOnly());

		writer.writeProperty(Json.totals, totals);
		writer.writeProperty(Json.columnCount, columnCount);
	}

	private void writeKeys(JsonWriter writer, Collection<Field> fields) {
		Field primaryKey = primaryKey();
		if(primaryKey != null && fields.contains(primaryKey))
			writer.writeProperty(Json.primaryKey, primaryKey.id());

		Field lockKey = lockKey();
		if(lockKey != null && fields.contains(lockKey))
			writer.writeProperty(Json.lockKey, lockKey.id());

		Field attachments = attachmentKey();
		if(attachments != null && fields.contains(attachments))
			writer.writeProperty(Json.attachmentsKey, attachments.id());

		Field period = periodKey();
		if(period != null)
			writer.writeProperty(Json.periodKey, period.id());

		Field parentKey = parentKey();
		if(parentKey != null && fields.contains(parentKey))
			writer.writeProperty(Json.parentKey, parentKey().id());
	}

	private void writeCommands(JsonWriter writer) {
		writer.startArray(Json.commands);

		for(ICommand command : getCommands()) {
			writer.startObject();
			command.write(writer);
			writer.finishObject();
		}

		writer.finishArray();
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////

	public sql_bool z8_having() {
		return new True();
	}

	public sql_bool z8_where() {
		return new True();
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
		setWhere(json.get());
	}

	public void z8_setWhere(RCollection<string> json) {
		setWhere(string.unwrap(json));
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
	public Field.CLASS<? extends Field> z8_getFieldById(string id) {
		return (Field.CLASS)getFieldById(id.get()).getCLASS();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Field.CLASS<? extends Field> z8_getFieldByName(string name) {
		return (Field.CLASS)getFieldByName(name.get()).getCLASS();
	}

	public guid z8_create() {
		try {
			return create();
		} catch(Throwable e) {
			throw new exception(e);
		}
	}

	public guid z8_create(guid recordId) {
		try {
			return create(recordId);
		} catch(Throwable e) {
			throw new exception(e);
		}
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

	public void z8_onNew(guid recordId, guid parentId) {
	}

	public void z8_onCopy() {
	}

	public void z8_beforeRead(guid parentId) {
	}

	public void z8_afterRead(guid parentId) {
	}

	public void z8_beforeCreate(guid recordId, guid parentId) {
	}

	public void z8_afterCreate(guid recordId, guid parentId) {
	}

	public void z8_beforeUpdate(guid recordId) {
	}

	public void z8_afterUpdate(guid recordId) {
	}

	public void z8_beforeDestroy(guid recordId) {
	}

	public void z8_afterDestroy(guid recordId) {
	}

	@SuppressWarnings("rawtypes")
	public void z8_onCommand(Command.CLASS<? extends Command> command, RCollection recordIds) {
	}

	@SuppressWarnings("rawtypes")
	public void z8_onReport(string report, RCollection recordIds) {
	}
}
