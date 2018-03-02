package org.zenframework.z8.server.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.query.ReadLock;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.ILink;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.Sql;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.db.sql.SqlStringToken;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.expressions.And;
import org.zenframework.z8.server.engine.Database;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.types.primary;

public class Select {
	private static String SelectAlias = "S";
	private static String FieldAlias = "F";

	private Collection<Field> fields = new ArrayList<Field>();

	private Query rootQuery = null;
	private Collection<ILink> links = new ArrayList<ILink>();

	private Select select;

	private SqlToken where;
	private SqlToken having;

	private Collection<Field> groupBy = new ArrayList<Field>();
	private Collection<Field> orderBy = new ArrayList<Field>();

	private boolean isAggregated = false;

	private Database database;
	private Cursor cursor;

	private Collection<FieldState> fieldStates = new ArrayList<FieldState>();

	public Select() {
		this(null);
	}

	public Select(Select select) {
		if(select != null) {
			database = select.database;

			fields.addAll(select.fields);

			rootQuery = select.rootQuery;
			links = select.links;

			this.select = select.select;

			where = select.where;
			having = select.having;

			groupBy = select.groupBy;
			orderBy = select.orderBy;

			isAggregated = select.isAggregated;
		}
	}

	public Database database() {
		return ServerConfig.database();
	}

	public DatabaseVendor vendor() {
		return database().vendor();
	}

	public Collection<Field> getFields() {
		return fields;
	}

	public void setFields(Collection<Field> fields) {
		this.fields = fields == null ? new ArrayList<Field>() : fields;

		int position = 0;
		for(Field field : this.fields) {
			field.position = position;
			position++;
		}
	}

	public void addField(Field field) {
		field.position = fields.size();
		fields.add(field);
	}

	public void setRootQuery(Query query) {
		this.rootQuery = query;
	}

	public void setLinks(Collection<ILink> links) {
		this.links = links == null ? new ArrayList<ILink>() : links;
	}

	public void setSubselect(Select select) {
		this.select = select;
	}

	public void setWhere(SqlToken where) {
		this.where = where;
	}

	public void addWhere(SqlToken where) {
		this.where = this.where == null ? where : new And(this.where, where);
	}

	public SqlToken getHaving() {
		return having;
	}

	public void setHaving(SqlToken having) {
		this.having = having;
	}

	public Collection<Field> getGroupBy() {
		return groupBy;
	}

	public void setGroupBy(Collection<Field> groupBy) {
		this.groupBy = groupBy == null ? new ArrayList<Field>() : groupBy;
	}

	public Collection<Field> getOrderBy() {
		return orderBy;
	}

	public void setOrderBy(Collection<Field> orderBy) {
		this.orderBy = orderBy == null ? new ArrayList<Field>() : orderBy;
	}

	public boolean isGrouped() {
		return !groupBy.isEmpty() || isAggregated;
	}

	protected boolean isOrdered() {
		return !orderBy.isEmpty();
	}

	protected void setAggregated(boolean isAggregated) {
		this.isAggregated = isAggregated;
	}

	protected boolean isAggregated() {
		return isAggregated;
	}

	protected String sql(FormatOptions options) {
		String from = formatFrom(options);
		boolean isGrouped = isGrouped();

		if(!isGrouped)
			options.disableAggregation();

		String fields = formatFields(options);
		String orderBy = formatOrderBy(options);

		if(!isGrouped)
			options.enableAggregation();

		options.disableAggregation();

		String result = "select" + fields + from + formatWhere(options) + formatGroupBy(options);

		options.enableAggregation();
		result += formatHaving(options);

		options.disableAggregation();
		result += orderBy;

		result += formatReadLock(options);

		options.enableAggregation();

		updateAliases(options);

		return result;
	}

	private String formatReadLock(FormatOptions options) {
		String readLock = "";

		if(rootQuery == null || rootQuery.getReadLock() == ReadLock.None)
			return readLock;

		readLock = '\n' + rootQuery.getReadLock().toString();

		if(vendor() == DatabaseVendor.Postgres)
			readLock += " of " + rootQuery.getAlias();

		return readLock;
	}

	private Collection<Field> getAggregatedFields() {
		Collection<Field> result = new ArrayList<Field>();

		for(Field field : fields) {
			if(field.isAggregated() || groupBy.contains(field))
				result.add(field);
		}

		return result;
	}

	private void updateAliases(FormatOptions options) {
		int index = 0;
		for(Field field : fields) {
			options.setFieldAlias(field, getAlias() + "." + getFieldAlias(index));
			index++;
		}
	}

	private String getAlias() {
		return SelectAlias;
	}

	private String getFieldName(Field field, DatabaseVendor vendor, FormatOptions options) {
		return field.format(vendor, options);
	}

	private String getFieldAlias(int index) {
		return FieldAlias + (index + 1);
	}

