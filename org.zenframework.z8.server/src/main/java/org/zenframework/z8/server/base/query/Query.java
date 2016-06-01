package org.zenframework.z8.server.base.query;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.zenframework.z8.server.base.file.Folders;
import org.zenframework.z8.server.base.form.Control;
import org.zenframework.z8.server.base.form.FieldGroup;
import org.zenframework.z8.server.base.model.actions.ActionParameters;
import org.zenframework.z8.server.base.model.actions.CopyAction;
import org.zenframework.z8.server.base.model.actions.DestroyAction;
import org.zenframework.z8.server.base.model.actions.NewAction;
import org.zenframework.z8.server.base.model.actions.ReadAction;
import org.zenframework.z8.server.base.model.actions.UpdateAction;
import org.zenframework.z8.server.base.model.command.ICommand;
import org.zenframework.z8.server.base.model.sql.Insert;
import org.zenframework.z8.server.base.model.sql.Select;
import org.zenframework.z8.server.base.simple.Runnable;
import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.TreeTable;
import org.zenframework.z8.server.base.table.value.AttachmentExpression;
import org.zenframework.z8.server.base.table.value.AttachmentField;
import org.zenframework.z8.server.base.table.value.Expression;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.FollowPolicy;
import org.zenframework.z8.server.base.table.value.GuidField;
import org.zenframework.z8.server.base.table.value.ILink;
import org.zenframework.z8.server.base.table.value.IValue;
import org.zenframework.z8.server.base.table.value.Link;
import org.zenframework.z8.server.base.table.value.LinkExpression;
import org.zenframework.z8.server.base.view.command.Command;
import org.zenframework.z8.server.base.view.filter.Filter;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.expressions.And;
import org.zenframework.z8.server.db.sql.expressions.Group;
import org.zenframework.z8.server.db.sql.expressions.True;
import org.zenframework.z8.server.db.sql.functions.InVector;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.reports.BirtFileReader;
import org.zenframework.z8.server.reports.ReportBindingFileReader;
import org.zenframework.z8.server.reports.ReportInfo;
import org.zenframework.z8.server.request.IMonitor;
import org.zenframework.z8.server.request.Loader;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.search.SearchEngine;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.decimal;
import org.zenframework.z8.server.types.exception;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.types.sql.sql_bool;

public class Query extends Runnable {
    static public class strings {
        public final static String ReadError = "Query.readError";
    }

    public static class CLASS<T extends Query> extends Runnable.CLASS<T> {
        public CLASS(IObject container) {
            super(container);
            setJavaClass(Query.class);
            setAttribute(Native, Query.class.getCanonicalName());
        }

        @Override
        public Object newObject(IObject container) {
            return new Query(container);
        }
    }

    public Query.CLASS<? extends Query> model;

    public bool readOnly = new bool(false);

    public bool showAsGrid = new bool(true); // to show treeTable as an ordinary
                                             // table
    public ViewMode viewMode = ViewMode.Table;

    public bool showTotals = new bool(false);
    public bool visible = new bool(true);
    public bool collapseGroups = new bool(false);
    public bool printAsList = new bool(false);

    public integer columns = new integer(3);
    public decimal width = new decimal(0);
    public decimal height = new decimal(0);

    public bool parentsSelectable = new bool(true);

    public RCollection<Query.CLASS<? extends Query>> queries = new RCollection<Query.CLASS<? extends Query>>(true);

    public RCollection<Field.CLASS<? extends Field>> sortFields = new RCollection<Field.CLASS<? extends Field>>(true);
    public RCollection<Field.CLASS<? extends Field>> groupFields = new RCollection<Field.CLASS<? extends Field>>(true);
    public RCollection<Field.CLASS<? extends Field>> searchFields = new RCollection<Field.CLASS<? extends Field>>(true);
    public Field.CLASS<? extends Field> searchId;

    public DataFields dataFields = new DataFields(this);
    public FormFields formFields = new FormFields(this);

    public ChartType chartType = ChartType.Column;

    public RCollection<Field.CLASS<? extends Field>> chartSeries = new RCollection<Field.CLASS<? extends Field>>(true);

    public RCollection<Command.CLASS<? extends Command>> commands = new RCollection<Command.CLASS<? extends Command>>(
            true);

    public RCollection<guid> recordIds = new RCollection<guid>();

    public RCollection<guid> filterBy = new RCollection<guid>(true);

    public RCollection<Link.CLASS<? extends Link>> aggregateBy = new RCollection<Link.CLASS<? extends Link>>(true);
    public RCollection<Field.CLASS<? extends Field>> groupBy = new RCollection<Field.CLASS<? extends Field>>(true);

    public Period.CLASS<? extends Period> period = null;

    public RCollection<RecordActions> recordActions = new RCollection<RecordActions>();

    private Query contextQuery;
    private Query[] rootQueries;

    private Collection<OBJECT.CLASS<? extends OBJECT>> links = null;

    private Field[] primaryKeys = null;

    private SqlToken where = null;
    private SqlToken having = null;

    protected Select cursor;
    protected ReadLock readLock = ReadLock.None;

    protected Query() {
        this(null);
    }

    protected Query(IObject container) {
        super(container);
    }

    @Override
    public void constructor2() {
        super.constructor2();

        recordActions.add(RecordActions.Add);
        recordActions.add(RecordActions.Copy);
        recordActions.add(RecordActions.Delete);
    }

    public boolean equals(Query anObject) {
        return this == anObject;
    }

    @SuppressWarnings("unchecked")
    private Query.CLASS<? extends Query> myClass() {
        return (Query.CLASS<? extends Query>) this.getCLASS();
    }

    public void onNew(guid recordId, guid parentId, guid modelRecordId) {
        if (contextQuery != null)
            contextQuery.onNew(this, recordId, parentId, modelRecordId);

        if (contextQuery != this)
            onNew(this, recordId, parentId, modelRecordId);

        Query rootQuery = getRootQuery();

        if (rootQuery != this)
            rootQuery.onNew(rootQuery, recordId, parentId, modelRecordId);
    }

    protected void onNew(Query data, guid recordId, guid parentId, guid modelRecordId) {
        if (ApplicationServer.events())
            z8_onNew(data.myClass(), recordId, parentId, modelRecordId);
    }

    public void onCopy() {
        if (contextQuery != null)
            contextQuery.onCopy(this);

        if (contextQuery != this)
            onCopy(this);

        Query rootQuery = getRootQuery();

        if (rootQuery != this)
            rootQuery.onCopy(rootQuery);
    }

    protected void onCopy(Query data) {
        if (ApplicationServer.events())
            z8_onCopy(data.myClass());
    }

    public void beforeRead(guid parentId, Query model, guid modelRecordId) {
        if (contextQuery != null)
            contextQuery.beforeRead(this, parentId, model, modelRecordId);

        if (contextQuery != this)
            beforeRead(this, parentId, model, modelRecordId);

        Query rootQuery = getRootQuery();

        if (rootQuery != this)
            rootQuery.beforeRead(rootQuery, parentId, model, modelRecordId);
    }

    protected void beforeRead(Query data, guid parentId, Query model, guid modelRecordId) {
        if (ApplicationServer.events())
            z8_beforeRead(data.myClass(), parentId, model.myClass(), modelRecordId);
    }

