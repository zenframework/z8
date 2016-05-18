package org.zenframework.z8.server.base.model.actions;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.zenframework.z8.server.base.model.sql.AggregatingSelect;
import org.zenframework.z8.server.base.model.sql.CountingSelect;
import org.zenframework.z8.server.base.model.sql.FramedSelect;
import org.zenframework.z8.server.base.model.sql.Select;
import org.zenframework.z8.server.base.model.sql.SelectFactory;
import org.zenframework.z8.server.base.query.Period;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.query.ReadLock;
import org.zenframework.z8.server.base.query.Style;
import org.zenframework.z8.server.base.table.value.Aggregation;
import org.zenframework.z8.server.base.table.value.Expression;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.ILink;
import org.zenframework.z8.server.base.table.value.Link;
import org.zenframework.z8.server.base.view.filter.Filter;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.SqlConst;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.expressions.And;
import org.zenframework.z8.server.db.sql.expressions.Group;
import org.zenframework.z8.server.db.sql.expressions.Operation;
import org.zenframework.z8.server.db.sql.expressions.Or;
import org.zenframework.z8.server.db.sql.expressions.Rel;
import org.zenframework.z8.server.db.sql.expressions.True;
import org.zenframework.z8.server.db.sql.expressions.Unary;
import org.zenframework.z8.server.db.sql.functions.IsNull;
import org.zenframework.z8.server.db.sql.functions.string.Like;
import org.zenframework.z8.server.db.sql.functions.string.Lower;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.types.sql.sql_string;
import org.zenframework.z8.server.utils.ArrayUtils;

public class ReadAction extends Action {
	private static final Collection<Field> emptyFieldList = new ArrayList<Field>();

	private Map<Field, Collection<Query>> fieldToQueries = new LinkedHashMap<Field, Collection<Query>>();
	private Map<Query, Collection<ILink>> queryToPath = new LinkedHashMap<Query, Collection<ILink>>();

	protected Collection<Field> selectFields = new LinkedHashSet<Field>();
	protected Collection<Field> groupFields = new LinkedHashSet<Field>();
	protected Collection<Field> sortFields = new LinkedHashSet<Field>();
	protected Collection<Field> groupBy = new LinkedHashSet<Field>();
	protected Collection<Link> aggregateBy = new LinkedHashSet<Link>();

	private Collection<SqlToken> filters = new LinkedHashSet<SqlToken>();
	private Collection<Field> filterFields = new LinkedHashSet<Field>();

	private Collection<SqlToken> groupFilters = new LinkedHashSet<SqlToken>();
	private Collection<Field> groupFilterFields = new LinkedHashSet<Field>();

	guid recordId = null;
	guid modelRecordId = null;
	guid parentId = null;

	public ReadAction(Query query) {
		this(new ActionParameters(query), null);
	}

	public ReadAction(Query query, guid recordId) {
		this(new ActionParameters(query), recordId);
	}

	public ReadAction(Query query, Collection<Field> fields) {
		this(new ActionParameters(query, fields), null);
	}

	public ReadAction(Query query, Collection<Field> fields, guid recordId) {
		this(new ActionParameters(query, fields), recordId);
	}

	public ReadAction(ActionParameters actionParameters) {
		this(actionParameters, null);
	}

	protected ReadAction(ActionParameters actionParameters, guid recordId) {
		super(actionParameters);

		this.recordId = recordId;

		initialize();
	}