	protected String formatField(Field field, int index, DatabaseVendor vendor, FormatOptions options) {
		boolean hasAlias = options.getFieldAlias(field) != null;

		options.disableAggregation();

		String result = new SqlField(field).format(vendor, options);

		if(!hasAlias)
			result = field.wrapForSelect(result, vendor);

		options.enableAggregation();

		return Sql.aggregate(new SqlStringToken(result, field.type()), field.aggregation).format(vendor, options, false) + " as " + getFieldAlias(index);
	}

	protected String formatFields(FormatOptions options) {
		if(fields.isEmpty())
			return "\n\tcount(0)" + " as " + getFieldAlias(0);

		DatabaseVendor vendor = vendor();
		String result = "";

		int index = 0;
		for(Field field : fields) {
			result += (result.isEmpty() ? "" : ", ") + "\n\t" + formatField(field, index, vendor, options);
			index++;
		}
		return result;
	}

	private String queryName(Query query) {
		Database database = database();
		DatabaseVendor vendor = vendor();
		return query != null ? database.tableName(query.name()) + (vendor == DatabaseVendor.SqlServer ? " as " : " ") + query.getAlias() : "";
	}

	private String emptyFrom() {
		switch(vendor()){
		case Postgres:
			return "";
		case Oracle:
			return " from dual";
		default:
			throw new UnsupportedOperationException();
		}
	}

	protected String formatFrom(FormatOptions options) {
		String join = "";

		String root = select != null ? "(" + select.sql(options) + ") " + select.getAlias() : null;

		for(ILink link : links) {
			String name = link.getQuery().name();

			if(name != null) {
				options.disableAggregation();
				join += "\n\t" + link.getJoin() + " join " + queryName(link.getQuery()) + " on " + link.on().format(vendor(), options, true);
				options.enableAggregation();
			}
		}

		if(root == null && rootQuery == null)
			return emptyFrom();

		return "\nfrom " + (root != null ? root : queryName(rootQuery)) + join;
	}

	private String formatWhere(FormatOptions options) {
		if(where == null)
			return "";

		return "\n" + "where" + "\n\t" + where.format(vendor(), options, true);
	}

	private String formatOrderBy(FormatOptions options) {
		String result = "";

		for(Field field : orderBy)
			result += (result.isEmpty() ? "" : ", ") + new SqlField(field).format(vendor(), options) + " " + field.sortDirection;

		return result.isEmpty() ? "" : ("\norder by\n\t" + result);
	}

	private String formatGroupBy(FormatOptions options) {
		String result = "";

		for(Field field : groupBy)
			result += (result.isEmpty() ? "" : ", ") + getFieldName(field, vendor(), options);

		return result.isEmpty() ? "" : ("\ngroup by\n\t" + result);
	}

	protected String formatHaving(FormatOptions options) {
		if(having == null)
			return "";

		return "\n" + "having" + "\n\t" + having.format(vendor(), options, true);
	}

	public void aggregate() {
		isAggregated = true;

		setFields(getAggregatedFields());

		open();
	}

	public void open() {
		String sql = sql(new FormatOptions());

		boolean traceSql = ServerConfig.traceSql();
		long startAt = traceSql ? System.currentTimeMillis() : 0;

		try {
			cursor = BasicSelect.cursor(sql);
		} catch(Throwable e) {
			close();
			Trace.logError(sql, e);
			throw new RuntimeException(e);
		}

		if(traceSql)
			Trace.logEvent(sql + "\n" + "Execution time: " + (System.currentTimeMillis() - startAt) + " ms\n");

		activate();
	}

	public void close() {
		if(cursor != null) {
			deactivate();
			cursor.close();
		}
	}

	private class FieldState {
		private Field field;
		private boolean changed;
		private primary value;
		private int position;

		public FieldState(Field field) {
			this.field = field;
			this.position = field.position;

			changed = field.changed();

			if(changed) {
				this.value = field.get();
				field.reset();
			}

			field.setCursor(null);
		}

		public void restore() {
			field.position = position;
			if(changed)
				field.set(value);
		}
	}

	public void saveState() {
		for(Field field : fields)
			fieldStates.add(new FieldState(field));
	}

	public void restoreState() {
		for(FieldState state : fieldStates)
			state.restore();

		fieldStates.clear();

		activate();
	}

	private void activate() {
		for(Field field : fields)
			field.setCursor(this);
	}

	private void deactivate() {
		for(Field field : fields)
			field.setCursor(null);
	}

	protected Cursor getCursor() {
		return cursor;
	}

	public boolean next() {
		try {
			return getCursor().next();
		} catch(SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean isAfterLast() {
		try {
			return getCursor().isAfterLast();
		} catch(SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean isClosed() {
		return cursor == null;
	}

	public primary get(Field field) throws SQLException {
		return cursor.get(field);
	}

	public primary get(Field field, FieldType type) throws SQLException {
		return cursor.get(field, type);
	}
}