    public void afterRead(guid parentId, Query model, guid modelRecordId) {
        if (contextQuery != null)
            contextQuery.afterRead(this, parentId, model, modelRecordId);

        if (contextQuery != this)
            afterRead(this, parentId, model, modelRecordId);

        Query rootQuery = getRootQuery();

        if (rootQuery != this)
            rootQuery.afterRead(rootQuery, parentId, model, modelRecordId);
    }

    protected void afterRead(Query data, guid parentId, Query model, guid modelRecordId) {
        if (ApplicationServer.events())
            z8_afterRead(data.myClass(), parentId, model.myClass(), modelRecordId);
    }

    public void beforeCreate(guid recordId, guid parentId, Query model, guid modelRecordId) {
        if (contextQuery != null)
            contextQuery.beforeCreate(this, recordId, parentId, model, modelRecordId);

        if (contextQuery != this)
            beforeCreate(this, recordId, parentId, model, modelRecordId);

        Query rootQuery = getRootQuery();

        if (rootQuery != this)
            rootQuery.beforeCreate(rootQuery, recordId, parentId, model, modelRecordId);
    }

    protected void beforeCreate(Query data, guid recordId, guid parentId, Query model, guid modelRecordId) {
        if (ApplicationServer.events())
            z8_beforeCreate(data.myClass(), recordId, parentId, model.myClass(), modelRecordId);
    }

    public void afterCreate(guid recordId, guid parentId, Query model, guid modelRecordId) {
        if (contextQuery != null)
            contextQuery.afterCreate(this, recordId, parentId, model, modelRecordId);

        if (contextQuery != this)
            afterCreate(this, recordId, parentId, model, modelRecordId);

        Query rootQuery = getRootQuery();

        if (rootQuery != this)
            rootQuery.afterCreate(rootQuery, recordId, parentId, model, modelRecordId);
    }

    protected void afterCreate(Query data, guid recordId, guid parentId, Query model, guid modelRecordId) {
        if (ApplicationServer.events())
            z8_afterCreate(data.myClass(), recordId, parentId, model.myClass(), modelRecordId);

        if (hasAttribute(IObject.SearchIndex) && !searchFields.isEmpty())
            SearchEngine.INSTANCE.updateRecord(this, recordId.toString());
    }

    public void beforeUpdate(guid recordId, Collection<Field> fields, Query model, guid modelId) {
        if (contextQuery != null)
            contextQuery.beforeUpdate(this, recordId, fields, model, modelId);

        if (contextQuery != this)
            beforeUpdate(this, recordId, fields, model, modelId);

        Query rootQuery = getRootQuery();

        if (rootQuery != this)
            rootQuery.beforeUpdate(rootQuery, recordId, fields, model, modelId);
    }

    @SuppressWarnings("unchecked")
    protected void beforeUpdate(Query data, guid recordId, Collection<Field> fields, Query model, guid modelRecordId) {
        RCollection<Field.CLASS<? extends Field>> changedFields = new RCollection<Field.CLASS<? extends Field>>();

        for (Field field : fields)
            changedFields.add((Field.CLASS<? extends Field>) field.getCLASS());

        if (ApplicationServer.events())
            z8_beforeUpdate(data.myClass(), recordId, changedFields, model.myClass(), modelRecordId);
    }

    public void afterUpdate(guid recordId, Collection<Field> fields, Query model, guid modelId) {
        if (contextQuery != null)
            contextQuery.afterUpdate(this, recordId, fields, model, modelId);

        if (contextQuery != this)
            afterUpdate(this, recordId, fields, model, modelId);

        Query rootQuery = getRootQuery();

        if (rootQuery != this)
            rootQuery.afterUpdate(rootQuery, recordId, fields, model, modelId);
    }

    @SuppressWarnings("unchecked")
    protected void afterUpdate(Query data, guid recordId, Collection<Field> fields, Query model, guid modelRecordId) {
        RCollection<Field.CLASS<? extends Field>> changedFields = new RCollection<Field.CLASS<? extends Field>>();

        for (Field field : fields)
            changedFields.add((Field.CLASS<? extends Field>) field.getCLASS());

        if (hasAttribute(IObject.SearchIndex) && !searchFields.isEmpty()) {
            for (Field.CLASS<? extends Field> field : changedFields) {
                if (searchFields.contains(field)) {
                    SearchEngine.INSTANCE.updateRecord(this, recordId.toString());
                    break;
                }
            }
        }

        if (ApplicationServer.events())
            z8_afterUpdate(data.myClass(), recordId, changedFields, model.myClass(), modelRecordId);
    }

    public void beforeDestroy(guid recordId, Query model, guid modelRecordId) {
        if (contextQuery != null)
            contextQuery.beforeDestroy(this, recordId, model, modelRecordId);

        if (contextQuery != this)
            beforeDestroy(this, recordId, model, modelRecordId);

        Query rootQuery = getRootQuery();

        if (rootQuery != this)
            rootQuery.beforeDestroy(rootQuery, recordId, model, modelRecordId);
    }

    protected void beforeDestroy(Query data, guid recordId, Query model, guid modelRecordId) {
        if (ApplicationServer.events())
            z8_beforeDestroy(data.myClass(), recordId, model.myClass(), modelRecordId);
    }

    public void afterDestroy(guid recordId, Query model, guid modelRecordId) {
        if (contextQuery != null)
            contextQuery.afterDestroy(this, recordId, model, modelRecordId);

        if (contextQuery != this)
            afterDestroy(this, recordId, model, modelRecordId);

        Query rootQuery = getRootQuery();

        if (rootQuery != this)
            rootQuery.afterDestroy(rootQuery, recordId, model, modelRecordId);
    }

    protected void afterDestroy(Query data, guid recordId, Query model, guid modelRecordId) {
        if (ApplicationServer.events())
            z8_afterDestroy(data.myClass(), recordId, model.myClass(), modelRecordId);

        if (hasAttribute(IObject.SearchIndex) && !searchFields.isEmpty())
            SearchEngine.INSTANCE.deleteRecord(this, recordId.toString());
    }

    public void onRender() {
        if (contextQuery != null)
            contextQuery.onRender(this);

        if (contextQuery != this)
            onRender(this);

        Query rootQuery = getRootQuery();

        if (rootQuery != this)
            rootQuery.onRender(this);
    }

    protected void onRender(Query data) {
        z8_onRender(data.myClass());
    }

    public Style renderRecord() {
        Style style = null;

        if (contextQuery != null)
            style = contextQuery.renderRecord(this);

        if (style != null)
            return style;

        if (contextQuery != this)
            style = renderRecord(this);

        if (style != null)
            return style;

        Query rootQuery = getRootQuery();

        if (rootQuery != null && rootQuery != this)
            style = rootQuery.renderRecord(rootQuery);

        return style;
    }

    protected Style renderRecord(Query query) {
        Style.CLASS<? extends Style> style = z8_renderRecord(query.myClass());
        return style != null ? style.get() : null;
    }

    public void onCommand(Command command, Collection<guid> recordIds) {
        if (contextQuery != null)
            contextQuery.onCommand(this, command, recordIds);

        if (contextQuery != this)
            onCommand(this, command, recordIds);

        Query rootQuery = getRootQuery();

        if (rootQuery != this)
            rootQuery.onCommand(rootQuery, command, recordIds);
    }

    @SuppressWarnings("unchecked")
    protected void onCommand(Query data, Command command, Collection<guid> recordIds) {
        RCollection<guid> ids = getGuidCollection(recordIds);
        z8_onCommand(data.myClass(), (Command.CLASS<? extends Command>) command.getCLASS(), ids);
    }

