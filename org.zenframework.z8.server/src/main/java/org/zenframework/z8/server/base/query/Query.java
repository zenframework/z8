package org.zenframework.z8.server.base.query;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

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
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.expressions.And;
import org.zenframework.z8.server.db.sql.expressions.Group;
import org.zenframework.z8.server.db.sql.expressions.True;
import org.zenframework.z8.server.db.sql.functions.InVector;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.engine.Database;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.reports.BirtFileReader;
import org.zenframework.z8.server.reports.ReportBindingFileReader;
import org.zenframework.z8.server.reports.ReportConstants;
import org.zenframework.z8.server.reports.ReportInfo;
import org.zenframework.z8.server.request.IMonitor;
import org.zenframework.z8.server.request.Loader;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.search.SearchEngine;
import org.zenframework.z8.server.security.IAccess;
import org.zenframework.z8.server.security.IForm;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.decimal;
import org.zenframework.z8.server.types.exception;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.types.sql.sql_bool;;

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

    public bool showAsGrid = new bool(true); // to show treeTable as an ordinary table
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

    public RCollection<Command.CLASS<? extends Command>> commands = new RCollection<Command.CLASS<? extends Command>>(true);

    public RCollection<guid> recordIds = new RCollection<guid>();

    public RCollection<guid> filterBy = new RCollection<guid>(true);

    public RCollection<Link.CLASS<? extends Link>> aggregateBy = new RCollection<Link.CLASS<? extends Link>>(true);
    public RCollection<Field.CLASS<? extends Field>> groupBy = new RCollection<Field.CLASS<? extends Field>>(true);

    public RCollection<Filter.CLASS<? extends Filter>> filters = new RCollection<Filter.CLASS<? extends Filter>>(true);

    public Period.CLASS<? extends Period> period = null;

    public RCollection<RecordActions> recordActions = new RCollection<RecordActions>();

    private Query contextQuery;
    private Query[] rootQueries;

    private Collection<OBJECT.CLASS<? extends OBJECT>> links = null;

    private Field[] primaryKeys = null;

    private SqlToken where = null;
    private SqlToken having = null;

    private Database database;
    private Database cachedDatabase;

    protected Select cursor;

    private List<Select> state = new ArrayList<Select>();
    private List<Map<Field, primary>> fieldState = new ArrayList<Map<Field, primary>>();

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

    public void onNew(guid recordId, guid parentId) {
        if (contextQuery != null) {
            contextQuery.onNew(this, recordId, parentId);
        }

        if (contextQuery != this) {
            onNew(this, recordId, parentId);
        }

        Query rootQuery = getRootQuery();

        if (rootQuery != this) {
            rootQuery.onNew(rootQuery, recordId, parentId);
        }
    }

    protected void onNew(Query data, guid recordId, guid parentId) {
        if (ApplicationServer.events())
            z8_onNew(data.myClass(), recordId, parentId);
    }

    public void onCopy() {
        if (contextQuery != null) {
            contextQuery.onCopy(this);
        }

        if (contextQuery != this) {
            onCopy(this);
        }

        Query rootQuery = getRootQuery();

        if (rootQuery != this) {
            rootQuery.onCopy(rootQuery);
        }
    }

    protected void onCopy(Query data) {
        if (ApplicationServer.events())
            z8_onCopy(data.myClass());
    }

    public void beforeRead(guid parentId, Query model, guid modelRecordId) {
        if (contextQuery != null) {
            contextQuery.beforeRead(this, parentId, model, modelRecordId);
        }

        if (contextQuery != this) {
            beforeRead(this, parentId, model, modelRecordId);
        }

        Query rootQuery = getRootQuery();

        if (rootQuery != this) {
            rootQuery.beforeRead(rootQuery, parentId, model, modelRecordId);
        }
    }

    protected void beforeRead(Query data, guid parentId, Query model, guid modelRecordId) {
        if (ApplicationServer.events())
            z8_beforeRead(data.myClass(), parentId, model.myClass(), modelRecordId);
    }

    public void afterRead(guid parentId, Query model, guid modelRecordId) {
        if (contextQuery != null) {
            contextQuery.afterRead(this, parentId, model, modelRecordId);
        }

        if (contextQuery != this) {
            afterRead(this, parentId, model, modelRecordId);
        }

        Query rootQuery = getRootQuery();

        if (rootQuery != this) {
            rootQuery.afterRead(rootQuery, parentId, model, modelRecordId);
        }
    }

    protected void afterRead(Query data, guid parentId, Query model, guid modelRecordId) {
        if (ApplicationServer.events())
            z8_afterRead(data.myClass(), parentId, model.myClass(), modelRecordId);
    }

    public void beforeCreate(guid recordId, guid parentId, Query model, guid modelRecordId) {
        if (contextQuery != null) {
            contextQuery.beforeCreate(this, recordId, parentId, model, modelRecordId);
        }

        if (contextQuery != this) {
            beforeCreate(this, recordId, parentId, model, modelRecordId);
        }

        Query rootQuery = getRootQuery();

        if (rootQuery != this) {
            rootQuery.beforeCreate(rootQuery, recordId, parentId, model, modelRecordId);
        }
    }

    protected void beforeCreate(Query data, guid recordId, guid parentId, Query model, guid modelRecordId) {
        if (ApplicationServer.events())
            z8_beforeCreate(data.myClass(), recordId, parentId, model.myClass(), modelRecordId);
    }

    public void afterCreate(guid recordId, guid parentId, Query model, guid modelRecordId) {
        if (contextQuery != null) {
            contextQuery.afterCreate(this, recordId, parentId, model, modelRecordId);
        }

        if (contextQuery != this) {
            afterCreate(this, recordId, parentId, model, modelRecordId);
        }

        Query rootQuery = getRootQuery();

        if (rootQuery != this) {
            rootQuery.afterCreate(rootQuery, recordId, parentId, model, modelRecordId);
        }
    }

    protected void afterCreate(Query data, guid recordId, guid parentId, Query model, guid modelRecordId) {
        if (ApplicationServer.events()) {
            z8_afterCreate(data.myClass(), recordId, parentId, model.myClass(), modelRecordId);
        }
        if (hasAttribute(IObject.SearchIndex)) {
            SearchEngine.INSTANCE.updateRecord(this, recordId.toString());
        }
    }

    public void beforeUpdate(guid recordId, Collection<Field> fields, Query model, guid modelId) {
        if (contextQuery != null) {
            contextQuery.beforeUpdate(this, recordId, fields, model, modelId);
        }

        if (contextQuery != this) {
            beforeUpdate(this, recordId, fields, model, modelId);
        }

        Query rootQuery = getRootQuery();

        if (rootQuery != this) {
            rootQuery.beforeUpdate(rootQuery, recordId, fields, model, modelId);
        }
    }

    @SuppressWarnings("unchecked")
    protected void beforeUpdate(Query data, guid recordId, Collection<Field> fields, Query model, guid modelRecordId) {
        RCollection<Field.CLASS<? extends Field>> changedFields = new RCollection<Field.CLASS<? extends Field>>();

        for (Field field : fields) {
            changedFields.add((Field.CLASS<? extends Field>) field.getCLASS());
        }

        if (ApplicationServer.events())
            z8_beforeUpdate(data.myClass(), recordId, changedFields, model.myClass(), modelRecordId);
    }

    public void afterUpdate(guid recordId, Collection<Field> fields, Query model, guid modelId) {
        if (contextQuery != null) {
            contextQuery.afterUpdate(this, recordId, fields, model, modelId);
        }

        if (contextQuery != this) {
            afterUpdate(this, recordId, fields, model, modelId);
        }

        Query rootQuery = getRootQuery();

        if (rootQuery != this) {
            rootQuery.afterUpdate(rootQuery, recordId, fields, model, modelId);
        }
    }

    @SuppressWarnings("unchecked")
    protected void afterUpdate(Query data, guid recordId, Collection<Field> fields, Query model, guid modelRecordId) {
        RCollection<Field.CLASS<? extends Field>> changedFields = new RCollection<Field.CLASS<? extends Field>>();

        for (Field field : fields) {
            changedFields.add((Field.CLASS<? extends Field>) field.getCLASS());
        }

        if (hasAttribute(IObject.SearchIndex)) {
            SearchEngine.INSTANCE.updateRecord(this, recordId.toString());
        }

        if (ApplicationServer.events())
            z8_afterUpdate(data.myClass(), recordId, changedFields, model.myClass(), modelRecordId);
    }

    public void beforeDestroy(guid recordId, Query model, guid modelRecordId) {
        if (contextQuery != null) {
            contextQuery.beforeDestroy(this, recordId, model, modelRecordId);
        }

        if (contextQuery != this) {
            beforeDestroy(this, recordId, model, modelRecordId);
        }

        Query rootQuery = getRootQuery();

        if (rootQuery != this) {
            rootQuery.beforeDestroy(rootQuery, recordId, model, modelRecordId);
        }
    }

    protected void beforeDestroy(Query data, guid recordId, Query model, guid modelRecordId) {
        if (ApplicationServer.events())
            z8_beforeDestroy(data.myClass(), recordId, model.myClass(), modelRecordId);
    }

    public void afterDestroy(guid recordId, Query model, guid modelRecordId) {
        if (contextQuery != null) {
            contextQuery.afterDestroy(this, recordId, model, modelRecordId);
        }

        if (contextQuery != this) {
            afterDestroy(this, recordId, model, modelRecordId);
        }

        Query rootQuery = getRootQuery();

        if (rootQuery != this) {
            rootQuery.afterDestroy(rootQuery, recordId, model, modelRecordId);
        }
    }

    protected void afterDestroy(Query data, guid recordId, Query model, guid modelRecordId) {
        if (ApplicationServer.events()) {
            z8_afterDestroy(data.myClass(), recordId, model.myClass(), modelRecordId);
        }
        if (hasAttribute(IObject.SearchIndex)) {
            SearchEngine.INSTANCE.deleteRecord(this, recordId.toString());
        }
    }

    public void onRender() {
        if (contextQuery != null) {
            contextQuery.onRender(this);
        }

        if (contextQuery != this) {
            onRender(this);
        }

        Query rootQuery = getRootQuery();

        if (rootQuery != this) {
            rootQuery.onRender(this);
        }
    }

    protected void onRender(Query data) {
        z8_onRender(data.myClass());
    }

    public Style renderRecord() {
        Style style = null;

        if (contextQuery != null) {
            style = contextQuery.renderRecord(this);
        }

        if (style != null) {
            return style;
        }

        if (contextQuery != this) {
            style = renderRecord(this);
        }

        if (style != null) {
            return style;
        }

        Query rootQuery = getRootQuery();

        if (rootQuery != null && rootQuery != this) {
            style = rootQuery.renderRecord(rootQuery);
        }

        return style;
    }

    protected Style renderRecord(Query query) {
        Style.CLASS<? extends Style> style = z8_renderRecord(query.myClass());
        return style != null ? style.get() : null;
    }

    public void onCommand(Command command, Collection<guid> recordIds) {
        if (contextQuery != null) {
            contextQuery.onCommand(this, command, recordIds);
        }

        if (contextQuery != this) {
            onCommand(this, command, recordIds);
        }

        Query rootQuery = getRootQuery();

        if (rootQuery != this) {
            rootQuery.onCommand(rootQuery, command, recordIds);
        }
    }

    @SuppressWarnings("unchecked")
    protected void onCommand(Query data, Command command, Collection<guid> recordIds) {
        RCollection<guid> ids = getGuidCollection(recordIds);
        z8_onCommand(data.myClass(), (Command.CLASS<? extends Command>) command.getCLASS(), ids);
    }

    public Collection<Query> onReport(String report, Collection<guid> recordIds) {
        Collection<Query> queries = getReportQueries(report, recordIds);

        if (queries.isEmpty()) {
            queries.add(this);
        }

        for (Query query : queries) {
            onReport(query, report, recordIds);
        }

        return queries;
    }

    private Collection<Query> getReportQueries(String report, Collection<guid> recordIds) {
        File reportFile = new File(new File(ApplicationServer.workingPath(), ReportConstants.DEFAULT_REPORT_FOLDER), report);

        BirtFileReader birtXMLReader = new BirtFileReader(reportFile.getAbsolutePath());

        Collection<Query> result = new ArrayList<Query>();

        Collection<String> classNames = birtXMLReader.getDataSets();

        for (String className : classNames) {
            if (className.endsWith(".CLASS")) {
                int index = className.lastIndexOf(".CLASS");
                className = className.substring(0, index);
            }

            result.add((Query) Loader.getInstance(className));
        }

        return result;
    }

    private void onReport(Query query, String report, Collection<guid> recordIds) {
        Query contextQuery = query.getContext();

        if (contextQuery != null) {
            contextQuery.callOnReport(this, report, recordIds);
        }

        if (contextQuery != query) {
            query.callOnReport(this, report, recordIds);
        }

        Query rootQuery = query.getRootQuery();

        if (rootQuery != query) {
            rootQuery.callOnReport(rootQuery, report, recordIds);
        }
    }

    private void callOnReport(Query data, String report, Collection<guid> recordIds) {
        RCollection<guid> ids = getGuidCollection(recordIds);
        z8_onReport(data.myClass(), new string(report), ids);
    }

    public Query onFollow(Field field, Collection<guid> recordIds) {
        Query result = null;

        if (contextQuery != null) {
            result = contextQuery.callOnFollow(field, recordIds);
        }

        if (result == null) {
            result = callOnFollow(field, recordIds);
        }

        if (result == null) {
            Query rootQuery = getRootQuery();

            if (rootQuery != this) {
                result = rootQuery.callOnFollow(field, recordIds);
            }
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

    public boolean hasRecord(guid recordId) {
        return readRecord(recordId, Arrays.asList(primaryKey()));
    }

    public int count() {
        return count(null);
    }

    public int count(SqlToken where) {
        ReadAction action = new ReadAction(this);
        action.addFilter(where);
        return action.getCounter().count();
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

        if (cursor != null) {
            cursor.close();
        }

        cursor = action.getTotals();

        return cursor.next();
    }

    private guid getParentId() {
        Field parentKey = getRootQuery().parentKey();

        guid parentId = null;

        if (parentKey != null) {
            parentId = parentKey.changed() ? parentKey.guid() : guid.NULL;
        }

        return parentId;
    }

    private Collection<Field> getInsertFields(guid recordId, guid parentId) {
        Query query = getRootQuery();

        Collection<Field> myFields = new ArrayList<Field>();
        Collection<Field> fields = query.getDataFields();

        for (Field field : fields) {
            if (field.getOwner() == query) {
                if (!field.changed()) {
                    field.set(field.getDefault());
                }

                if (field.isPrimaryKey() && !field.changed()) {
                    field.set(recordId);
                }

                if (field.isParentKey() && parentId != null && !field.changed()) {
                    field.set(parentId);
                }

                myFields.add(field);
            }
        }

        return myFields;
    }

    public void executeInsert(Collection<Field> fields) {
        Query rootQuery = getRootQuery();

        Insert insert = null;

        insert = new Insert(rootQuery, fields);

        try {
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

        for (Field field : fields) {
            field.reset();
        }

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
        NewAction.run(this, recordId, parentId);
        return insert(recordId, parentId, modelRecordId);
    }

    public guid copy(guid recordId) {
        guid parentId = getParentId();
        guid newRecordId = CopyAction.run(this, recordId, parentId);
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

        if (cursor != null) {
            cursor.close();
        }

        cursor = action.getCursor();
        return cursor.next();
    }

    public Database getDatabase() {
        return database;
    }

    public void setDatabase(Database database) {
        this.database = database;
    }

    private void setDatabase() {
        if (database != null) {
            cachedDatabase = ApplicationServer.setDatabase(database);
        }
    }

    private void restoreDatabase() {
        if (database != null) {
            ApplicationServer.setDatabase(cachedDatabase);
        }
    }

    protected boolean readFirst(Collection<Field> fields, Collection<Field> sortFields, Collection<Field> groupFields,
            SqlToken where, SqlToken having) {
        read(fields, sortFields, groupFields, where, having);
        return next();
    }

    protected void read(Collection<Field> fields, Collection<Field> sortFields, Collection<Field> groupFields,
            SqlToken where, SqlToken having) {
        try {
            setDatabase();

            ActionParameters parameters = new ActionParameters();
            parameters.query = this;
            parameters.fields = fields;
            parameters.sortFields = sortFields;
            parameters.groupBy = groupFields;

            ReadAction action = new ReadAction(parameters);
            action.addFilter(where);
            action.addGroupFilter(having);

            if (cursor != null) {
                cursor.close();
            }

            cursor = action.getCursor();
        } finally {
            restoreDatabase();
        }
    }

    public Collection<Field> getChangedFields() {
        Query query = getRootQuery();

        Collection<Field> fields = new ArrayList<Field>();

        for (Field.CLASS<? extends Field> field : query.primaryFields()) {
            if (field.hasInstance() && field.get().changed()) {
                fields.add(field.get());
            }
        }

        return fields;
    }

    public void update(guid id) {
        Collection<Field> fields = getChangedFields();
        UpdateAction.run(this, id, fields, getModelRecordId(id));
    }

    public void destroy(guid id) {
        DestroyAction.run(this, id, getModelRecordId(id));
    }

    public void destroy(SqlToken where) {
        Collection<Field> fields = new ArrayList<Field>();
        fields.add(primaryKey());

        read(fields, where);

        while (next()) {
            guid id = recordId();
            DestroyAction.run(this, id, getModelRecordId(id));
        }
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
        if (cursor == null) {
            throw new RuntimeException("Method Query.read() should be called before Query.next()");
        }

        return cursor.next();
    }

    public boolean isAfterLast() {
        if (cursor == null) {
            throw new RuntimeException("Method Query.read() should be called before Query.isAfterLast()");
        }

        return cursor.isAfterLast();
    }

    public void saveState() {
        Map<Field, primary> fields = new HashMap<Field, primary>();

        state.add(cursor);
        fieldState.add(fields);

        for (Field field : getDataFields()) {
            if (field.changed()) {
                fields.put(field, field.get());
                field.reset();
            }
        }

        if (cursor != null) {
            cursor.saveState();
        }

        cursor = null;
    }

    public void restoreState() {
        if (cursor != null) {
            cursor.close();
        }

        cursor = state.remove(state.size() - 1);

        if (cursor != null) {
            cursor.restoreState();
        }

        Map<Field, primary> fields = fieldState.remove(fieldState.size() - 1);

        for (Field field : fields.keySet()) {
            field.set(fields.get(field));
        }
    }

    public Collection<Field.CLASS<? extends Field>> dataFields() {
        return dataFields;
    }

    public Collection<Field.CLASS<? extends Field>> formFields() {
        if (formFields.isEmpty()) {
            return dataFields();
        }

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

    @SuppressWarnings("unchecked")
    public Collection<Control.CLASS<? extends Control>> controls() {
        if (formFields.isEmpty()) {
            return (Collection) dataFields();
        }

        return formFields;
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
        if (searchFields.isEmpty()) {
            return getDataFields();
        } else {
            return CLASS.asList(searchFields);
        }
    }

    public Collection<AttachmentField> getAttachments() {
        Collection<AttachmentField> result = new ArrayList<AttachmentField>();
        for (Field field : getDataFields()) {
            if (field instanceof AttachmentField)
                result.add((AttachmentField) field);
        }
        return result;
    }

    public void registerDataField(Field.CLASS<?> field) {
        assert (field.instanceOf(Field.class));
        dataFields.add(field);
    }

    public void registerFormField(Field.CLASS<?> field) {
        assert (field.instanceOf(Field.class));
        formFields.add(field);
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

                if (whereToken != null && !(whereToken instanceof Group)) {
                    whereToken = new Group(whereToken);
                }

                where = whereToken == null ? new sql_bool(inVector) : new sql_bool(new And(whereToken, inVector));
            }
        }
        return where;
    }

    final public void setWhere(SqlToken where) {
        this.where = where;
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

    public Collection<Filter> getFilters() {
        return CLASS.asList(filters);
    }

    private RCollection<guid> getGuidCollection(Collection<guid> guids) {
        RCollection<guid> result = new RCollection<guid>();

        for (guid id : guids) {
            result.add(id);
        }

        return result;
    }

    private Field findPrimaryKey() {
        for (Field.CLASS<? extends Field> field : dataFields()) {
            if (field.getAttribute(IObject.PrimaryKey) != null) {
                return field.get();
            }
        }

        return null;
    }

    public Field[] primaryKeys() {
        if (primaryKeys != null) {
            return primaryKeys;
        }

        Query[] rootQueries = getRootQueries();

        if (rootQueries.length == 1) {
            Query rootQuery = rootQueries[0];

            Field primaryKey = rootQuery.findPrimaryKey();

            //			assert (primaryKey != null);

            primaryKeys = primaryKey != null ? new Field[] { primaryKey } : new Field[0];
        } else {
            Collection<Field> result = new ArrayList<Field>();

            for (Query query : rootQueries) {
                result.addAll(Arrays.asList(query.primaryKeys()));
            }

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

    public AttachmentField attachmentField() {
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
        return "T" + hashCode();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onInitialized() {
        super.onInitialized();

        for (Query.CLASS<? extends Query> query : queries()) {
            query.addReference((Query.CLASS) getCLASS());
        }

        if (model != null) {
            model.get().setContext(this);
        }

    }

    //////////////////////////////////////////////////////////////////////////////////////////////////
    // IQuery

    @Override
    public String toString() {
        return id();
    }

    public static Query getModel(Query query) {
        Query context = query.getContext();

        if (context != null) {
            return context.getModel() != null ? context.getModel() : query;
        }

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

    public Query getOwner() {
        return getOwnersCount() == 1 ? owners().iterator().next().get() : null;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Collection<Query.CLASS<Query>> owners() {
        return (Collection) getCLASS().getReferences();
    }

    public Collection<Query> getOwners() {
        Collection<Query> result = new ArrayList<Query>();

        for (Query.CLASS<Query> owner : owners()) {
            result.add(owner.get());
        }
        return result;
    }

    public Query getRootQuery() {
        Query[] rootQueries = getRootQueries();
        
        if(rootQueries.length != 1)
            return null;
        
        Query rootQuery = rootQueries[0];
        
        if(rootQuery == this)
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
                if (field instanceof Link.CLASS || field instanceof LinkExpression.CLASS) {
                    links.add(field);
                }
            }
        }

        return links;
    }

    @SuppressWarnings("unchecked")
    private void initializeRootQueries() {
        if (rootQueries != null) {
            return;
        }

        Query.CLASS<Query> me = (Query.CLASS<Query>) this.getCLASS();

        if (queries.size() == 0) {
            rootQueries = new Query[] { this };
            return;
        }

        List<Query.CLASS<? extends Query>> references = new ArrayList<Query.CLASS<? extends Query>>();
        references.addAll(queries());

        if (getLinks().size() != 0) {
            references.add(me);
        }

        List<Query.CLASS<? extends Query>> referers = new ArrayList<Query.CLASS<? extends Query>>();
        referers.addAll(references);

        for (Query.CLASS<? extends Query> query : referers) {
            if (!query.hasInstance()) {
                continue;
            }

            for (OBJECT.CLASS<? extends OBJECT> cls : query.get().getLinks()) {
                if (!cls.hasInstance()) {
                    continue;
                }

                ILink link = (ILink) cls.get();
                Query.CLASS<Query> linkedQuery = link.query();

                if (linkedQuery != me && linkedQuery != query) {
                    references.remove(linkedQuery);
                }
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

        if (references.size() == 0) {
            System.out.println("Model inconsistency. No root query detected. " + this);
        } else if (references.size() > 1) {
            if (model == null) {
                System.out.println("Model inconsistency. Multiple root queries detected. ");

                for (Query.CLASS<? extends Query> query : references) {
                    System.out.println("\t" + query.get());
                }
            }
        }

        rootQueries = CLASS.asList(references).toArray(new Query[0]);
    }

    private static Pattern pattern = Pattern.compile("\\.");

    private String[] parseId(String id) {
        String myId = id();

        if (!id.startsWith(myId)) {
            return null;
        }

        if (id.equals(myId)) {
            return new String[0];
        }

        id = id.substring(myId.length());

        if (!myId.isEmpty() && id.charAt(0) != '.') {
            return null;
        }

        String[] ids = pattern.split(id);
        return Arrays.copyOfRange(ids, myId.isEmpty() ? 0 : 1, ids.length);
    }

    private Query getQueryById(String id) {
        for (Query.CLASS<? extends Query> query : queries()) {
            if (query.id().startsWith(id()) && query.getIndex().equals(id)) {
                return query.get();
            }
        }
        return null;
    }

    private Query getMatchedQuery(String id) {
        for (Query.CLASS<? extends Query> query : queries()) {
            if (id.startsWith(query.id())) {
                return query.get();
            }
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

            if (result.query == null) {
                return null;
            }

            result.route.add(result.query);
            path = result.query.parseId(id);
        }

        int count = path.length - (ignoreLast ? 1 : 0);

        if (path.length == 0 || count == 0) {
            return result;
        }

        for (int i = 0; i < count; i++) {
            result.query = result.query.getQueryById(path[i]);
            result.route.add(result.query);

            if (result.query == null) {
                return null;
            }
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

        if (query == this) {
            return path;
        }

        Query owner = null;

        int ownersCount = query.getOwnersCount();

        if (ownersCount > 1) {
            for (Query o : query.getOwners()) {
                if (!getRouteByOwners(o).isEmpty() || getRootQuery() == o) {
                    owner = o;
                    break;
                }
            }
        } else if (ownersCount == 1) {
            owner = query.getOwner();
        }

        Query rootQuery = getRootQuery();

        while (owner != null && query != this && query != rootQuery) {
            Query root = owner.getRootQuery();

            while (query != root) {
                ILink link = owner.getLinkTo(query);

                if (link != null) {
                    path.add(0, link);
                    assert (owner != query);
                    query = owner;
                } else {
                    break;
                }
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
            if (link.getQuery() == query) {
                return link;
            }
        }

        return null;
    }

    public Collection<ILink> getPath(Query query) {
        Collection<Query> route = getRoute(query);

        if (route == null) {
            return getRouteByOwners(query);
        }

        Collection<ILink> path = new ArrayList<ILink>();

        Query current = this;

        for (Query q : route) {
            if (current.getRootQuery() != q) {
                ILink link = current.getLinkTo(q);

                if (link != null) {
                    path.add(link);
                }
            }
            current = q;
        }
        return path;
    }

    public Collection<ILink> getPath(Field field) {
        Collection<Query> route = getRoute(field);

        if (route == null) {
            return getRouteByOwners(field.getOwner());
        }

        Collection<ILink> path = new ArrayList<ILink>();

        Query current = this;

        for (Query query : route) {
            if (query != current.getRootQuery()) {
                ILink link = current.getLinkTo(query);

                if (link != null) {
                    path.add(link);
                }
            }
            current = query;
        }

        return path;
    }

    public Field findFieldById(String id) {
        FindQueryResult result = findQueryById(id, true);

        if (result != null) {
            return result.query.getFieldById(id);
        }

        return null;
    }

    public Field getFieldById(String id) {
        for (Field.CLASS<? extends Field> field : dataFields()) {
            if (id.equals(field.id())) {
                return field.get();
            }
        }
        return null;
    }

    public Field getFieldByName(String name) {
        for (Field.CLASS<? extends Field> field : dataFields()) {
            if (name.equals(field.name())) {
                return field.get();
            }
        }
        return null;
    }

    public Collection<Field.CLASS<? extends Field>> primaryFields() {
        Collection<Field.CLASS<? extends Field>> fields = new ArrayList<Field.CLASS<? extends Field>>();

        for (Field.CLASS<? extends Field> field : dataFields()) {
            fields.add(field);
        }

        return fields;
    }

    public Collection<Field> getPrimaryFields() {
        Collection<Field> dataFields = getDataFields();
        Collection<Field> fields = new ArrayList<Field>(dataFields.size());
        for (Field field : dataFields) {
            if (!(field instanceof Expression)) {
                fields.add(field);
            }
        }
        return fields;
    }

    private boolean isReachableVia(Query query, Field field) {
        if (field instanceof Expression) {
            Expression expression = (Expression) field;
            SqlToken token = expression.expression();

            Set<IValue> values = new HashSet<IValue>();
            token.collectFields(values);

            for (IValue value : values) {
                if (query.findFieldById(value.id()) == null) {
                    return false;
                }
            }
        }

        return query.findFieldById(field.id()) != null;
    }

    public Collection<Field> getFieldsVia(Query query) {
        if (this == query) {
            return getFormFields();
        }

        Collection<Field> result = new ArrayList<Field>();

        for (Field field : getFormFields()) {
            if (isReachableVia(query, field)) {
                result.add(field);
            }
        }

        return result;
    }

    public Collection<Field> getReachableFields(Collection<Field> fields) {
        Collection<Field> result = new ArrayList<Field>();

        for (Field field : fields) {
            if (findFieldById(field.id()) != null) {
                result.add(field);
            }
        }

        return result;
    }

    public Period getPeriod() {
        if (period != null) {
            return period.get();
        }

        Query rootQuery = getRootQuery();

        if (rootQuery != null && rootQuery.period != null) {
            return rootQuery.period.get();
        }

        return null;
    }

    public void setPeriod(Period.CLASS<? extends Period> period) {
        Query rootQuery = getRootQuery();

        if (rootQuery.period != null) {
            rootQuery.period = period;
        } else {
            this.period = period;
        }
    }

    public Collection<Command> commands() {
        return CLASS.asList(commands);
    }

    private boolean isModel() {
        if (contextQuery != null) {
            return contextQuery.getModel() == this;
        }

        return getModel() == null;
    }

    public Collection<Command> getCommands() {
        Collection<Command> result = new ArrayList<Command>();

        if (isModel()) {
            result.addAll(commands());

            if (contextQuery != null) {
                result.addAll(contextQuery.commands());
            }
        }

        return result;
    }

    public Command getCommand(String id) {
        for (Command command : getCommands()) {
            if (command.id().equals(id)) {
                return command;
            }
        }

        return null;
    }

    // ////////////////////////////////////////////////////////////////////////////////////////////////

    public boolean showAsTree() {
        Query rootQuery = getRootQuery();
        return !rootQuery.showAsGrid.get();
    }

    private IAccess getAccess() {
        Map<String, IForm> forms = ApplicationServer.getUser().forms();

        IForm form = forms.get(contextQuery != null ? contextQuery.classId() : classId());

        return form != null ? form.getAccess() : null;
    }

    private boolean readOnly() {
        if (contextQuery != null && contextQuery.readOnly.get()) {
            return true;
        }

        if (readOnly.get()) {
            return true;
        }

        Query rootQuery = getRootQuery();

        if (rootQuery != null && rootQuery != this) {
            return rootQuery.readOnly.get();
        }

        return false;
    }

    public Collection<Field> collectSortFields() {
        Collection<Field> fields = new ArrayList<Field>();

        if (contextQuery != null) {
            fields = contextQuery.getSortFields();
            fields = getReachableFields(fields);
        }

        if (fields.isEmpty()) {
            fields = getSortFields();
        }

        Query rootQuery = getRootQuery();

        if (fields.isEmpty() && rootQuery != null && rootQuery != this) {
            fields = rootQuery.getSortFields();
        }

        return fields;
    }

    public Collection<Field> collectGroupFields() {
        Collection<Field> fields = new ArrayList<Field>();

        if (contextQuery != null) {
            fields = contextQuery.getGroupFields();
            fields = getReachableFields(fields);
        }

        if (fields.isEmpty()) {
            fields = getGroupFields();
        }

        Query rootQuery = getRootQuery();

        if (fields.isEmpty() && rootQuery != null && rootQuery != this) {
            fields = rootQuery.getGroupFields();
        }

        return fields;
    }

    public Collection<Link> collectAggregateByFields() {
        Collection<Link> fields = new ArrayList<Link>();

        Query context = getContext();

        if (context != null) {
            fields = context.getAggregateByFields();
        }

        if (fields.isEmpty()) {
            fields = getAggregateByFields();
        }

        Query rootQuery = getRootQuery();

        if (fields.isEmpty() && rootQuery != null && rootQuery != this) {
            fields = rootQuery.getAggregateByFields();
        }

        return fields;
    }

    public Collection<Field> collectGroupByFields() {
        Collection<Field> fields = new ArrayList<Field>();

        Query context = getContext();

        if (context != null) {
            fields = context.getGroupByFields();
        }

        if (fields.isEmpty()) {
            fields = getGroupByFields();
        }

        Query rootQuery = getRootQuery();

        if (fields.isEmpty() && rootQuery != null && rootQuery != this) {
            fields = rootQuery.getGroupByFields();
        }

        return fields;
    }

    public String getRecordFullText() {
        List<Field> searchFields = getSearchFields();
        int len = 0;
        for (Field field : searchFields) {
            len += field.length.getInt() + 1;
        }
        StringBuilder str = new StringBuilder(len + 10);
        str.append("обращение ");
        for (Field field : searchFields) {
            str.append(field.get().toString()).append(' ');
        }
        return str.toString();
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

    public void writeMeta(JsonObject writer, Collection<Field> fields) {
        Query rootQuery = getRootQuery();

        String name = rootQuery != null ? rootQuery.name() : null;

        writer.put(Json.id, id());
        writer.put(Json.name, name);
        writer.put(Json.icon, icon());

        writeRecordActions(writer);

        writeKeys(writer, fields);

        boolean hasGroupBy = writeGroupByFields(writer, fields);

        writeFields(writer, fields);
        writeOwners(writer);
        writeReports(writer);
        writeCharts(writer);
        writeCommands(writer);
        writePeriod(writer);

        IAccess access = getAccess();

        boolean writeAccess = access != null ? access.getWrite() : true;
        boolean deleteAccess = access != null ? access.getDelete() : true;
        boolean importAccess = access != null ? access.getImport() : true;

        // visuals
        writer.put(Json.text, displayName());

        writer.put(Json.readOnly, hasGroupBy || rootQuery == null ? true : readOnly());
        writer.put(Json.writeAccess, writeAccess);
        writer.put(Json.deleteAccess, deleteAccess);
        writer.put(Json.importAccess, importAccess);

        writer.put(Json.showTotals, showTotals);
        writer.put(Json.columns, columns);
        writer.put(Json.viewMode, viewMode.toString());
        writer.put(Json.width, width);
        writer.put(Json.height, height);
    }

    private void writeRecordActions(JsonObject writer) {
        JsonArray actionsArr = new JsonArray();
        for (RecordActions action : recordActions) {
            actionsArr.put(action.toString());
        }
        writer.put(Json.actions, actionsArr);
    }

    private void writeKeys(JsonObject writer, Collection<Field> fields) {
        Field primaryKey = primaryKey();

        if (primaryKey != null && fields.contains(primaryKey)) {
            writer.put(Json.primaryKey, primaryKey.id());
        }

        Field lockKey = lockKey();

        if (lockKey != null && fields.contains(lockKey)) {
            writer.put(Json.lockKey, lockKey.id());
        }

        Field attachments = attachmentField();

        if (attachments != null && fields.contains(attachments)) {
            writer.put(Json.attachments, attachments.id());
        }

        Field parentKey = parentKey();

        if (parentKey != null && fields.contains(parentKey) && showAsTree()) {
            writer.put(Json.parentKey, parentKey().id());
            writer.put(Json.parentId, guid.NULL.toString());

            writer.put(Json.children, children().id());
            writer.put(Json.parentsSelectable, parentsSelectable);
        }

        if (!recordIds.isEmpty()) {
            writer.put(Json.ids, new JsonArray(recordIds));
        }
    }

    private boolean writeGroupByFields(JsonObject writer, Collection<Field> fields) {
        Collection<? extends Field> groupByFields = collectGroupByFields();

        if (groupByFields.isEmpty()) {
            groupByFields = collectAggregateByFields();
        }

        if (groupByFields.isEmpty()) {
            return false;
        }

        JsonArray groupsArr = new JsonArray();

        for (Field field : groupByFields) {
            assert (fields.contains(field));
            groupsArr.put(field.id());
        }
        writer.put(Json.groups, groupsArr);

        return true;
    }

    private boolean hasRequiredLinks(Collection<ILink> links) {
        for (ILink link : links) {
            Field field = (Field) link;
            if (field.required.get()) {
                return true;
            }
        }

        return false;
    }

    private boolean hasReadOnlyLinks(Collection<ILink> links) {
        for (ILink link : links) {
            Field field = (Field) link;
            if (field.readOnly.get()) {
                return true;
            }
        }

        return false;
    }

    private void writeFields(JsonObject writer, Collection<Field> fields) {
        JsonArray fieldsArr = new JsonArray();

        for (Field field : fields) {
            JsonObject fieldObj = new JsonObject();
            field.writeMeta(fieldObj);

            Collection<ILink> path = getPath(field);
            Query owner = field.getOwner();

            fieldObj.put(Json.depth, path.size());

            boolean readOnly = false;
            boolean required = false;

            if (!path.isEmpty()) {
                String linkId = path.toArray(new ILink[0])[path.size() - 1].id();
                fieldObj.put(Json.linked, true);
                fieldObj.put(Json.linkId, linkId);
                fieldObj.put(Json.linkedVia, owner.primaryKey().id());
                fieldObj.put(Json.groupId, owner.id());

                if (field.editWith == null) {
                    fieldObj.put(Json.editWith, owner.classId());
                    fieldObj.put(Json.editWithText, owner.displayName());
                }

                readOnly = hasReadOnlyLinks(path) || !field.selectable.get();
                required = !readOnly && (hasRequiredLinks(path) || field.required.get());
            } else {
                readOnly = field.readOnly.get();
                required = !readOnly && field.required.get();
            }

            fieldObj.put(Json.required, required);
            fieldObj.put(Json.readOnly, readOnly);

            Collection<Query> owners = QueryUtils.getOwners(path);
            owners.add(owner);

            Collection<Filter> filters = QueryUtils.getFilters(owners);

            JsonArray filtersArr = new JsonArray();

            for (Filter filter : filters) {
                JsonObject filterObj = new JsonObject();
                filter.write(filterObj);
                filtersArr.put(filterObj);
            }

            fieldObj.put(Json.filter, filtersArr);

            if (field.editWith != null) {
                fieldObj.put(Json.editWith, field.editWith.classId());
                fieldObj.put(Json.editWithText, field.editWith.displayName());
            }

            fieldsArr.put(fieldObj);
        }

        writer.put(Json.fields, fieldsArr);
    }

    private void writeOwners(JsonObject writer) {
        JsonArray bwdArr = new JsonArray();
        for (Query owner : getOwners()) {
            writeOwnerMeta(bwdArr, owner);
        }
        writer.put(Json.backwards, bwdArr);
    }

    private void writeReports(JsonObject writer) {
        Collection<ReportInfo> reports = getReports();
        JsonArray reportsArr = new JsonArray();
        for (ReportInfo report : reports) {
            JsonObject reportObj = new JsonObject();
            reportObj.put(Json.id, report.fileName());
            reportObj.put(Json.text, report.displayName());
            reportsArr.put(reportObj);
        }
        writer.put(Json.reports, reportsArr);
    }

    private void writeCharts(JsonObject writer) {
        writer.put(Json.chartType, chartType.toString());

        Collection<Field> series = getChartSeries();
        JsonArray fieldsArr = new JsonArray();

        for (Field field : series) {
            fieldsArr.put(field.id());
        }

        writer.put(Json.chartSeries, fieldsArr);
    }

    private void writeCommands(JsonObject writer) {
        JsonArray commandsArr = new JsonArray();
        for (ICommand command : getCommands()) {
            JsonObject commandObj = new JsonObject();
            command.write(commandObj);
            commandsArr.put(commandObj);
        }
        writer.put(Json.commands, commandsArr);
    }

    private void writePeriod(JsonObject writer) {
        if (period != null) {
            JsonObject periodObj = new JsonObject();
            periodObj.put(Json.period, period.get().type.toString());
            periodObj.put(Json.start, period.get().start);
            periodObj.put(Json.finish, period.get().finish);
            writer.put(Json.period, periodObj);
        }
    }

    private void writeOwnerMeta(JsonArray writer, Query owner) {
        while (owner != null) {
            Query ownerRoot = owner.getRootQuery();

            if (ownerRoot != null && owner.visible.get()) {
                JsonObject obj = new JsonObject();
                obj.put(Json.text, owner.getRootQuery().displayName());
                obj.put(Json.queryId, owner.id());
                obj.put(Json.icon, owner.getRootQuery().icon());
                writer.put(obj);
            }

            int ownersCount = owner.getOwnersCount();

            if (ownersCount == 0) {
                return;
            }

            if (ownersCount == 1 && owner.getOwner().getRootQuery() == owner) {
                owner = owner.getOwner();
            }

            if (ownersCount > 1) {
                for (Query o : owner.getOwners()) {
                    writeOwnerMeta(writer, o);
                }

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

    public void z8_setWhere(sql_bool where) {
        setWhere(where);
    }

    public bool z8_hasRecord(guid recordId) {
        return new bool(hasRecord(recordId));
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
    
    @SuppressWarnings("unchecked")
    public Field.CLASS<? extends Field> z8_getFieldById(string id) {
        return (Field.CLASS) getFieldById(id.get()).getCLASS();
    }

    @SuppressWarnings("unchecked")
    public Field.CLASS<? extends Field> z8_getFieldByName(string name) {
        return (Field.CLASS) getFieldByName(name.get()).getCLASS();
    }

    public guid z8_create() {
        return create();
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
        return new bool(readRecord(id, (Collection) fieldClasses));
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

    public void z8_update(guid id) {
        update(id);
    }

    public void z8_destroy(guid id) {
        destroy(id);
    }

    public void z8_destroy(sql_bool where) {
        destroy(where);
    }

    public bool z8_next() {
        return new bool(next());
    }

    public void z8_onNew(Query.CLASS<? extends Query> query, guid recordId, guid parentId) {}

    public void z8_onCopy(Query.CLASS<? extends Query> query) {}

    public void z8_beforeRead(Query.CLASS<? extends Query> query, guid parentId, Query.CLASS<? extends Query> model,
            guid modelRecordId) {}

    public void z8_afterRead(Query.CLASS<? extends Query> query, guid parentId, Query.CLASS<? extends Query> model,
            guid modelRecordId) {}

    public void z8_beforeCreate(Query.CLASS<? extends Query> query, guid recordId, guid parentId,
            Query.CLASS<? extends Query> model, guid modelRecordId) {}

    public void z8_afterCreate(Query.CLASS<? extends Query> query, guid recordId, guid parentId,
            Query.CLASS<? extends Query> model, guid modelRecordId) {}

    @SuppressWarnings("rawtypes")
    public void z8_beforeUpdate(Query.CLASS<? extends Query> query, guid recordId, RCollection changedFields,
            Query.CLASS<? extends Query> model, guid modelRecordId) {}

    @SuppressWarnings("rawtypes")
    public void z8_afterUpdate(Query.CLASS<? extends Query> query, guid recordId, RCollection changedFields,
            Query.CLASS<? extends Query> model, guid modelRecordId) {}

    public void z8_beforeDestroy(Query.CLASS<? extends Query> query, guid recordId, Query.CLASS<? extends Query> model,
            guid modelRecordId) {}

    public void z8_afterDestroy(Query.CLASS<? extends Query> query, guid recordId, Query.CLASS<? extends Query> model,
            guid modelRecordId) {}

    @SuppressWarnings("rawtypes")
    public void z8_onCommand(Query.CLASS<? extends Query> query, Command.CLASS<? extends Command> command,
            RCollection recordIds) {}

    public void z8_onRender(Query.CLASS<? extends Query> query) {}

    public Style.CLASS<? extends Style> z8_renderRecord(Query.CLASS<? extends Query> query) {
        return null;
    }

    @SuppressWarnings("rawtypes")
    public void z8_onReport(Query.CLASS<? extends Query> query, string report, RCollection recordIds) {}

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Query.CLASS<? extends Query> z8_onFollow(Field.CLASS<? extends Field> fieldClass, RCollection recordIds) {
        Field field = fieldClass.get();

        if (field.editWith != null) {
            return (Query.CLASS<? extends Query>) Loader.loadClass(field.editWith.classId());
        }

        if (field.anchorPolicy == FollowPolicy.Custom) {
            return null;
        }

        Class<?> cls = field.getOwner().getClass();

        String classId = cls.getCanonicalName();

        while (classId.indexOf(".__") != -1) {
            cls = cls.getSuperclass();
            classId = cls.getCanonicalName();
        }

        return (Query.CLASS<? extends Query>) Loader.loadClass(classId);
    }

    public void refresh() {
        IMonitor monitor = ApplicationServer.getMonitor();

        for (Query rootQuery : getRootQueries()) {
            monitor.refresh(rootQuery.name());
        }
    }

    public void z8_refresh() {
        refresh();
    }

    public void refreshRecord(guid id) {
        IMonitor monitor = ApplicationServer.getMonitor();

        for (Query rootQuery : getRootQueries()) {
            monitor.refresh(rootQuery.name(), id);
        }
    }

    public void z8_refreshRecord(guid id) {
        refreshRecord(id);
    }

    public string z8_getRecordFullText() {
        return new string(getRecordFullText());
    }

}
