package org.zenframework.z8.server.base.table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.Expression;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.generator.IForeignKey;
import org.zenframework.z8.server.db.generator.Index;
import org.zenframework.z8.server.db.sql.SqlConst;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.db.sql.SqlFieldList;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.primary;

public class TableBase extends Query implements ITable {
	public static class CLASS<T extends TableBase> extends Query.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(TableBase.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new TableBase(container);
		}
	}

	private List<Map<IField, primary>> staticRecords = new ArrayList<Map<IField, primary>>();

	public TableBase(IObject container) {
		super(container);
	}

	@Override
	public Collection<Map<IField, primary>> getStaticRecords() {
		initStaticRecords();
		return staticRecords;
	}

	@Override
	public Collection<IForeignKey> getForeignKeys() {
		LinkedHashSet<IForeignKey> foreignKeys = new LinkedHashSet<IForeignKey>();

		for (OBJECT.CLASS<? extends OBJECT> link : getLinks()) {
			if (link.instanceOf(IForeignKey.class) && link.foreignKey())
				foreignKeys.add((IForeignKey) link.get());
		}

		return foreignKeys;
	}

	@Override
	public Collection<Index> getIndexes() {
		List<Index> result = new ArrayList<Index>();

		for (Field field : getDataFields()) {
			if (!field.indexed() && !field.unique())
				continue;

			if (field.isExpression()) {
				Expression expression = (Expression) field;
				Collection<Field> usedFields = expression.getUsedFields();
				if (!usedFields.isEmpty())
					result.add(new Index(name(), field, new SqlFieldList(usedFields), result.size()));
				SqlToken token = expression.expression();
				if (token != null && !(token instanceof SqlConst))
					result.add(new Index(name(), field, token, result.size()));
			} else
				result.add(new Index(name(), field, new SqlField(field), result.size()));
		}

		return result;
	}

	@Override
	public void initStaticRecords() {}

	public void addRecord(guid key, Map<IField, primary> values) {
		for(Map<IField, primary> record : staticRecords) {
			if(key.equals(record.get(primaryKey())))
				return;
		}
		values.put(primaryKey(), key);
		staticRecords.add(values);
	}

	final protected void internalAddRecord(guid key, Map<IField, primary> values) {
		addRecord(key, values);
	}
}
