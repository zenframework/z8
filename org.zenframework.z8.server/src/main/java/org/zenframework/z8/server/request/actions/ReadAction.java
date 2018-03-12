package org.zenframework.z8.server.request.actions;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import org.zenframework.z8.server.base.query.Period;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.query.QueryUtils;
import org.zenframework.z8.server.base.table.value.Aggregation;
import org.zenframework.z8.server.base.table.value.Expression;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.ILink;
import org.zenframework.z8.server.base.table.value.Join;
import org.zenframework.z8.server.base.table.value.JoinType;
import org.zenframework.z8.server.base.table.value.Link;
import org.zenframework.z8.server.base.view.filter.Filter;
import org.zenframework.z8.server.db.AggregatingSelect;
import org.zenframework.z8.server.db.CountingSelect;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.FramedSelect;
import org.zenframework.z8.server.db.Select;
import org.zenframework.z8.server.db.SelectFactory;
import org.zenframework.z8.server.db.sql.SqlConst;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.expressions.And;
import org.zenframework.z8.server.db.sql.expressions.Group;
import org.zenframework.z8.server.db.sql.expressions.Operation;
import org.zenframework.z8.server.db.sql.expressions.Or;
import org.zenframework.z8.server.db.sql.expressions.Rel;
import org.zenframework.z8.server.db.sql.expressions.Unary;
import org.zenframework.z8.server.db.sql.functions.IsNull;
import org.zenframework.z8.server.db.sql.functions.string.Like;
import org.zenframework.z8.server.db.sql.functions.string.Lower;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.security.Privileges;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.types.sql.sql_bool;
import org.zenframework.z8.server.types.sql.sql_string;
import org.zenframework.z8.server.utils.ArrayUtils;

public class ReadAction extends RequestAction {
	static public int DefaultStart = 0;
	static public int DefaultLimit = 200;

	static private final Collection<Field> emptyFieldList = new ArrayList<Field>();

	private Map<Field, Collection<Query>> fieldToQueries = new LinkedHashMap<Field, Collection<Query>>();
	private Map<Query, Collection<ILink>> queryToPath = new LinkedHashMap<Query, Collection<ILink>>();

	protected Collection<Field> selectFields = new LinkedHashSet<Field>();
	protected Collection<Field> groupFields = new LinkedHashSet<Field>();
	protected Collection<Field> sortFields = new LinkedHashSet<Field>();
	protected Collection<Field> groupBy = new LinkedHashSet<Field>();
	protected Collection<Link> aggregateBy = new LinkedHashSet<Link>();
	protected Collection<Field> notNullFields = new HashSet<Field>();

	private Collection<SqlToken> filters = new LinkedHashSet<SqlToken>();
	private Collection<Field> filterFields = new LinkedHashSet<Field>();

	private Collection<SqlToken> groupFilters = new LinkedHashSet<SqlToken>();
	private Collection<Field> groupFilterFields = new LinkedHashSet<Field>();

	guid parentId = null;

	private int start = DefaultStart;
	private int limit = -1;

	private int totalCount = 0;

	private Field primaryKey = null;
	private Field parentKey = null;

	private boolean hasRightJoin = false;

	public ReadAction(Query query) {
		this(new ActionConfig(query));
	}

	public ReadAction(Query query, guid recordId) {
		this(new ActionConfig(query, null, recordId != null ? Arrays.asList(recordId) : null));
	}

	public ReadAction(Query query, Collection<Field> fields) {
		this(new ActionConfig(query, fields));
	}

	public ReadAction(Query query, Collection<Field> fields, guid recordId) {
		this(new ActionConfig(query, fields, recordId != null ? Arrays.asList(recordId) : null));
	}

	public ReadAction(Query query, Collection<Field> fields, Collection<guid> recordIds) {
		this(new ActionConfig(query, fields, recordIds));
	}

