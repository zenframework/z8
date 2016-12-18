package org.zenframework.z8.server.base.table;

import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.base.table.value.BoolField;
import org.zenframework.z8.server.base.table.value.DatetimeField;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.GuidField;
import org.zenframework.z8.server.base.table.value.IntegerField;
import org.zenframework.z8.server.base.table.value.StringField;
import org.zenframework.z8.server.base.table.value.TextField;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;

public class Table extends TableBase {
	static public class names {
		public final static String RecordId = "RecordId";
		public final static String CreatedAt = "CreatedAt";
		public final static String CreatedBy = "CreatedBy";
		public final static String ModifiedAt = "ModifiedAt";
		public final static String ModifiedBy = "ModifiedBy";

		public final static String Id = "Id";
		public final static String Name = "Name";
		public final static String Description = "Description";
		public final static String Lock = "Lock";
		public final static String Archive = "Archive";
	}

	static public class strings {
		public final static String RecordId = "Table.recordId";
		public final static String CreatedAt = "Table.createdAt";
		public final static String CreatedBy = "Table.createdBy";
		public final static String ModifiedAt = "Table.modifiedAt";
		public final static String ModifiedBy = "Table.modifiedBy";

		public final static String Id = "Table.id";
		public final static String Name = "Table.name";
		public final static String Description = "Table.description";
	}

	static public class displayNames {
		public final static String RecordId = Resources.get(strings.RecordId);
		public final static String CreatedAt = Resources.get(strings.CreatedAt);
		public final static String CreatedBy = Resources.get(strings.CreatedBy);
		public final static String ModifiedAt = Resources.get(strings.ModifiedAt);
		public final static String ModifiedBy = Resources.get(strings.ModifiedBy);

		public final static String Id = Resources.get(strings.Id);
		public final static String Name = Resources.get(strings.Name);
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

	public StringField.CLASS<? extends StringField> id = new StringField.CLASS<StringField>(this);
	public StringField.CLASS<? extends StringField> name = new StringField.CLASS<StringField>(this);
	public TextField.CLASS<? extends StringField> description = new TextField.CLASS<TextField>(this);

	public IntegerField.CLASS<? extends IntegerField> lock = new IntegerField.CLASS<IntegerField>(this);
	public BoolField.CLASS<? extends BoolField> archive = new BoolField.CLASS<BoolField>(this);

	final static public int IdLength = 15;
	final static public int NameLength = 50;

	public Table(IObject container) {
		super(container);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		id.setName(names.Id);
		id.setIndex("id");
		id.setDisplayName(displayNames.Id);
		id.get().length = new integer(IdLength);

		name.setName(names.Name);
		name.setIndex("name");
		name.setDisplayName(displayNames.Name);
		name.get().length = new integer(NameLength);

		description.setName(names.Description);
		description.setIndex("description");
		description.setDisplayName(displayNames.Description);

		lock.setName(names.Lock);
		lock.setIndex("lock");
		lock.setSystem(true);

		recordId.setName(names.RecordId);
		recordId.setIndex("recordId");
		recordId.setDisplayName(displayNames.RecordId);
		recordId.setAttribute(PrimaryKey, "");
		recordId.setSystem(true);

		createdAt.setName(names.CreatedAt);
		createdAt.setIndex("createdAt");
		createdAt.setDisplayName(displayNames.CreatedAt);
		createdAt.setSystem(true);

		createdBy.setName(names.CreatedBy);
		createdBy.setIndex("createdBy");
		createdBy.setDisplayName(displayNames.CreatedBy);
		createdBy.setSystem(true);

		modifiedAt.setName(names.ModifiedAt);
		modifiedAt.setIndex("modifiedAt");
		modifiedAt.setDisplayName(displayNames.ModifiedAt);
		modifiedAt.setSystem(true);

		modifiedBy.setName(names.ModifiedBy);
		modifiedBy.setIndex("modifiedBy");
		modifiedBy.setDisplayName(displayNames.ModifiedBy);
		modifiedBy.setSystem(true);

		archive.setName(names.Archive);
		archive.setIndex("archive");
		archive.setSystem(true);

		registerDataField(recordId);
		registerDataField(createdAt);
		registerDataField(modifiedAt);
		registerDataField(createdBy);
		registerDataField(modifiedBy);
		registerDataField(lock);
		registerDataField(archive);

		registerDataField(id);
		registerDataField(name);
		registerDataField(description);
	}

	@Override
	public Field primaryKey() {
		return recordId.get();
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

	public Map<guid, Field.CLASS<? extends Field>> getFieldsMap() {
		Map<guid, Field.CLASS<? extends Field>> map = new HashMap<guid, Field.CLASS<? extends Field>>();

		for(Field.CLASS<? extends Field> field : primaryFields())
			map.put(field.key(), field);

		return map;
	}

	public int controlSum() {
		int result = 0;

		for(Field field : getPrimaryFields())
			result += field.controlSum();

		return Math.abs(result);
	}
}
