package org.zenframework.z8.server.base.table.system;

import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.IntegerField;
import org.zenframework.z8.server.base.table.value.StringField;
import org.zenframework.z8.server.base.table.value.TextField;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.integer;

public class Sequences extends Table {
	final static public String TableName = "SystemSequences";

	static public class fieldNames {
		public final static String Description = "Description";
		public final static String Key = "Key";
		public final static String Value = "Value";
	}

	static public class strings {
		public final static String Title = "Sequences.title";
		public final static String Key = "Sequences.key";
		public final static String Value = "Sequences.value";
	}

	static public class displayNames {
		public final static String Title = Resources.get(strings.Title);
		public final static String Key = Resources.get(strings.Key);
		public final static String Value = Resources.get(strings.Value);
	}

	public static class CLASS<T extends Sequences> extends Table.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(Sequences.class);
			setName(TableName);
			setDisplayName(displayNames.Title);
		}

		@Override
		public Object newObject(IObject container) {
			return new Sequences(container);
		}
	}

	public TextField.CLASS<? extends StringField> description = new TextField.CLASS<TextField>(this);
	public StringField.CLASS<StringField> key = new StringField.CLASS<StringField>(this);
	public IntegerField.CLASS<IntegerField> value = new IntegerField.CLASS<IntegerField>(this);

	public Sequences(IObject container) {
		super(container);
	}

	@Override
	public void initMembers() {
		super.initMembers();

		objects.add(description);
		objects.add(key);
		objects.add(value);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		description.setName(fieldNames.Description);
		description.setIndex("description");

		key.setName(fieldNames.Key);
		key.setIndex("key");
		key.setDisplayName(displayNames.Key);
		key.get().indexed = bool.True;
		key.get().length = new integer(256);

		value.setName(fieldNames.Value);
		value.setIndex("value");
		value.setDisplayName(displayNames.Value);
	}
}
