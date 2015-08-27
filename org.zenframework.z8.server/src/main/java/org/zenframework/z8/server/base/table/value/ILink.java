package org.zenframework.z8.server.base.table.value;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.ITable;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.sql.sql_guid;

public interface ILink extends IObject {
    
    public Query.CLASS<Query> query();
    public Query getQuery();
    public Query getOwner();
    
    public ITable getReferencedTable();
    public IField getReferer();

    public Join getJoin();

    public sql_guid sql_guid();
}
