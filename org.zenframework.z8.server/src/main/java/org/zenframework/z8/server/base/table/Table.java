package org.zenframework.z8.server.base.table;

import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.base.table.value.Aggregation;
import org.zenframework.z8.server.base.table.value.BoolField;
import org.zenframework.z8.server.base.table.value.DatetimeField;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.GuidField;
import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.base.table.value.IntegerField;
import org.zenframework.z8.server.base.table.value.StringField;
import org.zenframework.z8.server.base.table.value.TextField;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.primary;

public class Table extends TableBase {
	static public class fieldNames {
		public final static String RecordId = "RecordId";
		public final static String CreatedAt = "CreatedAt";
		public final static String CreatedBy = "CreatedBy";
		public final static String ModifiedAt = "ModifiedAt";
		public final static String ModifiedBy = "ModifiedBy";

		public final static String Id = "Id";
		public final static String Name = "Name";
		public final static String ShortName = "Short name";
		public final static String Description = "Description";
		public final static String Lock = "Lock";
		public final static String Archive = "Archive";
	}

	static public class strings {
		public final static String Id = "Table.id";
		public final static String Description = "Table.description";
	}

	static public class displayNames {
		public final static String Id = Resources.get(strings.Id);
		public final static String Description = Resources.get(strings.Description);
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

	public GuidField.CLASS<GuidField> recordId = new GuidField.CLASS<GuidField>(this);

	public DatetimeField.CLASS<? extends DatetimeField> createdAt = new DatetimeField.CLASS<DatetimeField>(this);
	public DatetimeField.CLASS<? extends DatetimeField> modifiedAt = new DatetimeField.CLASS<DatetimeField>(this);

	public GuidField.CLASS<? extends GuidField> createdBy = new GuidField.CLASS<GuidField>(this);
	public GuidField.CLASS<? extends GuidField> modifiedBy = new GuidField.CLASS<GuidField>(this);

	public IntegerField.CLASS<? extends IntegerField> id = new IntegerField.CLASS<IntegerField>(this);
	public StringField.CLASS<? extends StringField> name = new StringField.CLASS<StringField>(this);
	public StringField.CLASS<? extends StringField> shortName = new StringField.CLASS<StringField>(this);
	public TextField.CLASS<? extends StringField> description = new TextField.CLASS<TextField>(this);

	public IntegerField.CLASS<? extends IntegerField> lock = new IntegerField.CLASS<IntegerField>(this);
	public BoolField.CLASS<? extends BoolField> archive = new BoolField.CLASS<BoolField>(this);

	final static public int ShortNameLength = 15;
	final static public int NameLength = 50;

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
		objects.add(archive);

		objects.add(id);
		objects.add(name);
		objects.add(shortName);
		objects.add(description);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		id.setName(fieldNames.Id);
		id.setIndex("id");
		id.setDisplayName(displayNames.Id);
		id.get().aggregation = Aggregation.None;

		name.setName(fieldNames.Name);
		name.setIndex("name");
		name.get().length = new integer(NameLength);

		shortName.setName(fieldNames.ShortName);
		shortName.setIndex("shortName");
		shortName.get().length = new integer(ShortNameLength);

		description.setName(fieldNames.Description);
		description.setIndex("description");
		description.setDisplayName(displayNames.Description);

		lock.setName(fieldNames.Lock);
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

		archive.setName(fieldNames.Archive);
		archive.setIndex("archive");
		archive.setSystem(true);
	}

	@Override
	public void constructor() {
		super.constructor();

		String displayName = name.displayName();
		if(displayName == null || displayName.isEmpty())
			name.setDisplayName(displayName());
	}

	@Override
	public Field primaryKey() {
		return recordId.get();
	}

	@Override
	public Field lockKey() {
		return lock.get();
	}

	@Override
	public void beforeCreate(guid recordId, guid parentId) {
		createdAt.get().set(new date());
		createdBy.get().set(ApplicationServer.getUser().id());

		super.beforeCreate(recordId, parentId);
	}

	@Override
	public void beforeUpdate(guid recordId) {
		modifiedAt.get().set(new date());
		modifiedBy.get().set(ApplicationServer.getUser().id());

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

		for(Map<IField, primary> record : getStaticRecords()) {
			for(primary value : record.values())
				result += Math.abs(value.hashCode());
		}

		return Math.abs(Integer.toString(result).hashCode());
	}
}
