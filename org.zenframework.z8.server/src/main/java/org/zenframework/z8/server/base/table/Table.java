package org.zenframework.z8.server.base.table;

import java.util.Collection;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.BoolField;
import org.zenframework.z8.server.base.table.value.DatetimeField;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.GuidField;
import org.zenframework.z8.server.base.table.value.StringField;
import org.zenframework.z8.server.base.table.value.TextField;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.bool;
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
		public final static String Locked = "Locked";
		public final static String Archive = "Archive";
		public final static String FullText = "Full text";
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
		public final static String Locked = "Table.locked";
		public final static String Archive = "Table.archive";
		public final static String FullText = "Table.fullText";
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
		public final static String Locked = Resources.get(strings.Locked);
		public final static String Archive = Resources.get(strings.Archive);
		public final static String FullText = Resources.get(strings.FullText);
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

	public BoolField.CLASS<? extends BoolField> locked = new BoolField.CLASS<BoolField>(this);
	public BoolField.CLASS<? extends BoolField> archive = new BoolField.CLASS<BoolField>(this);
	public StringField.CLASS<? extends StringField> fullText = new StringField.CLASS<StringField>(this);

	final static public int IdLength = 15;
	final static public int NameLength = 50;
	final static public int NameWidth = 20;
	final static public int FullTextLength = 8192;
	final static public int FullTextWidth = 128;

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
		id.get().width = new integer(IdLength);
		id.get().stretch = new bool(false);

		name.setName(names.Name);
		name.setIndex("name");
		name.setDisplayName(displayNames.Name);
		name.get().length = new integer(NameLength);
		name.get().width = new integer(NameWidth);

		description.setName(names.Description);
		description.setIndex("description");
		description.setDisplayName(displayNames.Description);

		locked.setName(names.Locked);
		locked.setIndex("locked");
		locked.setDisplayName(displayNames.Locked);
		locked.setSystem(true);

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
		archive.setDisplayName(displayNames.Archive);
		archive.setSystem(true);
		
		fullText.setName(names.FullText);
		fullText.setIndex("fullText");
		fullText.setDisplayName(displayNames.FullText);
		fullText.get().length = new integer(FullTextLength);
		fullText.get().width = new integer(FullTextWidth);
		fullText.setExportable(false);
		fullText.setSystem(true);

		registerDataField(recordId);
		registerDataField(createdAt);
		registerDataField(modifiedAt);
		registerDataField(createdBy);
		registerDataField(modifiedBy);
		registerDataField(archive);
		registerDataField(fullText);

		registerDataField(id);
		registerDataField(name);
		registerDataField(description);
		registerDataField(locked);
	}

	@Override
	public Field primaryKey() {
		return recordId.get();
	}
	
	@Override
	public Field fullTextField() {
		return fullText.get();
	}

	@Override
	public int fullTextLength() {
		return FullTextLength;
	}
	
	@Override
	protected void beforeCreate(Query data, guid recordId, guid parentId, Query model, guid modelRecordId) {
		createdAt.get().set(new date());
		createdBy.get().set(ApplicationServer.getUser().id());

		super.beforeCreate(data, recordId, parentId, model, modelRecordId);
	}

	@Override
	protected void beforeUpdate(Query data, guid recordId, Collection<Field> fields, Query model, guid modelRecordId) {
		if(data == this && !fields.isEmpty()) {
			modifiedAt.get().set(new date());
			modifiedBy.get().set(ApplicationServer.getUser().id());
		}

		super.beforeUpdate(data, recordId, fields, model, modelRecordId);
	}
	
	public int controlSum() {
		int result = 0;

		for(Field field : getPrimaryFields())
			result += field.controlSum();

		return Math.abs(result);
	}
}
