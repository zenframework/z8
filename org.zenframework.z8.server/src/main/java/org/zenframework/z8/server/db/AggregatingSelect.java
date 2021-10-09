package org.zenframework.z8.server.db;

import java.util.ArrayList;
import java.util.Collection;

import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.db.sql.FormatOptions;

public class AggregatingSelect extends Select {
	public AggregatingSelect() {
		super();
	}

	public AggregatingSelect(Select select) {
		super(select);
	}

	@Override
	protected String getSql(FormatOptions options) {
		setOrderBy(null);

		if(isGrouped()) {
			setSubselect(new Select(this));

			setFields(getAggregatedFields());
			setLinks(null);
			setRootQuery(null);
			setWhere(null);
			setHaving(null);

			setGroupBy(null);
			setOrderBy(null);
		}

		setAggregated(true);

		return super.getSql(options);
	}

	private Collection<Field> getAggregatedFields() {
		Collection<Field> result = new ArrayList<Field>();

		for(Field field : getFields()) {
			if(field.isAggregated())
				result.add(field);
		}

		return result;
	}
}