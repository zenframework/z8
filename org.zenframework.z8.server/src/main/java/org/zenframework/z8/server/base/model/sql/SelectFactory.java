package org.zenframework.z8.server.base.model.sql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

import org.zenframework.z8.server.base.model.actions.ReadAction;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.Aggregation;
import org.zenframework.z8.server.base.table.value.Expression;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.ILink;
import org.zenframework.z8.server.base.table.value.Link;
import org.zenframework.z8.server.db.sql.SqlToken;

public class SelectFactory {
	private ReadAction action;

	private Collection<Link> aggregateBy = null;

	private List<Field> selectFields;
	private List<Field> groupedSelectFields;

	private boolean isCounter = false;
	private boolean isFramed = false;
	private boolean isAggregated = false;

	static public SelectFactory create(ReadAction action) {
		return new SelectFactory(action);
	}

	public SelectFactory(ReadAction action) {
		this.action = action;
		aggregateBy = action.getAggregateByFields();
	}

	public Select cursor() {
		return create();
	}

	public CountingSelect count() {
		isCounter = true;
		return (CountingSelect)create();
	}

	public AggregatingSelect aggregate() {
		isAggregated = true;
		return (AggregatingSelect)create();
	}

	public FramedSelect frame() {
		isFramed = true;
		return (FramedSelect)create();
	}

	private int getStart() {
		int total = action.getTotalCount();
		int start = Math.max(action.getStart() + 1, 1);
		return start > total ? 1 : start;
	}

	private int getLimit() {
		return action.getLimit();
	}

	private Collection<Field> getSortFields() {
		return action.getSortFields();
	}

	private Collection<Field> getGroupFields() {
		return action.getGroupByFields();
	}

	private boolean isGrouped() {
		return !getGroupFields().isEmpty();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Select create() {
		Select subselect = null;
		groupedSelectFields = null;

		boolean aggregatedRoot = !aggregateBy.isEmpty();

		if(aggregatedRoot) {
			groupedSelectFields = getAggregatedFieldsFromRoot();

			Collection<Query> from = new LinkedHashSet<Query>();
			from.addAll(action.getQueries(groupedSelectFields));
			from.addAll(action.getFilterQueries());

			Collection<ILink> links = getLinks(from, true);
			SqlToken filter = action.getFilter();

			subselect = new Select();
			subselect.setFields(groupedSelectFields);
			subselect.setRootQuery(action.getRootQuery());
			subselect.setLinks(links);
			subselect.setWhere(filter);
			subselect.setGroupBy((Collection)aggregateBy);

			// if(isCounter && !isGrouped())
			// {
			// return new CountingSelect(subselect);
			// }
		}

		selectFields = getSelectFields();
		Collection<Query> usedQueries = new LinkedHashSet<Query>();
		usedQueries.addAll(action.getQueries(selectFields));
		usedQueries.addAll(action.getQueries(getGroupFields()));
		usedQueries.addAll(action.getQueries(getSortFields()));
		usedQueries.addAll(action.getGroupFilterQueries());

		if(aggregateBy.isEmpty())
			usedQueries.addAll(action.getFilterQueries());
		else
			usedQueries.remove(action.getRootQuery());

		usedQueries = filterUsedQueries(usedQueries);

		Collection<ILink> links = getLinks(usedQueries, false);
		SqlToken filter = aggregatedRoot ? (!isGrouped() ? action.getGroupFilter() : null) : action.getFilter();
		SqlToken groupFilter = isGrouped() ? action.getGroupFilter() : null;

		Select result = new Select();
		result.setFields(selectFields);
		result.setRootQuery(action.getRootQuery());
		result.setLinks(links);
		result.setSubselect(subselect);
		result.setWhere(filter);
		result.setGroupBy(getGroupFields());
		result.setHaving(groupFilter);

		if(!isCounter)
			return new CountingSelect(result);

		result.setOrderBy(getSortFields());
		return isFramed ? new FramedSelect(result, getStart(), getLimit()) : isAggregated ? new AggregatingSelect(result) : result;
	}

	private List<Field> getAggregatedFieldsFromRoot() {
		assert (!aggregateBy.isEmpty());

		List<Field> result = new ArrayList<Field>();
		result.addAll(aggregateBy);

		for(Field field : action.getSelectFields()) {
			if(field.owner() == action.getRootQuery() || field.owner() == action.getQuery()) {
				if(field.getAggregation() != Aggregation.None)
					result.add(field);
			}
		}

		return result;
	}

	private boolean checkField(Field field) {
		if(aggregateBy.isEmpty() || groupedSelectFields.contains(field) || field instanceof Expression)
			return true;

		for(Link link : aggregateBy) {
			if(!link.owner().getPath(field).isEmpty())
				return true;
		}

		return false;
	}

	private List<Field> getSelectFields() {
		List<Field> fields = new ArrayList<Field>();

		Collection<Field> groupFields = getGroupFields();
		boolean grouped = isGrouped();

		for(Field field : action.getSelectFields()) {
			if(checkField(field)) {
				if(grouped) {
					if(groupFields.contains(field) || field.isAggregated())
						fields.add(field);
				} else if(isAggregated) {
					if(field.isAggregated())
						fields.add(field);
				} else
					fields.add(field);
			}
		}

		return fields;
	}

	private Collection<Query> filterUsedQueries(Collection<Query> queries) {
		if(aggregateBy.isEmpty())
			return queries;

		Collection<Query> result = new HashSet<Query>();

		for(Link link : aggregateBy) {
			Query linkQuery = link.getQuery();

			for(Query query : queries) {
				if(linkQuery == query || !linkQuery.getPath(query).isEmpty())
					result.add(query);
			}
		}

		return result;
	}

	public Collection<ILink> getLinks(Collection<Query> queries, boolean innerSelect) {
		if(innerSelect || aggregateBy.isEmpty())
			return action.getLinks(queries);

		Collection<ILink> links = new HashSet<ILink>();

		for(ILink link : aggregateBy) {
			Query linkQuery = link.getQuery();

			for(Query query : queries) {
				Collection<ILink> path = linkQuery.getPath(query);
				if(linkQuery == query || !path.isEmpty()) {
					links.addAll(path);
					links.add(link);
				}
			}
		}

		return links;
	}
}
