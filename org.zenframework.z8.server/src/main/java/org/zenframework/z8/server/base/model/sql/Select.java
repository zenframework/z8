package org.zenframework.z8.server.base.model.sql;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.Aggregation;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.GuidField;
import org.zenframework.z8.server.base.table.value.ILink;
import org.zenframework.z8.server.base.table.value.IValue;
import org.zenframework.z8.server.base.table.value.Link;
import org.zenframework.z8.server.db.BasicSelect;
import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.db.Cursor;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.expressions.And;
import org.zenframework.z8.server.db.sql.expressions.Group;
import org.zenframework.z8.server.db.sql.expressions.Operation;
import org.zenframework.z8.server.db.sql.expressions.Rel;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.engine.Database;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.sql.sql_bool;
import org.zenframework.z8.server.utils.ArrayUtils;

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

    private Map<Field, primary> changedFields = new HashMap<Field, primary>();

    public Select() {
        this(null);
    }

    public Select(Select select) {
        if(select != null) {
            database = select.database;
            
            fields = select.fields;

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

    public void setDatabase(Database database) {
        this.database = database;
    }

    public Database database() {
        if(database == null)
            database = ApplicationServer.database();
        
        return database;
    }

    public DatabaseVendor vendor() {
        return database().vendor();
    }

    public void setFields(Collection<Field> fields) {
        this.fields = fields == null ? new ArrayList<Field>() : fields;
    }

    public void addField(Field field) {
        if(isGrouped())
            field.aggregate(isAggregated);

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
        this.isAggregated = isAggregated ;
    }

    protected boolean isAggregated() {
        return isAggregated;
    }
    
    protected String sql(FormatOptions options) {
        String from = formatFrom(options);
        
        if(isGrouped()) {
            aggregateFields(fields);
        }

        String fields = formatFields(options);

        disaggregateFields(this.fields);

        String result = "select" + fields + from + formatWhere(options) + formatGroupBy(options)
                + formatHaving(options);

        if(isGrouped()) {
            aggregateFields(orderBy);
        }

        result += formatOrderBy(options);

        disaggregateFields(this.fields);

        updateAliases(options);

        return result;
    }

    private Collection<Field> getAggregatedFields() {
        Collection<Field> result = new ArrayList<Field>();

        for(Field field : fields) {
            if(field.aggregation != Aggregation.None || (isAggregated && field.totals != Aggregation.None)
                    || groupBy.contains(field)) {
                result.add(field);
            }
        }

        return result;
    }

    protected void aggregateFields(Collection<Field> fields) {
        for(Field field : fields) {
            field.aggregate(isAggregated);
        }
    }

    protected void disaggregateFields(Collection<Field> fields) {
        for(Field field : fields) {
            field.disaggregate();
        }
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

    private String aggregate(Field field, DatabaseVendor vendor, FormatOptions options) {
        return new SqlField(field).format(vendor, options);
    }

    protected String formatField(Field field, int index, DatabaseVendor vendor, FormatOptions options) {
        return aggregate(field, vendor, options) + " as " + getFieldAlias(index);
    }

    protected String formatFields(FormatOptions options) {
        if(fields.isEmpty()) {
            return "\n\tcount(0)" + " as " + getFieldAlias(0);
        }

        String result = "";

        int index = 0;
        for(Field field : fields) {
            result += (result.isEmpty() ? "" : ", ") + "\n\t" + formatField(field, index, vendor(), options);
            index++;
        }
        return result;
    }

    private String queryName(Query query) {
        Database database = database();
        DatabaseVendor vendor = vendor();
        return query != null ? database.tableName(query.name()) + (vendor == DatabaseVendor.SqlServer ? " as " : " ") + query.getAlias() : "";
    }
    
    protected String formatFrom(FormatOptions options) {
        String join = "";
        
        for(ILink link : links) {
            String name = link.getQuery().name();
            
            if(name != null) {
                Field field = (Field)link;
                
                sql_bool joinOn = link instanceof Link ? ((Link)link).joinOn : null;
                
                Query query = link.getQuery().getRootQuery();
                GuidField primaryKey = (GuidField) query.primaryKey();
                SqlToken token = new Rel(link.sql_guid(), Operation.Eq, primaryKey.sql_guid());
                if(joinOn != null)
                    token = new And(token, new Group(joinOn));

                boolean aggregated = field.isAggregated();
                field.setAggregated(false);
                
                join += "\n\t" + link.getJoin() + " join " + queryName(query) + " on " + token.format(vendor(), options, true);

                field.setAggregated(aggregated);
            }
        }
        
        String result = queryName(rootQuery) + join;

        if(select != null) {
            result += (result.isEmpty() ? "" : ", ") + "(" + select.sql(options) + ") " + select.getAlias();
        }

        return "\nfrom " + result;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected String formatWhere(FormatOptions options) {
        if(where == null)
            return "";

        disaggregateFields((Collection)where.getUsedFields());
        return "\n" + "where" + "\n\t" + where.format(vendor(), options, true);
    }

    protected String formatOrderBy(FormatOptions options) {
        assert (orderBy != null);

        String result = "";

        for(Field field : orderBy) {
            result += (result.isEmpty() ? "" : ", ") + aggregate(field, vendor(), options) + " " + field.sortDirection;
        }

        return result.isEmpty() ? "" : ("\norder by\n\t" + result);
    }

    protected String formatGroupBy(FormatOptions options) {
        String result = "";

        for(Field field : groupBy) {
            result += (result.isEmpty() ? "" : ", ") + getFieldName(field, vendor(), options);
        }

        return result.isEmpty() ? "" : ("\ngroup by\n\t" + result);
    }

    protected String formatHaving(FormatOptions options) {
        if(having == null) {
            return "";
        }

        for(IValue value : having.getUsedFields()) {
            Field field = (Field)value;
            field.aggregate();
        }

        return "\n" + "having" + "\n\t" + having.format(vendor(), options, true);
    }

    public void aggregate() {
        isAggregated = true;

        fields = getAggregatedFields();

        open();
    }

    public void open() {
        Connection connection = ConnectionManager.get(database);

        String sql = sql(new FormatOptions());

        /*		System.out.println("\n\n");
        		System.out.println(sql);
        */
        try {
            cursor = BasicSelect.cursor(connection, sql);
        } catch(Throwable e) {
            close();
            System.out.println(e.getMessage());
            System.out.println(sql);
            throw new RuntimeException(e);
        }

        activate();
    }

    public void close() {
        if(cursor != null) {
            cursor.close();
        }
    }

    public void saveState() {
        for(Field field : fields) {
            if(field.changed()) {
                changedFields.put(field, field.get());
            }

            field.setCursor(null);
            field.reset();
        }
    }

    public void restoreState() {
        activate();

        for(Field field : fields) {
            if(changedFields.containsKey(field)) {
                field.set(changedFields.get(field));
            }
        }

        changedFields.clear();
    }

    private void activate() {
        for(Field field : fields) {
            field.setCursor(this);
        }

        changedFields.clear();
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
        return get(getFieldPosition(field), field.type());
    }

    private int getFieldPosition(Field field) {
        int index = ArrayUtils.indexOf(fields.toArray(new Field[0]), field);
        assert (index != -1);
        return index + 1;
    }

    protected primary get(int index, FieldType fieldType) throws SQLException {
        return getCursor().get(index, fieldType);
    }

    public Collection<Field> getFields() {
        return fields;
    }
}
