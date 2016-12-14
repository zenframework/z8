package org.zenframework.z8.server.base.table.system;

import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.IntegerField;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.security.Role;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.integer;

public class Sequences extends Table {
	final static public String TableName = "SystemSequences";

	static public class names {
		public final static String Value = "Value";
	}

	static public class strings {
		public final static String Title = "Sequences.title";
		public final static String Value = "Sequences.value";
	}

	static public class displayNames {
		public final static String Title = Resources.get(strings.Title);
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

	public IntegerField.CLASS<IntegerField> value = new IntegerField.CLASS<IntegerField>(this);

	public Sequences(IObject container) {
		super(container);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		value.setName(names.Value);
		value.setIndex("value");
		value.setDisplayName(displayNames.Value);

		readOnly.set(ApplicationServer.getUser().role() != Role.Administrator);

		registerDataField(value);

		id.get().indexed = new bool(true);
		id.get().length = new integer(256);

		name.get().visible = new bool(false);

		description.get().width = new integer(100);

		sortFields.add(description);
	}
}
