package org.zenframework.z8.server.db.sql;

import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.base.table.value.IValue;

public class FormatOptions {
	private int enablesCount = 0;

	private Map<IValue, String> aliases = new HashMap<IValue, String>();

	public String getFieldAlias(IValue field) {
		return aliases.get(field);
	}

	public void setFieldAlias(IValue field, String alias) {
		aliases.put(field, alias);
	}

	public boolean isAggregationEnabled() {
		return enablesCount == 0;
	}

	public void enableAggregation() {
		if(enablesCount > 0)
			enablesCount--;
	}

	public void disableAggregation() {
		enablesCount++;
	}
}