    public Collection<Query> onReport(String report, Collection<guid> recordIds) {
        Collection<Query> queries = getReportQueries(report, recordIds);

        if (queries.isEmpty())
            queries.add(this);

        for (Query query : queries)
            onReport(query, report, recordIds);

        return queries;
    }

    private Collection<Query> getReportQueries(String report, Collection<guid> recordIds) {
        File reportFile = FileUtils.getFile(Folders.Base, Folders.Reports, report);

        BirtFileReader birtXMLReader = new BirtFileReader(reportFile);

        Collection<Query> result = new ArrayList<Query>();

        Collection<String> classNames = birtXMLReader.getDataSets();

        for (String className : classNames) {
            className = className.split(";")[0];
            result.add((Query) Loader.getInstance(className));
        }

        return result;
    }

    private void onReport(Query query, String report, Collection<guid> recordIds) {
        Query contextQuery = query.getContext();

        if (contextQuery != null)
            contextQuery.callOnReport(this, report, recordIds);

        if (contextQuery != query)
            query.callOnReport(this, report, recordIds);

        Query rootQuery = query.getRootQuery();

        if (rootQuery != query)
            rootQuery.callOnReport(rootQuery, report, recordIds);
    }

    private void callOnReport(Query data, String report, Collection<guid> recordIds) {
        RCollection<guid> ids = getGuidCollection(recordIds);
        z8_onReport(data.myClass(), new string(report), ids);
    }

