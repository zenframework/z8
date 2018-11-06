package org.zenframework.z8.server.db.sql;

import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.base.table.value.IField;

public class FormatOptions {
	private int aggregationEnabled = 0;
	private boolean orderBy = false;

	private Map<IField, String> aliases = new HashMap<IField, String>();

	public String getFieldAlias(IField field) {
		return aliases.get(field);
	}

	public void setFieldAlias(IField field, String alias) {
		aliases.put(field, alias);
	}

	public boolean isAggregationEnabled() {
		return aggregationEnabled == 0;
	}

	public void enableAggregation() {
		if(aggregationEnabled > 0)
			aggregationEnabled--;
	}

	public void disableAggregation() {
		aggregationEnabled++;
	}

	public void setOrderBy(boolean orderBy) {
		this.orderBy = orderBy;
	}

	public boolean isOrderBy() {
		return orderBy;
	}
}
