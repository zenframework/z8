package org.zenframework.z8.server.base.table.system;

import java.util.LinkedHashMap;

import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.security.Role;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;

public class Roles extends Table {
	final static public String TableName = "SystemRoles";

	static public class strings {
		public final static String Title = "Roles.title";
		public final static String Name = "Roles.name";
	}

	static public class displayNames {
		public final static String Title = Resources.get(strings.Title);
		public final static String Name = Resources.get(strings.Name);
	}

	public static class CLASS<T extends Roles> extends Table.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(Roles.class);
			setName(Roles.TableName);
			setDisplayName(displayNames.Title);
		}

		@Override
		public Object newObject(IObject container) {
			return new Roles(container);
		}
	}

	public Roles(IObject container) {
		super(container);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		name.setDisplayName(displayNames.Name);
		name.get().length = new integer(50);
	}

	@Override
	public void initStaticRecords() {
		{
			LinkedHashMap<IField, primary> values = new LinkedHashMap<IField, primary>();
			values.put(name.get(), new string(Role.displayNames.Administrator));
			values.put(description.get(), new string(Role.displayNames.Administrator));
			addRecord(Role.Administrator.guid(), values);
		}
		{
			LinkedHashMap<IField, primary> values = new LinkedHashMap<IField, primary>();
			values.put(name.get(), new string(Role.displayNames.User));
			values.put(description.get(), new string(Role.displayNames.User));
			addRecord(Role.User.guid(), values);
		}
	}
}
