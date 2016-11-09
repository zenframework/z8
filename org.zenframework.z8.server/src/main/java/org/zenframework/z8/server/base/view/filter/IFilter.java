package org.zenframework.z8.server.base.view.filter;

import org.zenframework.z8.server.db.sql.SqlToken;

public interface IFilter {
	public SqlToken where();
}
