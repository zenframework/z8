package org.zenframework.z8.server.db.sql;

import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.base.table.value.IField;

public class FormatOptions {
	private int enablesCount = 0;

	private Map<IField, String> aliases = new HashMap<IField, String>();

	public String getFieldAlias(IField field) {
		return aliases.get(field);
	}

	public void setFieldAlias(IField field, String alias) {
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