	protected void initialize() {
		ActionParameters parameters = actionParameters();

		initFields();
		initPeriod();
		initQuery();

        beforeRead();

        Query query = getQuery();

		Collection<Field> fields = parameters.fields != null && !parameters.fields.isEmpty() ? parameters.fields : query.getFormFields();
		Collection<Field> sortFields = parameters.sortFields != null ? parameters.sortFields : emptyFieldList;
		Collection<Field> groupFields = parameters.groupFields != null ? parameters.groupFields : emptyFieldList;
		Collection<Field> groupBy = parameters.groupBy != null ? parameters.groupBy : query.collectGroupByFields();
		Collection<Link> aggregateBy = parameters.aggregateBy != null ? parameters.aggregateBy : query.collectAggregateByFields();

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

		if(hasPrimaryKeys()) {
			for(Field primaryKey : query.primaryKeys())
				addSelectField(primaryKey);

			addSelectField(query.parentKey());
			addSelectField(query.parentKeys());
			addSelectField(query.children());

			if(parameters.requestId != null) {
				addSelectField(query.lockKey());
				addSelectField(query.getAttachmentField());
			}
		}
	}

	private void initializeFilters() {
		ActionParameters parameters = actionParameters();
		Query query = getQuery();

		collectUsedQueries(parameters.keyField);

		for(Field primaryKey : query.primaryKeys()) {
			addNullRecordFilter(primaryKey);
			addFilter(primaryKey, recordId, Operation.Eq);
		}

		if(recordId == null) {
			Query context = query.getContext();
			String fieldId = getParameters().get(Json.fieldId);

			if(context != null && fieldId == null) {
				addFilter(context.where());

				Query contextRoot = context.getRootQuery();

				if(contextRoot != null)
					addFilter(contextRoot.where());
			} else
				addFilter(query.where());

			collectFilters();

			guid recordId = getRecordIdParameter();
			guid filterBy = getFilterByParameter();

			addFilter(parameters.keyField, recordId);
			addFilter(query.primaryKey(), filterBy);

			if(filterBy == null && query.showAsTree()) {
				String id = getRequestParameter(Json.parentId);

				if(id != null) {
					guid parentId = id.isEmpty() ? guid.NULL : new guid(id);
					addFilter(query.parentKey(), parentId);
				}
			}

			String[] lookupFields = getLookupFields();
			if(lookupFields.length != 0)
				addLikeFilter(lookupFields, getLookupParameter());

			Collection<Filter> filters = Filter.parse(getFilterParameter(), query);
			for(Filter filter : filters) {
				// if (isGrouped()) {
				// addGroupFilter(filter.where());
				// } else {
				addFilter(filter.where());
				// }
			}

			if(isGrouped())
				addGroupFilter(getFilter1());
			else
				addFilter(getFilter1());
		}
	}

	private boolean hasPrimaryKeys() {
		return groupBy.isEmpty() && aggregateBy.isEmpty();
	}

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

	private Collection<ILink> getLinks(Field field) {
		Query owner = field.owner();

		if(!(field instanceof Expression) || getQuery().getPath(owner).size() != 0)
			return getLinks(owner);

		return new ArrayList<ILink>();
	}

	private Collection<ILink> getLinks(Query query) {
		Collection<ILink> links = queryToPath.get(query);

		if(links != null)
			return links;

		links = new LinkedHashSet<ILink>();

		for(ILink link : getQuery().getPath(query)) {
			if(link instanceof Expression) {
				Collection<Field> usedFields = getUsedFields((Field)link);
				Collection<Query> owners = getOwners(usedFields);

				for(Query owner : owners)
					links.addAll(getLinks(owner));
			}

			links.add(link);
		}

		queryToPath.put(query, links);

		return links;
	}

