package org.zenframework.z8.server.base.table.system;

import java.util.Arrays;
import java.util.LinkedHashMap;

import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.BoolField;
import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.sql.expressions.Equ;
import org.zenframework.z8.server.engine.Runtime;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.security.Role;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.exception;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;

public class Roles extends Table {
	final static public String TableName = "SystemRoles";

	static public class names {
		public final static String Role = "Role";
		public final static String Table = "Table";
		public final static String Read = "Read";
		public final static String Write = "Write";
		public final static String Create = "Create";
		public final static String Copy = "Copy";
		public final static String Destroy = "Destroy";
		public final static String Execute = "Execute";
	}

	static public class strings {
		public final static String Title = "Roles.title";
		public final static String Name = "Roles.name";
		public final static String Read = "Roles.read";
		public final static String Write = "Roles.write";
		public final static String Create = "Roles.create";
		public final static String Copy = "Roles.copy";
		public final static String Destroy = "Roles.destroy";
		public final static String Execute = "Roles.execute";
	}

	static public class displayNames {
		public final static String Title = Resources.get(strings.Title);
		public final static String Name = Resources.get(strings.Name);
		public final static String Read = Resources.get(strings.Read);
		public final static String Write = Resources.get(strings.Write);
		public final static String Create = Resources.get(strings.Create);
		public final static String Copy = Resources.get(strings.Copy);
		public final static String Destroy = Resources.get(strings.Destroy);
		public final static String Execute = Resources.get(strings.Execute);
	}

	public BoolField.CLASS<BoolField> read = new BoolField.CLASS<BoolField>(this);
	public BoolField.CLASS<BoolField> write = new BoolField.CLASS<BoolField>(this);
	public BoolField.CLASS<BoolField> create = new BoolField.CLASS<BoolField>(this);
	public BoolField.CLASS<BoolField> copy = new BoolField.CLASS<BoolField>(this);
	public BoolField.CLASS<BoolField> destroy = new BoolField.CLASS<BoolField>(this);
	public BoolField.CLASS<BoolField> execute = new BoolField.CLASS<BoolField>(this);

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

		read.setName(names.Read);
		read.setIndex("read");
		read.setDisplayName(displayNames.Read);
		read.get().setDefault(new bool(true));

		write.setName(names.Write);
		write.setIndex("write");
		write.setDisplayName(displayNames.Write);
		write.get().setDefault(new bool(true));

		create.setName(names.Create);
		create.setIndex("create");
		create.setDisplayName(displayNames.Create);
		create.get().setDefault(new bool(true));

		copy.setName(names.Copy);
		copy.setIndex("copy");
		copy.setDisplayName(displayNames.Copy);
		copy.get().setDefault(new bool(false));

		destroy.setName(names.Destroy);
		destroy.setIndex("destroy");
		destroy.setDisplayName(displayNames.Destroy);
		destroy.get().setDefault(new bool(false));

		execute.setName(names.Execute);
		execute.setIndex("execute");
		execute.setDisplayName(displayNames.Execute);
		execute.get().setDefault(new bool(false));

		registerDataField(read);
		registerDataField(write);
		registerDataField(create);
		registerDataField(copy);
		registerDataField(destroy);
		registerDataField(execute);

		this.setTransactive(true);
	}

	@Override
	public void initStaticRecords() {
		{
			LinkedHashMap<IField, primary> values = new LinkedHashMap<IField, primary>();
			values.put(name.get(), new string(Role.displayNames.Administrator));
			values.put(description.get(), new string(Role.displayNames.Administrator));
			values.put(read.get(), new bool(true));
			values.put(write.get(), new bool(true));
			values.put(create.get(), new bool(true));
			values.put(copy.get(), new bool(true));
			values.put(destroy.get(), new bool(true));
			values.put(execute.get(), new bool(true));
			values.put(locked.get(), new bool(true));
			addRecord(Role.Administrator.guid(), values);
		}
		{
			LinkedHashMap<IField, primary> values = new LinkedHashMap<IField, primary>();
			values.put(name.get(), new string(Role.displayNames.User));
			values.put(description.get(), new string(Role.displayNames.User));
			values.put(read.get(), new bool(true));
			values.put(write.get(), new bool(true));
			values.put(create.get(), new bool(true));
			values.put(copy.get(), new bool(true));
			values.put(destroy.get(), new bool(true));
			values.put(execute.get(), new bool(true));
			values.put(locked.get(), new bool(true));
			addRecord(Role.User.guid(), values);
		}

		{
			LinkedHashMap<IField, primary> values = new LinkedHashMap<IField, primary>();
			values.put(name.get(), new string(Role.displayNames.Guest));
			values.put(description.get(), new string(Role.displayNames.Guest));
			values.put(read.get(), new bool(true));
			values.put(write.get(), new bool(false));
			values.put(create.get(), new bool(false));
			values.put(copy.get(), new bool(false));
			values.put(destroy.get(), new bool(false));
			values.put(execute.get(), new bool(false));
			values.put(locked.get(), new bool(true));
			addRecord(Role.Guest.guid(), values);
		}
	}

	@Override
	public void afterCreate(guid recordId, guid parentId) {
		super.beforeCreate(recordId, parentId);

		RoleTableAccess rta = new RoleTableAccess.CLASS<RoleTableAccess>().get();

		for(guid table : Runtime.instance().tableKeys()) {
			rta.role.get().set(recordId);
			rta.table.get().set(table);
			rta.create();
		}

		Fields fields = new Fields.CLASS<Fields>().get();
		RoleFieldAccess rfa = new RoleFieldAccess.CLASS<RoleFieldAccess>().get();

		fields.read(Arrays.asList(fields.primaryKey()));

		while(fields.next()) {
			rfa.role.get().set(recordId);
			rfa.field.get().set(fields.recordId());
			rfa.create();
		}
	}

	@Override
	public void beforeDestroy(guid recordId) {
		super.beforeDestroy(recordId);

		if(Role.Administrator.guid().equals(recordId) || Role.User.guid().equals(recordId) ||
				Role.Guest.guid().equals(recordId))
			throw new exception("Builtin roles can not be deleted");

		RoleFieldAccess rfa = new RoleFieldAccess.CLASS<RoleFieldAccess>().get();
		rfa.destroy(new Equ(rfa.role.get(), recordId));

		RoleTableAccess rta = new RoleTableAccess.CLASS<RoleTableAccess>().get();
		rta.destroy(new Equ(rta.role.get(), recordId));
	}
}
