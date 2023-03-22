package org.zenframework.z8.server.base.table;

import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.base.table.value.DatetimeField;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.GuidField;
import org.zenframework.z8.server.base.table.value.IntegerField;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.engine.Runtime;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.guid;

public class Table extends TableBase {
	static public class fieldNames {
		public final static String RecordId = "RecordId";
		public final static String CreatedAt = "CreatedAt";
		public final static String CreatedBy = "CreatedBy";
		public final static String ModifiedAt = "ModifiedAt";
		public final static String ModifiedBy = "ModifiedBy";

		public final static String Lock = "Lock";
	}

	public static class CLASS<T extends Table> extends TableBase.CLASS<T> {

		public CLASS(IObject container) {
			super(container);
			setJavaClass(Table.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new Table(container);
		}
	}

	public GuidField.CLASS<? extends GuidField> recordId = new GuidField.CLASS<GuidField>(this);

	public DatetimeField.CLASS<? extends DatetimeField> createdAt = new DatetimeField.CLASS<DatetimeField>(this);
	public DatetimeField.CLASS<? extends DatetimeField> modifiedAt = new DatetimeField.CLASS<DatetimeField>(this);

	public GuidField.CLASS<? extends GuidField> createdBy = new GuidField.CLASS<GuidField>(this);
	public GuidField.CLASS<? extends GuidField> modifiedBy = new GuidField.CLASS<GuidField>(this);

	public IntegerField.CLASS<? extends IntegerField> lock = new IntegerField.CLASS<IntegerField>(this);

	public Table(IObject container) {
		super(container);
	}

	@Override
	public void initMembers() {
		super.initMembers();

		objects.add(recordId);
		objects.add(createdAt);
		objects.add(modifiedAt);
		objects.add(createdBy);
		objects.add(modifiedBy);
		objects.add(lock);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		lock.setName(fieldNames.Lock);
		lock.setAttribute(LockKey, "");
		lock.setIndex("lock");
		lock.setSystem(true);

		recordId.setName(fieldNames.RecordId);
		recordId.setIndex("recordId");
		recordId.setAttribute(PrimaryKey, "");
		recordId.setSystem(true);

		createdAt.setName(fieldNames.CreatedAt);
		createdAt.setIndex("createdAt");
		createdAt.setSystem(true);

		createdBy.setName(fieldNames.CreatedBy);
		createdBy.setIndex("createdBy");
		createdBy.setSystem(true);

		modifiedAt.setName(fieldNames.ModifiedAt);
		modifiedAt.setIndex("modifiedAt");
		modifiedAt.setSystem(true);

		modifiedBy.setName(fieldNames.ModifiedBy);
		modifiedBy.setIndex("modifiedBy");
		modifiedBy.setSystem(true);
	}

	@Override
	public Field primaryKey() {
		return recordId.get();
	}

	@Override
	public void beforeCreate(guid recordId) {
		if(ApplicationServer.systemEventsEnabled()) {
			createdAt.get().set(new date());
			createdBy.get().set(ApplicationServer.getUser().id());
		}

		super.beforeCreate(recordId);
	}

	@Override
	public void beforeUpdate(guid recordId) {
		if(ApplicationServer.systemEventsEnabled()) {
			modifiedAt.get().set(new date());
			modifiedBy.get().set(ApplicationServer.getUser().id());
		}

		super.beforeUpdate(recordId);
	}

	public Map<guid, Field> getFieldsMap() {
		Map<guid, Field> map = new HashMap<guid, Field>();

		for(Field field : getPrimaryFields())
			map.put(field.key(), field);

		return map;
	}

	@Override
	public int controlSum() {
		int result = 0;

		for(Field field : getPrimaryFields())
			result += field.controlSum();
/*
		for(Map<IField, primary> record : getStaticRecords()) {
			for(primary value : record.values())
				result += Math.abs(value.hashCode());
		}
*/
		return Math.abs(Integer.toString(result).hashCode());
	}

	@Override
	public int priority() {
		return priority != null ? priority.getInt() : Runtime.modelGraph().getTablePriority(this);
	}

}