	public Collection<ILink> getLinks(Collection<Query> queries) {
		Collection<ILink> links = new LinkedHashSet<ILink>();

		for(Query query : queries)
			links.addAll(getLinks(query));

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

	private void addSelectField(Field field) {
		if(field != null && checkAggregation(field)) {
			selectFields.add(field);

			if(hasPrimaryKeys()) {
				Collection<ILink> links = getLinks(field);

				if(!links.isEmpty()) {
					for(ILink link : links)
						selectFields.add((Field)link);

					Query owner = field.owner();
					Field primaryKey = owner.primaryKey();

					if(primaryKey != null)
						selectFields.add(primaryKey);
				}
			}

			collectUsedQueries(field);
		}
	}

	private void addSelectField(Field[] fields) {
		for(Field field : fields)
			addSelectField(field);
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
				for(ILink link : getLinks(query)) {
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
			collectFilterQueries(query.where(), filters, filterFields);
			collectFilterQueries(query.having(), groupFilters, groupFilterFields);
		}
	}

	private void collectFilterQueries(SqlToken filter, Collection<SqlToken> filters, Collection<Field> filterFields) {
		if(filter != null && !(filter instanceof True) && !filters.contains(filter)) {
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

		Collection<Field> fields = getFormulaFields(field);
		fields.add(field);

		return fields;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Collection<Field> getUsedFields(SqlToken token) {
		return token != null ? (Collection)token.getUsedFields() : new LinkedHashSet<Field>();
	}

	private void getFormulaFields(Field field, Collection<Field> result, Set<Field> processedFields) {
		if(processedFields == null)
			processedFields = new LinkedHashSet<Field>();

		if(!processedFields.contains(field)) {
			processedFields.add(field);

			Collection<Field> fields = getUsedFields(field.formula());
			for(Field f : fields) {
				result.add(f);
				getFormulaFields(f, result, processedFields);
			}
		}
	}

	private Collection<Field> getFormulaFields(Field field) {
		Collection<Field> result = new LinkedHashSet<Field>();
		getFormulaFields(field, result, null);
		return result;
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

	private void addNullRecordFilter(Field primaryKey) {
		SqlToken left = getFilter(primaryKey, guid.NULL, Operation.NotEq);
		SqlToken right = getIsNullFilter(primaryKey, Operation.None);
		addFilter(new Group(new Or(left, right)));
	}

	private void addFilter(Field field, guid value, Operation operation) {
		addFilter(getFilter(field, value, operation));
	}

	private void addLikeFilter(String[] fields, String lookup) {
		if(fields.length == 0 || lookup == null || lookup.isEmpty())
			return;

		Query query = getQuery();

		SqlToken filter = null;

		for(int index = 0; index < fields.length; index++) {
			Field field = query.findFieldById(fields[index]);
			FieldType type = field.type();

			if(type == FieldType.String || type == FieldType.Text) {
				Lower left = new Lower(field);
				sql_string right = new sql_string((index != 0 ? "%" : "") + lookup.toLowerCase() + "%");
				SqlToken like = new Like(left, right, null);

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

	public String getFilterAsText() {
		String fieldFilter = getFieldFiltersText();
		String filter1Text = getFilter1Text();

		String result = fieldFilter != null && !fieldFilter.isEmpty() ? fieldFilter : "";

		if(filter1Text == null || filter1Text.isEmpty())
			return result;

		return !result.isEmpty() ? result + " " + Operation.And.toReadableString() + " " + filter1Text : result;
	}

	protected String getFieldFiltersText() {
		String result = null;

		String filterData = getFilterParameter();

		if(filterData == null)
			return result;

		JsonArray filters = new JsonArray(filterData);

		for(int index = 0; index < filters.length(); index++) {
			JsonObject filter = (JsonObject)filters.get(index);

			if(filter.has(Json.value)) {
				Field field = getQuery().findFieldById(filter.getString(filter.has(Json.field) ? Json.field : Json.property));
				Collection<String> values = parseValues(filter.getString(Json.value));
				String comparison = filter.has(Json.comparison) ? filter.getString(Json.comparison) : filter.has(Json.operator) ? filter.getString(Json.operator) : null;
				Operation operation = comparison != null ? Operation.fromString(comparison) : null;

				Filter f = new Filter(field, operation, values);

				if(result == null)
					result = f.toString();
				else
					result += " " + Operation.And.toReadableString() + " " + f.toString();
			}
		}

		return result;
	}

	protected String getFilter1Text() {
		String filter1 = getFilter1Parameter();

		if(filter1 == null || filter1.isEmpty())
			return null;

		String result = null;

		JsonObject object = new JsonObject(filter1);
		JsonArray items = object.getJsonArray(Json.items);

		for(int index = 0; index < items.length(); index++) {
			JsonObject filter = (JsonObject)items.get(index);

			String fieldId = parseJsonProperty(filter, filter.has(Json.field) ? Json.field : Json.property);
			String value = parseJsonProperty(filter, Json.value);
			String operator = parseJsonProperty(filter, Json.operator);

			if(fieldId != null && value != null && operator != null) {
				Field field = getQuery().findFieldById(fieldId);
				Collection<String> values = parseValues(value);
				Operation operation = Operation.fromString(operator);

				Filter f = new Filter(field, operation, values);

				if(index != 0) {
					String andOr = parseJsonProperty(filter, Json.andOr);

					if("and".equals(andOr))
						result += " " + Operation.And.toReadableString() + f.toString();
					else if("or".equals(andOr))
						result += " " + Operation.Or.toReadableString() + f.toString();
					else
						throw new RuntimeException("Read.getFilter1Text() unknown token '" + andOr + "'");
				} else
					result = f.toString();
			}
		}

		return result;
	}

	protected SqlToken getFilter1() {
		String filter1 = getFilter1Parameter();

		if(filter1 == null || filter1.isEmpty())
			return null;

		SqlToken result = null;

		JsonObject object = new JsonObject(filter1);
		JsonArray items = object.getJsonArray(Json.items);

		for(int index = 0; index < items.length(); index++) {
			JsonObject filter = (JsonObject)items.get(index);

			String fieldId = parseJsonProperty(filter, filter.has(Json.field) ? Json.field : Json.property);
			String value = parseJsonProperty(filter, Json.value);
			String operator = parseJsonProperty(filter, Json.operator);

			if(fieldId != null && value != null && operator != null) {
			    Field field = getQuery().findFieldById(fieldId);
				Collection<String> values = parseValues(value);
				Operation operation = Operation.fromString(operator);

				SqlToken token = new Filter(field, operation, values).where();

				if(index != 0) {
					String andOr = parseJsonProperty(filter, Json.andOr);

					if("and".equals(andOr))
						result = new And(result, token);
					else if("or".equals(andOr))
						result = new Or(result, token);
					else
						throw new RuntimeException("Read.getFilter1() unknown token '" + andOr + "'");
				} else
					result = token;
			}
		}

		return result != null ? new Group(result) : null;
	}

	private void initFields() {
		String recordData = getRecordParameter();

		if(recordData == null || recordData.isEmpty())
			return;

		JsonObject record = new JsonObject(recordData);

		Query query = actionParameters().requestQuery;

		for(String fieldId : JsonObject.getNames(record)) {
			Field field = query.findFieldById(fieldId);

			if(field != null) {
				String text = record.getString(fieldId);

				if(text != null && !text.isEmpty()) {
					primary value = primary.create(field.type(), text);
					field.set(value);
				}
			} else
				Trace.logEvent("ReadAction.initFields(): field '" + fieldId + "' cannot be found.");
		}
	}

	private void initPeriod() {
		String json = getPeriodParameter();

		if(json != null && !json.isEmpty())
			getQuery().setPeriod(Period.parse(json));
	}

	private void initQuery() {
		String json = getGridParameter();

		if(json != null)
			getQuery().showAsGrid.set(new bool(json));
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

	private void writeCount(JsonWriter writer) throws SQLException {
		CountingSelect counter = counter();
		writer.writeProperty(Json.total, counter.count());
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

	private void writeGroupTotals(JsonWriter writer, Field groupField) throws SQLException {
		Select select = cursor();

		if(select.isGrouped()) {
			Select summary = new Select();
			summary.setSubselect(select);
			summary.setFields(select.getFields());
			summary.setGroupBy(ArrayUtils.collection(groupField));

			select = summary;
		} else
			select.setGroupBy(ArrayUtils.collection(groupField));

		Select frame = new FramedSelect(select, 1, 50);

		frame.aggregate();

		writer.startArray(Json.data);
		
		while(frame.next()) {
			writer.startObject();

			for(Field field : frame.getFields()) {
				if(field.aggregation != Aggregation.None && field.type() != FieldType.Boolean)
					field.writeData(writer);
			}

			writer.finishObject();
		}

		writer.finishArray();
	}

	private Groupping writeFrame(JsonWriter writer) throws SQLException {
		Query query = getQuery();

		Groupping groups = new Groupping(this.groupFields);

		boolean hasRecords = false;

		FramedSelect frame = frame();

		writer.startArray(Json.data);

		try {
			frame.saveState();

			frame.open();

			while(frame.next()) {
				writer.startObject();

				primary[] group = !hasRecords ? groups.firstGroup : groups.lastGroup;

				for(Field field : getSelectFields()) {
					int index = ArrayUtils.indexOf(groups.fields, field);

					if(index != -1)
						group[index] = field.get();

					field.writeData(writer);
				}

				hasRecords = true;

				Style style = query.renderRecord();

				if(style != null)
					style.write(writer);

				writer.finishObject();
			}
		} finally {
			frame.restoreState();
			frame.close();
		}

		writer.finishArray();

		return hasRecords && groups.fields.length != 0 ? groups : null;
	}

	private void writeTotals(JsonWriter writer) throws SQLException {
		AggregatingSelect totals = totals();

		try {
			totals.open();

			writer.startObject(Json.totalsData);

			while(totals.next()) {
				for(Field field : totals.getFields()) {
					if(field.aggregation != Aggregation.None && field.type() != FieldType.Boolean)
						field.writeData(writer);
				}
			}

			writer.finishObject();
		} finally {
			totals.close();
		}
	}

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

		Field expression = createAggregatedExpression(groupField, groupField.aggregation, groupField.type());
		Field count = createAggregatedExpression(groupField, Aggregation.Count, FieldType.Integer);

		if(expression != null && expression != groupField)
			select.addField(expression);

		if(count != groupField)
			select.addField(count);

		Select summary = new Select();
		summary.setSubselect(select);
		summary.setFields(select.getFields());
		summary.setGroupBy(ArrayUtils.collection(groupField));

		try {
			summary.aggregate();

			writer.startObject(Json.summaryData);

			while(summary.next()) {
				writer.startObject(JsonObject.quote(groupField.get().toString()));
				
				for(Field field : summary.getFields()) {
					if(field == groupField)
						writer.writeProperty(Json.groupValue, groupField.get());
					else if(field == expression)
						writer.writeProperty(JsonObject.quote(groupField.id()), expression.get());
					else if(field == count)
						writer.writeProperty(Json.total, count.get());
					else if(field.aggregation != Aggregation.None && field.type() != FieldType.Boolean)
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
		Query model = Query.getModel(query);

		Query rootQuery = getRootQuery();
		Field parentKey = rootQuery.parentKey();

		modelRecordId = getRecordIdParameter();
		parentId = parentKey != null ? getParentIdParameter() : null;

		query.beforeRead(parentId, model, modelRecordId);

		initializeFilters();
	}

	private void afterRead() {
		Query query = getQuery();
		Query model = Query.getModel(query);

		query.afterRead(parentId, model, modelRecordId);
	}

	@Override
	public void writeResponse(JsonWriter writer) throws Throwable {
		Field totalsBy = actionParameters().totalsBy;

		Query query = getQuery();
        query.setReadLock(ReadLock.None);

		try {
			if(totalsBy == null) {
				if(getStartParameter() != -1 || getLimitParameter() != -1)
					writeCount(writer);

				Groupping groups = writeFrame(writer);

				if(query.showTotals.get())
					writeTotals(writer);

				if(groups != null)
					writeSummary(writer, groups);
			} else
				writeGroupTotals(writer, totalsBy);
		} finally {
			afterRead();
		}
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
}
