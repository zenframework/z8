package org.zenframework.z8.server.base.table.value;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.ITable;
import org.zenframework.z8.server.types.sql.sql_bool;

public interface ILink extends IField {
	public Query.CLASS<Query> query();
	public Query getQuery();

	public ITable getReferencedTable();
	public IField getReferer();

	public Join getJoin();
	public sql_bool on();
}