    public Query onFollow(Field field, Collection<guid> recordIds) {
        Query result = null;

        if (contextQuery != null)
            result = contextQuery.callOnFollow(field, recordIds);

        if (result == null)
            result = callOnFollow(field, recordIds);

        if (result == null) {
            Query rootQuery = getRootQuery();

            if (rootQuery != this)
                result = rootQuery.callOnFollow(field, recordIds);
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private Query callOnFollow(Field field, Collection<guid> recordIds) {
        RCollection<guid> ids = getGuidCollection(recordIds);
        Query.CLASS<? extends Query> cls = z8_onFollow((Field.CLASS<? extends Field>) field.getCLASS(), ids);
        return cls != null ? (Query) cls.get() : null;
    }

    public guid recordId() {
        return (guid) primaryKey().get();
    }

    public ReadLock getReadLock() {
        return readLock;
    }

    public void setReadLock(ReadLock readLock) {
        this.readLock = readLock;
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
        while (next())
            records.add(primaryKey().guid());
        return records;
    }

    public int count() {
        return count(null);
    }

    public int count(SqlToken where) {
    	try {
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

        if (cursor != null)
            cursor.close();

        cursor = action.getTotals();

        return cursor.next();
    }

    private guid getParentId() {
        Field parentKey = getRootQuery().parentKey();

        if (parentKey != null)
            return parentKey.changed() ? parentKey.guid() : guid.NULL;

        return null;
    }

    private Collection<Field> getInsertFields(guid recordId, guid parentId) {
        Query query = getRootQuery();

        Collection<Field> myFields = new ArrayList<Field>();
        Collection<Field> fields = query.getDataFields();

        for (Field field : fields) {
            if (field.owner() == query) {
                if (!field.changed())
                    field.set(field.getDefault());

                if (field.isPrimaryKey() && !field.changed())
                    field.set(recordId);

                if (field.isParentKey() && parentId != null && !field.changed())
                    field.set(parentId);

                myFields.add(field);
            }
        }

        return myFields;
    }

    /*
        public Statement batchStatement = null;

        public boolean isBatching() {
            return batchStatement != null;
        }
        
        public void startBatch() {
            batchStatement = new BatchStatement();
            batchStatement.statement()..
        }
        
        public void finishBatch() {
            batchStatement.executeBatch();
            batchStatement.close();
        }
    */
    public void executeInsert(Collection<Field> fields) {
        Query rootQuery = getRootQuery();

        Insert insert = new Insert(rootQuery, fields);

        try {
            /*
                        if(isBatching())
                            batchStatement.addBatch(insert);
                        else
            */
            insert.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public guid insert(guid recordId, guid parentId, guid modelRecordId) {
        Query model = getModel(this);

        Collection<Field> fields = getInsertFields(recordId, parentId);

        try {
            beforeCreate(recordId, parentId, model, modelRecordId);

            executeInsert(fields);

            afterCreate(recordId, parentId, model, modelRecordId.equals(guid.NULL) ? recordId : modelRecordId);
        } catch (exception e) {
            throw e;
        }

        recordId = primaryKey().guid();

        for (Field field : fields)
            field.reset();

        return recordId;
    }

    private guid getModelRecordId(guid id) {
        Query context = getContext();
        return context != null && context.getModel() == this ? id : guid.NULL;
    }

    public guid create() {
        guid id = guid.create();
        return create(id, getParentId(), getModelRecordId(id));
    }

    public guid create(guid recordId) {
        return create(recordId, guid.NULL, guid.NULL);
    }

    public guid create(guid recordId, guid parentId, guid modelRecordId) {
        NewAction.run(this, recordId, parentId, modelRecordId);
        return insert(recordId, parentId, modelRecordId);
    }

    public guid copy(guid recordId) {
        guid parentId = getParentId();
        guid newRecordId = CopyAction.run(this, recordId, parentId, guid.NULL);
        return insert(newRecordId, parentId, getModelRecordId(newRecordId));
    }

    public boolean readRecord(guid id) {
        return readRecord(id, (Collection<Field>) null);
    }

    public void read() {
        read((Collection<Field>) null, null, null, null, null);
    }

    public boolean readFirst() {
        read();
        return next();
    }

    public void read(SqlToken where) {
        read(null, where);
    }

    public boolean readFirst(SqlToken where) {
        read(where);
        return next();
    }

    public void read(Collection<Field> fields) {
        read(fields, null);
    }

    public boolean readFirst(Collection<Field> fields) {
        read(fields);
        return next();
    }

    public void read(Collection<Field> fields, SqlToken where) {
        read(fields, null, null, where, null);
    }

    public boolean readFirst(Collection<Field> fields, SqlToken where) {
        read(fields, null, null, where, null);
        return next();
    }

    public void read(Collection<Field> fields, Collection<Field> sortFields, SqlToken where) {
        read(fields, sortFields, null, where, null);
    }

    public boolean readFirst(Collection<Field> fields, Collection<Field> sortFields, SqlToken where) {
        read(fields, sortFields, null, where, null);
        return next();
    }

    public void sort(Collection<Field> sortFields, SqlToken where) {
        read(null, sortFields, where);
    }

    public void sort(Collection<Field> fields, Collection<Field> sortFields, SqlToken where) {
        read(fields, null, sortFields, where, null);
    }

    public void group(Collection<Field> groupFields, SqlToken where) {
        group(null, groupFields, where);
    }

    public void group(Collection<Field> fields, Collection<Field> groupFields, SqlToken where) {
        read(fields, null, groupFields, where, null);
    }

    public boolean readRecord(guid id, Collection<Field> fields) {
        assert (id != null);

        ReadAction action = new ReadAction(this, fields, id);
        action.addFilter(where);

        if (cursor != null)
            cursor.close();

        cursor = action.getCursor();
        return cursor.next();
    }

    protected boolean readFirst(Collection<Field> fields, Collection<Field> sortFields, Collection<Field> groupFields,
            SqlToken where, SqlToken having) {
        read(fields, sortFields, groupFields, where, having);
        return next();
    }

    protected void read(Collection<Field> fields, Collection<Field> sortFields, Collection<Field> groupFields,
            SqlToken where, SqlToken having) {

        ActionParameters parameters = new ActionParameters();
        parameters.query = this;
        parameters.fields = fields;
        parameters.sortFields = sortFields;
        parameters.groupBy = groupFields;

        ReadAction action = new ReadAction(parameters);
        action.addFilter(where);
        action.addGroupFilter(having);

        if (cursor != null)
            cursor.close();

        cursor = action.getCursor();
    }

    public Collection<Field> getChangedFields() {
        Query query = getRootQuery();

        Collection<Field> fields = new ArrayList<Field>();

        for (Field.CLASS<? extends Field> field : query.primaryFields()) {
            if (field.hasInstance() && field.get().changed())
                fields.add(field.get());
        }

        return fields;
    }

    public int update(guid id) {
        Collection<Field> fields = getChangedFields();
        return UpdateAction.run(this, id, fields, getModelRecordId(id));
    }

    public int update(SqlToken where) {
        Collection<Field> changedFields = getChangedFields();

        Collection<Field> fields = new ArrayList<Field>();
        fields.add(primaryKey());

        read(fields, where);

        int result = 0;

        while (next()) {
            guid id = recordId();
            result += UpdateAction.run(this, id, changedFields, getModelRecordId(id), false);
        }

        for (Field field : changedFields)
            field.reset();

        return result;
    }

    public int destroy(guid id) {
        return DestroyAction.run(this, id, getModelRecordId(id));
    }

    public int destroy(SqlToken where) {
        Collection<Field> fields = new ArrayList<Field>();
        fields.add(primaryKey());

        read(fields, where);

        int result = 0;

        while (next()) {
            guid id = recordId();
            result += DestroyAction.run(this, id, getModelRecordId(id));
        }

        return result;
    }

    private boolean readRecord(guid id, RCollection<Field.CLASS<Field>> fieldClasses) {
        Collection<Field> fields = CLASS.asList(fieldClasses);

        fields = fields.isEmpty() ? null : fields;

        return readRecord(id, fields);
    }

    private void read1(RCollection<Field.CLASS<Field>> fieldClasses, RCollection<Field.CLASS<Field>> sortClasses,
            RCollection<Field.CLASS<Field>> groupClasses, SqlToken where, SqlToken having) {
        Collection<Field> fields = CLASS.asList(fieldClasses);
        Collection<Field> sortFields = CLASS.asList(sortClasses);
        Collection<Field> groupFields = CLASS.asList(groupClasses);

        fields = fields.isEmpty() ? null : fields;
        sortFields = sortFields.isEmpty() ? null : sortFields;
        groupFields = groupFields.isEmpty() ? null : groupFields;

        read(fields, sortFields, groupFields, where, having);
    }

    public boolean next() {
        if (cursor == null)
            throw new RuntimeException("Method Query.read() should be called before Query.next()");

        return cursor.next();
    }

    public boolean isAfterLast() {
        if (cursor == null)
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

            if (changed) {
                value = field.get();
                field.reset();
            }
        }

        public void restore() {
            if (changed)
                field.set(value);
        }
    }

    private static class State {
        private Select cursor;
        private Collection<FieldState> fieldStates = new ArrayList<FieldState>();

        public State(Collection<Field> fields, Select cursor) {
            this.cursor = cursor;

            for (Field field : fields)
                fieldStates.add(new FieldState(field));
        }

        public Select cursor() {
            return cursor;
        }

        public void restore() {
            for (FieldState state : fieldStates)
                state.restore();
        }
    }

    public void saveState() {
        states.add(new State(getDataFields(), cursor));

        if (cursor != null)
            cursor.saveState();

        cursor = null;
    }

    public void restoreState() {
        if (cursor != null)
            cursor.close();

        State state = states.remove(states.size() - 1);

        cursor = state.cursor();

        if (cursor != null)
            cursor.restoreState();

        state.restore();
    }

    public Collection<Field.CLASS<? extends Field>> dataFields() {
        return dataFields;
    }

    public Collection<Field.CLASS<? extends Field>> formFields() {
        if (formFields.isEmpty())
            return dataFields();

        List<Field.CLASS<? extends Field>> result = new ArrayList<Field.CLASS<? extends Field>>();

        for (Control.CLASS<? extends Control> formField : formFields) {
            if (formField instanceof Field.CLASS) {
                result.add((Field.CLASS<?>) formField);
            } else if (formField instanceof FieldGroup.CLASS) {
                FieldGroup group = (FieldGroup) formField.get();
                result.addAll(group.fields());
            }
        }
        return result;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Collection<Control.CLASS<? extends Control>> controls() {
        return formFields.isEmpty() ? (Collection) dataFields() : formFields;
    }

    public Collection<Query.CLASS<? extends Query>> queries() {
        return (Collection<Query.CLASS<? extends Query>>) queries;
    }

    public Collection<Query> getQueries() {
        return CLASS.asList(queries());
    }

    public List<Field> getDataFields() {
        return CLASS.asList(dataFields());
    }

    public List<Field> getFormFields() {
        return CLASS.asList(formFields());
    }

    public List<Control> getControls() {
        return CLASS.asList(controls());
    }

    public List<Field> getSearchFields() {
        return CLASS.asList(searchFields);
    }

    public Collection<Field> getAttachments() {
        Collection<Field> result = new ArrayList<Field>();
        for (Field field : getDataFields()) {
            if (field instanceof AttachmentField || field instanceof AttachmentExpression)
                result.add(field);
        }
        return result;
    }

    public Field getAttachmentField() {
        Collection<Field> attachmentFields = getAttachments();
        return attachmentFields.isEmpty() ? null : attachmentFields.iterator().next();
    }

    public void registerDataField(Field.CLASS<?> field) {
        dataFields.add(field);
    }

    public void unregisterDataField(Field.CLASS<?> field) {
        dataFields.remove(field);
    }

    public void registerFormField(Field.CLASS<?> field) {
        formFields.add(field);
    }

    public void unregisterFormField(Field.CLASS<?> field) {
        formFields.remove(field);
    }

    public boolean isShared() {
        return false;
    }

    final public SqlToken having() {
        if (having == null) {
            having = z8_having();
        }
        return having;
    }

    final public SqlToken where() {
        if (where == null) {
            where = z8_where();

            if (!filterBy.isEmpty()) {
                GuidField primaryKey = (GuidField) primaryKey();
                SqlToken inVector = new InVector(primaryKey.sql_guid(), filterBy);

                SqlToken whereToken = where instanceof True ? null : where;

                if (whereToken != null && !(whereToken instanceof Group))
                    whereToken = new Group(whereToken);

                where = whereToken == null ? new sql_bool(inVector) : new sql_bool(new And(whereToken, inVector));
            }
        }
        return where;
    }
    
    final public void addWhere(String json) {
        addWhere(parseWhere(Filter.parse(json, this)));
    }
    
    final public void addWhere(Collection<string> json) {
        addWhere(parseWhere(Filter.parse(json, this)));
    }
    
    final public void addWhere(SqlToken where) {
        this.where = new And(where(), where);
    }
    
    final public void setWhere(String json) {
        setWhere(parseWhere(Filter.parse(json, this)));
    }
    
    final public void setWhere(Collection<string> json) {
        setWhere(parseWhere(Filter.parse(json, this)));
    }

    final public void setWhere(SqlToken where) {
        this.where = where;
    }
    
    private SqlToken parseWhere(Collection<Filter> filters) {
        SqlToken result = null;
        for (Filter filter : filters) {
            SqlToken where = filter.where();
            result = result == null ? where : new And(result, where);
        }
        return result;
    }

    public Collection<Field> getSortFields() {
        return CLASS.asList(sortFields);
    }

    public Collection<Field> getGroupFields() {
        return CLASS.asList(groupFields);
    }

    public Collection<Link> getAggregateByFields() {
        return CLASS.asList(aggregateBy);
    }

    public Collection<Field> getGroupByFields() {
        return CLASS.asList(groupBy);
    }

    private RCollection<guid> getGuidCollection(Collection<guid> guids) {
        RCollection<guid> result = new RCollection<guid>();

        for (guid id : guids)
            result.add(id);

        return result;
    }

    private Field findPrimaryKey() {
        for (Field.CLASS<? extends Field> field : dataFields()) {
            if (field.getAttribute(IObject.PrimaryKey) != null)
                return field.get();
        }

        return null;
    }

    public Field[] primaryKeys() {
        if (primaryKeys != null)
            return primaryKeys;

        Query[] rootQueries = getRootQueries();

        if (rootQueries.length == 1) {
            Query rootQuery = rootQueries[0];
            Field primaryKey = rootQuery.findPrimaryKey();
            primaryKeys = primaryKey != null ? new Field[] { primaryKey } : new Field[0];
        } else {
            Collection<Field> result = new ArrayList<Field>();

            for (Query query : rootQueries)
                result.addAll(Arrays.asList(query.primaryKeys()));

            primaryKeys = result.toArray(new Field[0]);
        }

        return primaryKeys;
    }

    public Field primaryKey() {
        Field[] primaryKeys = primaryKeys();
        return primaryKeys.length == 1 ? primaryKeys[0] : null;
    }

    public Field parentKey() {
        Query rootQuery = getRootQuery();

        if (rootQuery != null && rootQuery instanceof TreeTable) {
            TreeTable table = (TreeTable) rootQuery;
            return table.parentId.get();
        }

        return null;
    }

    public Field[] parentKeys() {
        Query rootQuery = getRootQuery();

        if (rootQuery != null && rootQuery instanceof TreeTable) {
            TreeTable table = (TreeTable) rootQuery;
            return new Field[] { table.parent1.get(), table.parent2.get(), table.parent3.get(), table.parent4.get(),
                    table.parent5.get(), table.parent6.get() };
        }

        return new Field[0];
    }

    public boolean hasPrimaryKey() {
        return primaryKey() != null;
    }

    public Field lockKey() {
        Query rootQuery = getRootQuery();

        if (rootQuery != null && rootQuery instanceof Table) {
            Table table = (Table) rootQuery;
            return table.locked.get();
        }

        return null;
    }

    public Field children() {
        Query rootQuery = getRootQuery();

        if (rootQuery != null && rootQuery instanceof TreeTable) {
            TreeTable table = (TreeTable) rootQuery;
            return table.children.get();
        }

        return null;
    }

    public String getAlias() {
        return "T" + Math.abs((id() + hashCode()).hashCode());
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void onInitialized() {
        super.onInitialized();

        for (Query.CLASS<? extends Query> query : queries())
            query.addReference((Query.CLASS) getCLASS());

        if (model != null)
            model.get().setContext(this);
    }

    // ////////////////////////////////////////////////////////////////////////////////////////////////
    // IQuery

    @Override
    public String toString() {
        return id();
    }

    public static Query getModel(Query query) {
        Query context = query.getContext();

        if (context != null)
            return context.getModel() != null ? context.getModel() : query;

        return query.getModel() != null ? query.getModel() : query;
    }

    public Query getModel() {
        return model != null ? model.get() : null;
    }

    public void setModel(Query model) {
        this.model = (Query.CLASS<?>) model.getCLASS();
    }

    public Query getContext() {
        return contextQuery;
    }

    public void setContext(Query contextQuery) {
        this.contextQuery = contextQuery;
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
        return (Collection) getCLASS().getReferences();
    }

    public Collection<Query> getOwners() {
        Collection<Query> result = new ArrayList<Query>();

        for (Query.CLASS<Query> owner : owners())
            result.add(owner.get());

        return result;
    }

    public Query getRootQuery() {
        Query[] rootQueries = getRootQueries();

        if (rootQueries.length != 1)
            return null;

        Query rootQuery = rootQueries[0];

        if (rootQuery == this)
            return rootQuery;

        return rootQuery.getRootQuery();
    }

    public Query[] getRootQueries() {
        initializeRootQueries();
        return rootQueries;
    }

    public void setRootQuery(Query rootQuery) {
        this.rootQueries = new Query[] { rootQuery };
    }

    public Collection<OBJECT.CLASS<? extends OBJECT>> getLinks() {
        if (links == null) {
            links = new ArrayList<OBJECT.CLASS<? extends OBJECT>>();

            for (Field.CLASS<? extends Field> field : dataFields()) {
                if (field instanceof Link.CLASS || field instanceof LinkExpression.CLASS)
                    links.add(field);
            }
        }

        return links;
    }

    @SuppressWarnings("unchecked")
    private void initializeRootQueries() {
        if (rootQueries != null)
            return;

        Query.CLASS<Query> me = (Query.CLASS<Query>) this.getCLASS();

        if (queries.size() == 0) {
            rootQueries = new Query[] { this };
            return;
        }

        List<Query.CLASS<? extends Query>> references = new ArrayList<Query.CLASS<? extends Query>>();
        references.addAll(queries());

        if (getLinks().size() != 0)
            references.add(me);

        List<Query.CLASS<? extends Query>> referers = new ArrayList<Query.CLASS<? extends Query>>();
        referers.addAll(references);

        for (Query.CLASS<? extends Query> query : referers) {
            if (!query.hasInstance())
                continue;

            for (OBJECT.CLASS<? extends OBJECT> cls : query.get().getLinks()) {
                if (!cls.hasInstance())
                    continue;

                ILink link = (ILink) cls.get();
                Query.CLASS<Query> linkedQuery = link.query();

                if (linkedQuery != me && linkedQuery != query)
                    references.remove(linkedQuery);
            }
        }

        if (references.size() != 1) {
            for (int index = references.size() - 1; index >= 0; index--) {
                Query query = references.get(index).get();

                if (query.getLinks().size() == 0) {
                    System.out.println("Unused query found. " + this + '.' + query);
                    references.remove(index);
                }
            }
        }

        if (references.size() > 1) {
            if (model == null) {
                String message = "Model inconsistency. Multiple root queries detected in " + classId();

                for (Query.CLASS<? extends Query> query : references)
                    message += ("\n\t" + query.get());

                Trace.logEvent(message);

            }
        } else if (references.size() == 0)
            Trace.logEvent("Model inconsistency. No root query detected. " + this);

        rootQueries = CLASS.asList(references).toArray(new Query[0]);
    }

    private static Pattern pattern = Pattern.compile("\\.");

    private String[] parseId(String id) {
        String myId = id();

        if (!id.startsWith(myId))
            return null;

        if (id.equals(myId))
            return new String[0];

        id = id.substring(myId.length());

        if (!myId.isEmpty() && id.charAt(0) != '.')
            return null;

        String[] ids = pattern.split(id);
        return Arrays.copyOfRange(ids, myId.isEmpty() ? 0 : 1, ids.length);
    }

    private Query getQueryById(String id) {
        for (Query.CLASS<? extends Query> query : queries()) {
            if (query.id().startsWith(id()) && query.getIndex().equals(id))
                return query.get();
        }
        return null;
    }

    private Query getMatchedQuery(String id) {
        for (Query.CLASS<? extends Query> query : queries()) {
            if (id.startsWith(query.id()))
                return query.get();
        }

        return null;
    }

    private class FindQueryResult {
        public Query query = null;
        public Collection<Query> route = new ArrayList<Query>();
    }

    private FindQueryResult findQueryById(String id, boolean ignoreLast) {
        FindQueryResult result = new FindQueryResult();

        result.query = this;

        String[] path = parseId(id);

        if (path == null) {
            result.query = getMatchedQuery(id);

            if (result.query == null)
                return null;

            result.route.add(result.query);
            path = result.query.parseId(id);
        }

        int count = path.length - (ignoreLast ? 1 : 0);

        if (path.length == 0 || count == 0)
            return result;

        for (int i = 0; i < count; i++) {
            result.query = result.query.getQueryById(path[i]);
            result.route.add(result.query);

            if (result.query == null)
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

        if (query == this)
            return path;

        Query owner = null;

        int ownersCount = query.getOwnersCount();

        if (ownersCount > 1) {
            for (Query o : query.getOwners()) {
                if (!getRouteByOwners(o).isEmpty() || getRootQuery() == o) {
                    owner = o;
                    break;
                }
            }
        } else if (ownersCount == 1)
            owner = query.getOwner();

        Query rootQuery = getRootQuery();

        while (owner != null && query != this && query != rootQuery) {
            Query root = owner.getRootQuery();

            while (query != root) {
                ILink link = owner.getLinkTo(query);

                if (link != null) {
                    path.add(0, link);
                    // assert (owner != query);
                    query = owner;
                } else
                    break;
            }

            query = owner;

            if (query.getOwnersCount() > 1) {
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
        for (OBJECT.CLASS<?> cls : getLinks()) {
            ILink link = (ILink) cls.get();
            if (link.getQuery() == query)
                return link;
        }

        return null;
    }

    public Collection<ILink> getPath(Query query) {
        Collection<Query> route = getRoute(query);

        if (route == null)
            return getRouteByOwners(query);

        Collection<ILink> path = new ArrayList<ILink>();

        Query current = this;

        for (Query q : route) {
            if (current.getRootQuery() != q) {
                ILink link = current.getLinkTo(q);

                if (link != null)
                    path.add(link);
            }
            current = q;
        }
        return path;
    }

    public Collection<ILink> getPath(Field field) {
        Collection<Query> route = getRoute(field);

        if (route == null)
            return getRouteByOwners(field.owner());

        Collection<ILink> path = new ArrayList<ILink>();

        Query current = this;

        for (Query query : route) {
            if (query != current.getRootQuery()) {
                ILink link = current.getLinkTo(query);

                if (link != null)
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
        for (Field.CLASS<? extends Field> field : dataFields()) {
            if (id.equals(field.id()))
                return field.get();
        }
        return null;
    }

    public Field getFieldByName(String name) {
        for (Field.CLASS<? extends Field> field : dataFields()) {
            if (name.equals(field.name()))
                return field.get();
        }
        return null;
    }

    public Collection<Field.CLASS<? extends Field>> primaryFields() {
        Collection<Field.CLASS<? extends Field>> fields = new ArrayList<Field.CLASS<? extends Field>>();

        for (Field.CLASS<? extends Field> field : dataFields())
            fields.add(field);

        return fields;
    }

    public Collection<Field> getPrimaryFields() {
        Collection<Field> dataFields = getDataFields();
        Collection<Field> fields = new ArrayList<Field>(dataFields.size());
        for (Field field : dataFields) {
            if (!(field instanceof Expression))
                fields.add(field);
        }
        return fields;
    }

    private boolean isReachableVia(Query query, Field field) {
        if (field instanceof Expression) {
            Expression expression = (Expression) field;
            SqlToken token = expression.expression();

            Collection<IValue> values = token.getUsedFields();

            for (IValue value : values) {
                if (query.findFieldById(value.id()) == null)
                    return false;
            }
        }

        return query.findFieldById(field.id()) != null;
    }

    public Collection<Field> getFieldsVia(Query query) {
        if (this == query)
            return getFormFields();

        Collection<Field> result = new ArrayList<Field>();

        for (Field field : getFormFields()) {
            if (isReachableVia(query, field))
                result.add(field);
        }

        return result;
    }

    public Collection<Field> getReachableFields(Collection<Field> fields) {
        Collection<Field> result = new ArrayList<Field>();

        for (Field field : fields) {
            if (findFieldById(field.id()) != null)
                result.add(field);
        }

        return result;
    }

    public Period getPeriod() {
        if (period != null)
            return period.get();

        Query rootQuery = getRootQuery();

        if (rootQuery != null && rootQuery.period != null)
            return rootQuery.period.get();

        return null;
    }

    public void setPeriod(Period.CLASS<? extends Period> period) {
        Query rootQuery = getRootQuery();

        if (rootQuery.period != null)
            rootQuery.period = period;
        else
            this.period = period;
    }

    public Collection<Command> commands() {
        return CLASS.asList(commands);
    }

    private boolean isModel() {
        return contextQuery != null ? contextQuery.getModel() == this : getModel() == null;
    }

    public Collection<Command> getCommands() {
        Collection<Command> result = new ArrayList<Command>();

        if (isModel()) {
            result.addAll(commands());

            if (contextQuery != null)
                result.addAll(contextQuery.commands());
        }

        return result;
    }

    public Command getCommand(String id) {
        for (Command command : getCommands()) {
            if (command.id().equals(id))
                return command;
        }

        return null;
    }

    // ////////////////////////////////////////////////////////////////////////////////////////////////

    public boolean showAsTree() {
        Query rootQuery = getRootQuery();
        return !rootQuery.showAsGrid.get();
    }

    private boolean readOnly() {
        if (contextQuery != null && contextQuery.readOnly.get())
            return true;

        if (readOnly.get())
            return true;

        Query rootQuery = getRootQuery();

        if (rootQuery != null && rootQuery != this)
            return rootQuery.readOnly.get();

        return false;
    }

    public Collection<Field> collectSortFields() {
        Collection<Field> fields = new ArrayList<Field>();

        if (contextQuery != null) {
            fields = contextQuery.getSortFields();
            fields = getReachableFields(fields);
        }

        if (fields.isEmpty())
            fields = getSortFields();

        Query rootQuery = getRootQuery();

        if (fields.isEmpty() && rootQuery != null && rootQuery != this)
            fields = rootQuery.getSortFields();

        return fields;
    }

    public Collection<Field> collectGroupFields() {
        Collection<Field> fields = new ArrayList<Field>();

        if (contextQuery != null) {
            fields = contextQuery.getGroupFields();
            fields = getReachableFields(fields);
        }

        if (fields.isEmpty())
            fields = getGroupFields();

        Query rootQuery = getRootQuery();

        if (fields.isEmpty() && rootQuery != null && rootQuery != this)
            fields = rootQuery.getGroupFields();

        return fields;
    }

    public Collection<Link> collectAggregateByFields() {
        Collection<Link> fields = new ArrayList<Link>();

        Query context = getContext();

        if (context != null)
            fields = context.getAggregateByFields();

        if (fields.isEmpty())
            fields = getAggregateByFields();

        Query rootQuery = getRootQuery();

        if (fields.isEmpty() && rootQuery != null && rootQuery != this)
            fields = rootQuery.getAggregateByFields();

        return fields;
    }

    public Collection<Field> collectGroupByFields() {
        Collection<Field> fields = new ArrayList<Field>();

        Query context = getContext();

        if (context != null)
            fields = context.getGroupByFields();

        if (fields.isEmpty())
            fields = getGroupByFields();

        Query rootQuery = getRootQuery();

        if (fields.isEmpty() && rootQuery != null && rootQuery != this)
            fields = rootQuery.getGroupByFields();

        return fields;
    }

    public String getRecordFullText() {
        List<Field> searchFields = getSearchFields();

        String result = "";

        for (Field field : searchFields) {
            if (field.type() != FieldType.Guid)
                result += (result.isEmpty() ? "" : " ") + field.searchValue();
        }

        return result;
    }

    public Field getSearchId() {
        return searchId != null ? searchId.get() : primaryKey();
    }

    private static Object mutex = new Object();
    private static Map<String, Collection<ReportInfo>> reports = new HashMap<String, Collection<ReportInfo>>();

    private Collection<ReportInfo> getReports() {
        if (isModel()) {
            String id = (contextQuery != null ? contextQuery : this).classId();

            synchronized (mutex) {
                Collection<ReportInfo> result = reports.get(id);

                if (result == null) {
                    result = new ReportBindingFileReader().getReportTemplateFileNames(id);
                    reports.put(id, result);
                }

                return result;
            }
        }

        return new ArrayList<ReportInfo>();
    }

    public Collection<Field.CLASS<? extends Field>> chartSeries() {
        return chartSeries;
    }

    private Collection<Field> getChartSeries() {
        return CLASS.asList(chartSeries());
    }

    public void writeMeta(JsonWriter writer, Collection<Field> fields) {
        Query rootQuery = getRootQuery();

        String name = rootQuery != null ? rootQuery.name() : null;

        writer.writeProperty(Json.id, id());
        writer.writeProperty(Json.name, name);
        writer.writeProperty(Json.icon, icon());

        writeRecordActions(writer);

        writeKeys(writer, fields);

        boolean hasGroupBy = writeGroupByFields(writer, fields);

        writeFields(writer, fields);
        writeOwners(writer);
        writeReports(writer);
        writeCharts(writer);
        writeCommands(writer);
        writePeriod(writer);

        // visuals
        writer.writeProperty(Json.text, displayName());

        writer.writeProperty(Json.readOnly, hasGroupBy || rootQuery == null ? true : readOnly());

        writer.writeProperty(Json.showTotals, showTotals);
        writer.writeProperty(Json.columns, columns);
        writer.writeProperty(Json.viewMode, viewMode.toString());
        writer.writeProperty(Json.width, width);
        writer.writeProperty(Json.height, height);
    }

    private void writeRecordActions(JsonWriter writer) {
        writer.startArray(Json.actions);

        for (RecordActions action : recordActions)
            writer.write(action.toString());

        writer.finishArray();
    }

    private void writeKeys(JsonWriter writer, Collection<Field> fields) {
        Field primaryKey = primaryKey();

        if (primaryKey != null && fields.contains(primaryKey))
            writer.writeProperty(Json.primaryKey, primaryKey.id());

        Field lockKey = lockKey();

        if (lockKey != null && fields.contains(lockKey))
            writer.writeProperty(Json.lockKey, lockKey.id());

        Field attachments = getAttachmentField();

        if (attachments != null && fields.contains(attachments))
            writer.writeProperty(Json.attachments, attachments.id());

        Field parentKey = parentKey();

        if (parentKey != null && fields.contains(parentKey) && showAsTree()) {
            writer.writeProperty(Json.parentKey, parentKey().id());
            writer.writeProperty(Json.parentId, guid.NULL.toString());

            writer.writeProperty(Json.children, children().id());
            writer.writeProperty(Json.parentsSelectable, parentsSelectable);
        }

        if (!recordIds.isEmpty()) {
            writer.startArray(Json.ids);
            for (guid id : recordIds)
                writer.write(id);
            writer.finishArray();
        }
    }

    private boolean writeGroupByFields(JsonWriter writer, Collection<Field> fields) {
        Collection<? extends Field> groupByFields = collectGroupByFields();

        if (groupByFields.isEmpty())
            groupByFields = collectAggregateByFields();

        if (groupByFields.isEmpty())
            return false;

        writer.startArray(Json.groups);

        for (Field field : groupByFields)
            writer.write(field.id());

        writer.finishArray();

        return true;
    }

    private boolean hasRequiredLinks(Collection<ILink> links) {
        for (ILink link : links) {
            Field field = (Field) link;
            if (field.required.get())
                return true;
        }

        return false;
    }

    private boolean hasReadOnlyLinks(Collection<ILink> links) {
        for (ILink link : links) {
            Field field = (Field) link;
            if (field.readOnly.get())
                return true;
        }

        return false;
    }

    private void writeFields(JsonWriter writer, Collection<Field> fields) {
        writer.startArray(Json.fields);

        for (Field field : fields) {
            writer.startObject();
            field.writeMeta(writer);

            Collection<ILink> path = getPath(field);
            Query owner = field.owner();

            writer.writeProperty(Json.depth, path.size());

            boolean readOnly = false;
            boolean required = false;

            if (!path.isEmpty()) {
                String linkId = path.toArray(new ILink[0])[path.size() - 1].id();
                writer.writeProperty(Json.linked, true);
                writer.writeProperty(Json.linkId, linkId);
                writer.writeProperty(Json.linkedVia, owner.primaryKey().id());
                writer.writeProperty(Json.groupId, owner.id());

                if (field.editWith == null) {
                    writer.writeProperty(Json.editWith, owner.classId());
                    writer.writeProperty(Json.editWithText, owner.displayName());
                }

                readOnly = hasReadOnlyLinks(path) || !field.selectable.get();
                required = !readOnly && (hasRequiredLinks(path) || field.required.get());
            } else {
                readOnly = field.readOnly.get();
                required = !readOnly && field.required.get();
            }

            writer.writeProperty(Json.required, required);
            writer.writeProperty(Json.readOnly, readOnly);

            if (field.editWith != null) {
                writer.writeProperty(Json.editWith, field.editWith.classId());
                writer.writeProperty(Json.editWithText, field.editWith.displayName());
            }

            writer.finishObject();
        }

        writer.finishArray();
    }

    private void writeOwners(JsonWriter writer) {
        writer.startArray(Json.backwards);
        for (Query owner : getOwners())
            writeOwnerMeta(writer, owner);
        writer.finishArray();
    }

    private void writeReports(JsonWriter writer) {
        Collection<ReportInfo> reports = getReports();

        writer.startArray(Json.reports);
        for (ReportInfo report : reports) {
            writer.startObject();
            writer.writeProperty(Json.id, report.fileName());
            writer.writeProperty(Json.text, report.displayName());
            writer.finishObject();
        }
        writer.finishArray();
    }

    private void writeCharts(JsonWriter writer) {
        writer.writeProperty(Json.chartType, chartType.toString());

        Collection<Field> series = getChartSeries();

        writer.startArray(Json.chartSeries);

        for (Field field : series)
            writer.write(field.id());

        writer.finishArray();
    }

    private void writeCommands(JsonWriter writer) {
        writer.startArray(Json.commands);

        for (ICommand command : getCommands()) {
            writer.startObject();
            command.write(writer);
            writer.finishObject();
        }

        writer.finishArray();
    }

    private void writePeriod(JsonWriter writer) {
        if (period != null) {
            writer.startObject(Json.period);
            writer.writeProperty(Json.period, period.get().type.toString());
            writer.writeProperty(Json.start, period.get().start);
            writer.writeProperty(Json.finish, period.get().finish);
            writer.finishObject();
        }
    }

    private void writeOwnerMeta(JsonWriter writer, Query owner) {
        while (owner != null) {
            Query ownerRoot = owner.getRootQuery();

            if (ownerRoot != null && owner.visible.get()) {
                writer.startObject();
                writer.writeProperty(Json.text, owner.getRootQuery().displayName());
                writer.writeProperty(Json.queryId, owner.id());
                writer.writeProperty(Json.icon, owner.getRootQuery().icon());
                writer.finishObject();
            }

            int ownersCount = owner.getOwnersCount();

            if (ownersCount == 0)
                return;

            if (ownersCount == 1 && owner.getOwner().getRootQuery() == owner)
                owner = owner.getOwner();

            if (ownersCount > 1) {
                for (Query o : owner.getOwners())
                    writeOwnerMeta(writer, o);
                return;
            }

            owner = owner.getOwner();
        }
    }

    // ////////////////////////////////////////////////////////////////////////////////////////////////

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
    
    public void z8_addWhere(RCollection<string> json) {
        addWhere(json);
    }

    public void z8_setWhere(sql_bool where) {
        setWhere(where);
    }

    public void z8_setWhere(string json) {
        setWhere(json.get());
    }
    
    public void z8_setWhere(RCollection<string> json) {
        setWhere(json);
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

    public bool z8_aggregate() {
        return z8_aggregate(null, null);
    }

    public bool z8_aggregate(RCollection<Field.CLASS<Field>> fieldClasses) {
        return z8_aggregate(fieldClasses, null);
    }

    public bool z8_aggregate(sql_bool where) {
        return z8_aggregate(null, where);
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
        return (Field.CLASS) getFieldById(id.get()).getCLASS();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Field.CLASS<? extends Field> z8_getFieldByName(string name) {
        return (Field.CLASS) getFieldByName(name.get()).getCLASS();
    }

    public guid z8_create() {
        try {
            return create();
        } catch (Throwable e) {
            throw new exception(e);
        }
    }

    public guid z8_create(guid recordId) {
        try {
            return create(recordId);
        } catch (Throwable e) {
            throw new exception(e);
        }
    }

    public guid z8_copy(guid recordId) {
        return copy(recordId);
    }

    public void z8_read() {
        read();
    }

    public bool z8_readRecord(guid id) {
        return new bool(readRecord(id));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public bool z8_readRecord(guid id, RCollection fieldClasses) {
        return new bool(readRecord(id, (RCollection<Field.CLASS<Field>>) fieldClasses));
    }

    public bool z8_readFirst() {
        return new bool(readFirst());
    }

    public bool z8_readFirst(sql_bool where) {
        return new bool(readFirst(where));
    }

    public void z8_read(sql_bool where) {
        read(where);
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
    public void z8_group(RCollection groupClasses, RCollection fieldClasses, RCollection sortClasses, sql_bool where,
            sql_bool having) {
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

    public void z8_onNew(Query.CLASS<? extends Query> query, guid recordId, guid parentId, guid modelRecordId) {
    }

    public void z8_onCopy(Query.CLASS<? extends Query> query) {
    }

    public void z8_beforeRead(Query.CLASS<? extends Query> query, guid parentId, Query.CLASS<? extends Query> model,
            guid modelRecordId) {
    }

    public void z8_afterRead(Query.CLASS<? extends Query> query, guid parentId, Query.CLASS<? extends Query> model,
            guid modelRecordId) {
    }

    public void z8_beforeCreate(Query.CLASS<? extends Query> query, guid recordId, guid parentId,
            Query.CLASS<? extends Query> model, guid modelRecordId) {
    }

    public void z8_afterCreate(Query.CLASS<? extends Query> query, guid recordId, guid parentId,
            Query.CLASS<? extends Query> model, guid modelRecordId) {
    }

    @SuppressWarnings("rawtypes")
    public void z8_beforeUpdate(Query.CLASS<? extends Query> query, guid recordId, RCollection changedFields,
            Query.CLASS<? extends Query> model, guid modelRecordId) {
    }

    @SuppressWarnings("rawtypes")
    public void z8_afterUpdate(Query.CLASS<? extends Query> query, guid recordId, RCollection changedFields,
            Query.CLASS<? extends Query> model, guid modelRecordId) {
    }

    public void z8_beforeDestroy(Query.CLASS<? extends Query> query, guid recordId, Query.CLASS<? extends Query> model,
            guid modelRecordId) {
    }

    public void z8_afterDestroy(Query.CLASS<? extends Query> query, guid recordId, Query.CLASS<? extends Query> model,
            guid modelRecordId) {
    }

    @SuppressWarnings("rawtypes")
    public void z8_onCommand(Query.CLASS<? extends Query> query, Command.CLASS<? extends Command> command,
            RCollection recordIds) {
    }

    public void z8_onRender(Query.CLASS<? extends Query> query) {
    }

    public Style.CLASS<? extends Style> z8_renderRecord(Query.CLASS<? extends Query> query) {
        return null;
    }

    @SuppressWarnings("rawtypes")
    public void z8_onReport(Query.CLASS<? extends Query> query, string report, RCollection recordIds) {
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Query.CLASS<? extends Query> z8_onFollow(Field.CLASS<? extends Field> fieldClass, RCollection recordIds) {
        Field field = fieldClass.get();

        if (field.editWith != null) {
            return (Query.CLASS<? extends Query>) Loader.loadClass(field.editWith.classId());
        }

        if (field.anchorPolicy == FollowPolicy.Custom) {
            return null;
        }

        Class<?> cls = field.owner().getClass();

        String classId = cls.getCanonicalName();

        while (classId.indexOf(".__") != -1) {
            cls = cls.getSuperclass();
            classId = cls.getCanonicalName();
        }

        return (Query.CLASS<? extends Query>) Loader.loadClass(classId);
    }

    public void refresh() {
        IMonitor monitor = ApplicationServer.getMonitor();

        for (Query rootQuery : getRootQueries())
            monitor.refresh(rootQuery.name());
    }

    public void z8_refresh() {
        refresh();
    }

    public void refreshRecord(guid id) {
        IMonitor monitor = ApplicationServer.getMonitor();

        for (Query rootQuery : getRootQueries())
            monitor.refresh(rootQuery.name(), id);
    }

    public void z8_refreshRecord(guid id) {
        refreshRecord(id);
    }

    public string z8_getRecordFullText() {
        return new string(getRecordFullText());
    }
}