	public ReadAction(ActionConfig config) {
		super(config);
		initialize();
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	protected void initialize() {
		ActionConfig parameters = config();

		Query query = getQuery();

		Collection<Field> fields = parameters.fields != null && !parameters.fields.isEmpty() ? parameters.fields : getFormFields();
		Collection<Field> sortFields = parameters.sortFields != null ? parameters.sortFields : emptyFieldList;
		Collection<Field> groupFields = parameters.groupFields != null ? parameters.groupFields : emptyFieldList;
		Collection<Field> groupBy = parameters.groupBy != null ? parameters.groupBy : query.getGroupByFields();
		Collection<Link> aggregateBy = query.getAggregateByFields();

		for(Link field : aggregateBy)
			addAggregateByField(field);

		for(Field field : groupBy)
			addGroupByField(field);

		for(Field field : fields)
			addSelectField(field);

		for(Link field : aggregateBy)
			addSelectField(field);

		for(Field field : groupBy)
			addSelectField(field);

		for(Field field : sortFields)
			addSortField(field);

		for(Field field : groupFields)
			addGroupField(field);

		if(hasPrimaryKey()) {
			primaryKey = addSelectField(query.primaryKey());
			parentKey = addSelectField(query.parentKey());

			if(parameters.requestId != null) {
				addSelectField(query.lockKey());
				addSelectField(query.attachmentKey());
			}
		}

		beforeRead();
	}

	private void initFilters() {
		Query query = getQuery();

		Field primaryKey = query.primaryKey();
		addNullRecordFilter(primaryKey);

		Collection<guid> recordIds = config().recordIds;
		addFilter(primaryKey, recordIds, Operation.Eq);

		for(Field field : notNullFields)
			addNullRecordFilter(field);

		if(recordIds == null) {
			addFilter(query.where());

			collectFilters();

			Collection<String> lookupFields = getLookupFields();
			if(lookupFields.size() != 0)
				addLikeFilter(lookupFields, getLookupParameter());

			Filter filter = new Filter(getFilterParameter(), query);
			addFilter(filter.where());

			Filter where = new Filter(getWhereParameter(), query);
			addFilter(where.where());

			Filter quickFilter = new Filter(getQuickFilterParameter(), query);
			addFilter(quickFilter.where());

			Period period = new Period(query.periodKey(), getPeriodParameter());
			addFilter(period.where());
		}
	}

	private boolean hasPrimaryKey() {
		return groupBy.isEmpty() && aggregateBy.isEmpty();
	}

	@SuppressWarnings("unused")
	private boolean isGrouped() {
		return !groupBy.isEmpty() || !aggregateBy.isEmpty();
	}

	public Collection<Field> getSelectFields() {
		return selectFields;
	}

	public Collection<Field> getSortFields() {
		return sortFields;
	}

	public Collection<Field> getGroupFields() {
		return groupFields;
	}

	public Collection<Field> getGroupByFields() {
		return groupBy;
	}

	public Collection<Link> getAggregateByFields() {
		return aggregateBy;
	}

	public Collection<Field> getUsedFields() {
		return fieldToQueries.keySet();
	}

	public Collection<Query> getQueries(Collection<Field> fields) {
		Collection<Query> result = new LinkedHashSet<Query>();

		for(Field field : fields) {
			Collection<Query> queries = fieldToQueries.get(field);

			if(queries != null)
				result.addAll(queries);
		}

		return result;
	}

	public Collection<Query> getFilterQueries() {
		Collection<Query> queries = new HashSet<Query>();

		for(Field field : filterFields)
			queries.addAll(fieldToQueries.get(field));

		return queries;
	}

	public Collection<Query> getGroupFilterQueries() {
		Collection<Query> queries = new HashSet<Query>();

		for(Field field : groupFilterFields)
			queries.addAll(fieldToQueries.get(field));

		return queries;
	}

	public SqlToken getFilter() {
		SqlToken result = null;

		for(SqlToken filter : filters)
			result = result == null ? filter : new And(result, filter);

		if(result != null && !(result instanceof Group))
			result = new Group(result);

		return result;
	}

	public SqlToken getGroupFilter() {
		SqlToken result = null;

		for(SqlToken filter : groupFilters)
			result = result == null ? filter : new And(result, filter);

		if(result != null && !(result instanceof Group))
			result = new Group(result);

		return result;
	}

	public void addFilter(SqlToken filter) {
		collectFilterQueries(filter, filters, filterFields);
	}

	public void addGroupFilter(SqlToken filter) {
		collectFilterQueries(filter, groupFilters, groupFilterFields);
	}

	public void addFilter(Field field, guid keyValue) {
		addFilter(field, keyValue, Operation.Eq);
	}

	private Collection<ILink> getPath(Field field) {
		Query owner = field.owner();
		if(owner != null) {
			Collection<ILink> path = getQuery().getPath(owner);
			if(path != null && !path.isEmpty())
				return getPath(owner);
		}
		return new ArrayList<ILink>();
	}

	private Collection<ILink> getPath(Query query) {
		Collection<ILink> links = queryToPath.get(query);

		if(links != null)
			return links;

		links = new LinkedHashSet<ILink>();

		Collection<ILink> path = getQuery().getPath(query);

		if(path == null)
			path = getContextQuery().getPath(query);

		for(ILink link : path) {
			if(link instanceof Expression) {
				Collection<Field> usedFields = getUsedFields((Field)link);
				Collection<Query> owners = getOwners(usedFields);

				for(Query owner : owners) {
					if(owner != query)
						links.addAll(getPath(owner));
				}
			}

			links.add(link);

			if(link.getJoin() == JoinType.Right)
				hasRightJoin = true;
		}

		queryToPath.put(query, links);

		return links;
	}

	public Collection<ILink> getLinks(Collection<Query> queries) {
		Collection<ILink> links = new LinkedHashSet<ILink>();

		for(Query query : queries)
			links.addAll(getPath(query));

		return links;
	}

	private boolean checkAggregation(Field field) {
		if(!groupBy.isEmpty())
			return field.aggregation != Aggregation.None || groupBy.contains(field);

		return true;
	}

	private void addGroupByField(Field field) {
		if(field != null)
			groupBy.add(field);
	}

	private void addAggregateByField(Link link) {
		if(link != null)
			aggregateBy.add(link);
	}

	private Field addSelectField(Field field) {
		if(field != null && checkAggregation(field)) {
			selectFields.add(field);

			if(hasPrimaryKey()) {
				Collection<ILink> links = getPath(field);

				for(ILink link : links) {
					if(!(link instanceof Join))
						selectFields.add((Field)link);

					if(link.getJoin() == JoinType.Right) {
						Field primaryKey = link.getQuery().primaryKey();
						selectFields.add(primaryKey);
						notNullFields.add(primaryKey);
					}
				}

				field.setPath(links);
			}

			collectUsedQueries(field);
		}
		return field;
	}

	private void addSortField(Field field) {
		if(field != null) {
			sortFields.add(field);
			collectUsedQueries(field);
		}
	}

	private void addGroupField(Field field) {
		if(field != null) {
			groupFields.add(field);
			collectUsedQueries(field);
		}
	}

	private void collectUsedQueries(Field field) {
		if(field != null && !fieldToQueries.containsKey(field)) {
			Collection<Field> usedFields = getUsedFields(field);
			Collection<Query> queries = getOwners(usedFields);

			for(Query query : queries.toArray(new Query[0])) {
				for(ILink link : getPath(query)) {
					Field linkField = (Field)link;
					if(linkField instanceof Expression) {
						for(Field usedField : getUsedFields(linkField))
							queries.add(usedField.owner());
					} else
						queries.add(linkField.owner());
				}
			}

			fieldToQueries.put(field, queries);
		}
	}

	private Collection<Query> getOwners(Collection<Field> fields) {
		Collection<Query> queries = new LinkedHashSet<Query>();

		for(Field field : fields)
			queries.add(field.owner());

		return queries;
	}

	private Collection<Query> getAllQueries() {
		Collection<Query> queries = new HashSet<Query>();

		for(Collection<Query> query : fieldToQueries.values())
			queries.addAll(query);

		return queries;
	}

	private void collectFilters() {
		Collection<Query> queries = getAllQueries();

		for(Query query : queries) {
			if(query != getQuery()) {
				collectFilterQueries(query.where(), filters, filterFields);
				collectFilterQueries(query.having(), groupFilters, groupFilterFields);
			}
		}
	}

	private void collectFilterQueries(SqlToken filter, Collection<SqlToken> filters, Collection<Field> filterFields) {
		if(filter != null && !(filter == sql_bool.True) && !filters.contains(filter)) {
			Collection<Field> fields = getUsedFields(filter);

			for(Field field : fields)
				collectUsedQueries(field);

			filterFields.addAll(fields);
			filters.add(filter);
		}
	}

	private Collection<Field> getUsedFields(Field field) {
		if(field instanceof Expression) {
			Expression expression = (Expression)field;
			return getUsedFields(expression.expression());
		}

		return Arrays.asList(field);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Collection<Field> getUsedFields(SqlToken token) {
		return token != null ? (Collection)token.getUsedFields() : new LinkedHashSet<Field>();
	}

	private SqlToken getIsNullFilter(Field field, Operation operation) {
		if(field == null)
			return null;

		SqlToken token = new IsNull(new SqlField(field));

		return operation == Operation.Not ? new Unary(operation, token) : token;
	}

	private SqlToken getFilter(Field field, guid value, Operation operation) {
		return field != null && value != null ? new Rel(field, operation, value.sql_guid()) : null;
	}

	private SqlToken getFilter(Field field, Collection<guid> value, Operation operation) {
		if(field == null || value == null)
			return null;
		return value.size() == 1 ? getFilter(field, value.iterator().next(), operation) : field.inVector(value);
	}

	private void addNullRecordFilter(Field primaryKey) {
		SqlToken left = getFilter(primaryKey, guid.Null, Operation.NotEq);

		if(hasRightJoin) {
			SqlToken right = getIsNullFilter(primaryKey, Operation.None);
			addFilter(new Group(new Or(left, right)));
		} else
			addFilter(left);
	}

	private void addFilter(Field field, guid value, Operation operation) {
		addFilter(getFilter(field, value, operation));
	}

	private void addFilter(Field field, Collection<guid> value, Operation operation) {
		addFilter(getFilter(field, value, operation));
	}

	private void addLikeFilter(Collection<String> fields, String lookup) {
		if(fields.isEmpty() || lookup == null || lookup.isEmpty())
			return;

		Query query = getQuery();

		SqlToken filter = null;

		for(String id : fields) {
			Field field = query.findFieldById(id);
			FieldType type = field.type();

			if(type == FieldType.String || type == FieldType.Text || type == FieldType.Attachments) {
				Lower left = new Lower(field);
				sql_string right = new sql_string('%' + lookup.toLowerCase() + '%');
				SqlToken like = new Like(left, right);
				filter = filter != null ? new Or(filter, like) : like;
			}
		}

		if(filter != null)
			addFilter(new Group(filter));
	}

	protected Collection<String> parseValues(String jsonData) {
		Collection<String> result = new ArrayList<String>();

		if(jsonData.isEmpty()) {
			result.add("");
			return result;
		}

		char startChar = jsonData.charAt(0);

		if(startChar == '[') { // array or guids
			JsonArray values = new JsonArray(jsonData);

			for(int index = 0; index < values.length(); index++) {
				String value = values.getString(index);
				result.add(value);
			}
		} else if(startChar == '{') { // Period
			JsonObject values = new JsonObject(jsonData);
			String start = values.getString(Json.start);
			String finish = values.getString(Json.finish);

			result.add(start);
			result.add(finish);
		} else
			result.add(jsonData);

		return result;
	}

	protected String parseJsonProperty(JsonObject json, string property) {
		return json.getString(property);
	}

	public Select getCursor() {
		try {
			Select cursor = cursor();
			cursor.open();

			return cursor;
		} finally {
			afterRead();
		}
	}

	private Select cursor() {
		return SelectFactory.create(this).cursor();
	}

	public CountingSelect getCounter() {
		try {
			return counter();
		} finally {
			afterRead();
		}

	}

	private CountingSelect counter() {
		return SelectFactory.create(this).count();
	}

	private FramedSelect frame() {
		return SelectFactory.create(this).frame();
	}

	public AggregatingSelect getTotals() {
		try {
			AggregatingSelect select = totals();
			select.open();

			return select;
		} finally {
			afterRead();
		}

	}

	private AggregatingSelect totals() {
		return SelectFactory.create(this).aggregate();
	}

	private int writeCount(JsonWriter writer) throws SQLException {
		CountingSelect counter = counter();
		int count = counter.count();
		writer.writeProperty(Json.total, count);
		return count;
	}

	class Groupping {
		Groupping(Collection<Field> fields) {
			this.fields = fields.toArray(new Field[0]);
			firstGroup = new primary[this.fields.length];
			lastGroup = new primary[this.fields.length];
		}

		boolean isEmpty() {
			return this.fields.length == 0;
		}

		Field[] fields;
		primary[] firstGroup;
		primary[] lastGroup;
	}

	/*
	* группировка для chart'ов
	*/
	@SuppressWarnings("unused")
	private void writeGroupTotals(JsonWriter writer, Field groupField) throws SQLException {
		Select select = cursor();

		Collection<Field> fields = getTotalsFields(select.getFields());
		fields.add(groupField);

		if(select.isGrouped()) {
			select.setFields(fields);

			Select summary = new Select();
			summary.setSubselect(select);
			summary.setFields(select.getFields());
			summary.setGroupBy(Arrays.asList(groupField));

			select = summary;
		} else {
			select.setFields(fields);
			select.setGroupBy(Arrays.asList(groupField));
		}

		Select frame = new FramedSelect(select, DefaultStart, DefaultLimit);

		frame.aggregate();

		writer.startArray(Json.data);

		while(frame.next()) {
			writer.startObject();

			for(Field field : frame.getFields())
				field.writeData(writer);

			writer.finishObject();
		}

		writer.finishArray();
	}

	private Groupping writeFrame(JsonWriter writer) throws SQLException {
		FramedSelect frame = frame();

		try {
			frame.saveState();

			frame.open();

			if(parentKey == null)
				return writePlainData(frame, writer);

			writeTreeData(frame, writer);
			return null;
		} finally {
			frame.restoreState();
			frame.close();
		}
	}

	private Groupping writePlainData(Select cursor, JsonWriter writer) {
		Groupping groups = new Groupping(this.groupFields);

		if(primaryKey != null)
			primaryKey.setWriteNulls(false);

		writer.startArray(Json.data);

		int records = 0;
		while(cursor.next()) {
			if(records > 1000)
				throw new RuntimeException("Too many records fetched for a json client. Use limit parameter.");

			writer.startObject();

			primary[] group = records == 0 ? groups.firstGroup : groups.lastGroup;

			for(Field field : getSelectFields()) {
				int index = ArrayUtils.indexOf(groups.fields, field);

				if(index != -1)
					group[index] = field.get();

				field.writeData(writer);
			}
			records++;

			writer.finishObject();
		}

		writer.finishArray();

		return records != 0 && groups.fields.length != 0 ? groups : null;
	}

	class TreeData {
		guid recordId;
		guid parentId;
		JsonWriter data;

		public TreeData(JsonWriter data, guid recordId, guid parentId) {
			this.data = data;
			this.recordId = recordId;
			this.parentId = parentId;
		}
	}

	private void writeTreeData(Select cursor, JsonWriter writer) {
		Collection<TreeData> roots = new ArrayList<TreeData>();
		Map<guid, Collection<TreeData>> map = new LinkedHashMap<guid, Collection<TreeData>>();

		writer.startArray(Json.data);

		int records = 0;
		while(cursor.next()) {
			if(records > 500)
				throw new RuntimeException("Too many records fetched for a json client. Use limit parameter.");

			records++;

			JsonWriter data = new JsonWriter();
			data.startObject();

			guid parentId = null;
			guid recordId = null;

			for(Field field : getSelectFields()) {
				field.writeData(data);
				if(field == parentKey)
					parentId = field.guid();
				if(field == primaryKey)
					recordId = field.guid();
			}

			// finish object we'll call later;

			TreeData treeData = new TreeData(data, recordId, parentId);
			if(!parentId.isNull()) {
				Collection<TreeData> children = map.get(parentId);
				if(children == null) {
					children = new ArrayList<TreeData>();
					map.put(parentId, children);
				}
				children.add(treeData);
			} else
				roots.add(treeData);
		}

		writeTreeData(writer, roots, map, 0);

		writer.finishArray();
	}

	private void writeTreeData(JsonWriter writer, Collection<TreeData> records, Map<guid, Collection<TreeData>> childrenMap, int level) {
		for(TreeData record : records) {
			Collection<TreeData> children = childrenMap.get(record.recordId);
			JsonWriter data = record.data;
			data.writeProperty(Json.hasChildren, children != null);
			data.writeProperty(Json.level, level);
			data.finishObject(); // start object was called in writeTreeData(Select, JsonWriter)
			writer.write(data);

			if(children != null) {
				writeTreeData(writer, children, childrenMap, level + 1);
				childrenMap.remove(record.recordId);
			}
		}

		if(level == 0 && !childrenMap.isEmpty()) {
			for(guid recordId : childrenMap.keySet().toArray(new guid[0])) {
				Collection<TreeData> children = childrenMap.remove(recordId);
				if(children != null)
					writeTreeData(writer, children, childrenMap, 0);
			}
		}
	}

	private void writeTotals(JsonWriter writer) throws SQLException {
		AggregatingSelect totals = totals();

		totals.setFields(getTotalsFields(totals.getFields()));

		try {
			totals.open();

			writer.startObject(Json.data);

			if(totals.next()) {
				for(Field field : totals.getFields())
					field.writeData(writer);
			}

			writer.finishObject();
		} finally {
			totals.close();
		}
	}

	private Collection<Field> getTotalsFields(Collection<Field> fields) {
		Collection<Field> result = new ArrayList<Field>();

		for(Field field : fields) {
			Aggregation aggregation = field.aggregation;

			if(aggregation == Aggregation.None || aggregation == Aggregation.Min || aggregation == Aggregation.Max)
				continue;

			if(aggregation == Aggregation.Count)
				field.aggregation = Aggregation.Sum;

			result.add(field);
		}

		return result;
	}

	@SuppressWarnings("unused")
	private void writeSummary(JsonWriter writer, Groupping groups) throws SQLException {
		sortFields.clear();

		Field groupField = groups.fields[0];

		SqlToken where = new Rel(groupField, Operation.Eq, new SqlConst(groups.firstGroup[0]));

		if(groups.lastGroup[0] != null && !groups.firstGroup[0].equals(groups.lastGroup[0])) {
			SqlToken last = new Rel(new SqlField(groupField), Operation.Eq, new SqlConst(groups.lastGroup[0]));
			where = new Group(new Or(where, last));
		}

		Select select = cursor();

		select.addWhere(where);

		Collection<Field> fields = getTotalsFields(select.getFields());

		Field expression = createAggregatedExpression(groupField, groupField.aggregation, groupField.type());
		Field count = createAggregatedExpression(groupField, Aggregation.Count, FieldType.Integer);

		fields.add(groupField);

		if(expression != null && expression != groupField)
			fields.add(expression);

		if(count != groupField)
			fields.add(count);

		select.setFields(fields);

		Select summary = new Select();
		summary.setSubselect(select);
		summary.setFields(select.getFields());
		summary.setGroupBy(Arrays.asList(groupField));

		try {
			summary.aggregate();

			writer.startObject(Json.summaryData);

			while(summary.next()) {
				writer.startObject(JsonObject.quote(groupField.get().toString()));

				for(Field field : summary.getFields()) {
					if(field == groupField)
						writer.writeProperty(Json.group, groupField.get());
					else if(field == expression)
						writer.writeProperty(JsonObject.quote(groupField.id()), expression.get());
					else if(field == count)
						writer.writeProperty(Json.total, count.get());
					else
						field.writeData(writer);
				}

				writer.finishObject();
			}

			writer.finishObject();
		} finally {
			summary.close();
		}
	}

	private void beforeRead() {
		Query query = getQuery();
		Field parentKey = query.parentKey();

		parentId = parentKey != null ? getParentIdParameter() : null;

		QueryUtils.setFieldValues(getContextQuery(), getRequestParameter(Json.values));

		query.beforeRead(parentId);

		initFilters();
	}

	private void afterRead() {
		getQuery().afterRead(parentId);
	}

	@Override
	public void writeResponse(JsonWriter writer) throws Throwable {
		Query query = getQuery();

		if(!query.access().read()) {
			ApplicationServer.getMonitor().warning(Privileges.displayNames.NoReadAccess);
			return;
		}

		try {
			JsonArray data = query.getData();
			if(data != null)
				writer.writeProperty(Json.data, data);
			else
				writeData(writer);
		} finally {
			afterRead();
		}
	}

	private void writeData(JsonWriter writer) throws Throwable {
		if(getTotalsParameter()) {
			writeTotals(writer);
			return;
		}

		limit = parentKey != null ? -1 : getRequestParameter(Json.limit, DefaultLimit);
		start = getRequestParameter(Json.start, DefaultStart);

		if(getCountParameter()) {
			totalCount = writeCount(writer);

			if(totalCount == 0 || start <= totalCount)
				return;

			int pages = totalCount / limit + (totalCount % limit != 0 ? 1 : 0);
			start = (pages - 1) * limit;
		}

		writeFrame(writer);
	}

	private Field createAggregatedExpression(final Field field, Aggregation aggregation, FieldType type) {
		if(aggregation == Aggregation.None)
			return null;

		if(field.aggregation == aggregation)
			return field;

		Expression expression = new Expression(new SqlField(field), type);
		expression.aggregation = aggregation;
		return expression;
	}

	public int getTotalCount() {
		return totalCount;
	}
}
