package org.zenframework.z8.server.base.table.system;

import java.util.Arrays;

import org.zenframework.z8.server.base.query.RecordLock;
import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.BoolField;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.Link;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IClass;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.guid;

public class RoleTableAccess extends Table {
	final static public String TableName = "SystemRoleTableAccess";

	static public class fieldNames {
		public final static String Role = "Role";
		public final static String Table = "Table";
		public final static String Read = "Read";
		public final static String Write = "Write";
		public final static String Create = "Create";
		public final static String Copy = "Copy";
		public final static String Destroy = "Destroy";
	}

	static public class strings {
		public final static String Title = "RoleTableAccess.title";
		public final static String Read = "RoleTableAccess.read";
		public final static String Write = "RoleTableAccess.write";
		public final static String Create = "RoleTableAccess.create";
		public final static String Copy = "RoleTableAccess.copy";
		public final static String Destroy = "RoleTableAccess.destroy";
	}

	static public class displayNames {
		public final static String Title = Resources.get(strings.Title);
		public final static String Read = Resources.get(strings.Read);
		public final static String Write = Resources.get(strings.Write);
		public final static String Create = Resources.get(strings.Create);
		public final static String Copy = Resources.get(strings.Copy);
		public final static String Destroy = Resources.get(strings.Destroy);
	}

	public static class CLASS<T extends RoleTableAccess> extends Table.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(RoleTableAccess.class);
			setName(TableName);
			setDisplayName(displayNames.Title);
		}

		@Override
		public Object newObject(IObject container) {
			return new RoleTableAccess(container);
		}
	}

	public Roles.CLASS<Roles> roles = new Roles.CLASS<Roles>(this);
	public Tables.CLASS<Tables> tables = new Tables.CLASS<Tables>(this);

	public Link.CLASS<Link> role = new Link.CLASS<Link>(this);
	public Link.CLASS<Link> table = new Link.CLASS<Link>(this);

	public BoolField.CLASS<BoolField> read = new BoolField.CLASS<BoolField>(this);
	public BoolField.CLASS<BoolField> write = new BoolField.CLASS<BoolField>(this);
	public BoolField.CLASS<BoolField> create = new BoolField.CLASS<BoolField>(this);
	public BoolField.CLASS<BoolField> copy = new BoolField.CLASS<BoolField>(this);
	public BoolField.CLASS<BoolField> destroy = new BoolField.CLASS<BoolField>(this);

	public RoleTableAccess() {
		this(null);
	}

	public RoleTableAccess(IObject container) {
		super(container);
	}

	@Override
	public void constructor1() {
		role.get(IClass.Constructor1).operatorAssign(roles);
		table.get(IClass.Constructor1).operatorAssign(tables);
	}

	@Override
	public void initMembers() {
		super.initMembers();

		objects.add(role);
		objects.add(table);

		objects.add(read);
		objects.add(write);
		objects.add(create);
		objects.add(copy);
		objects.add(destroy);

		objects.add(roles);
		objects.add(tables);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		lock.get().setDefault(RecordLock.Destroy);

		roles.setIndex("roles");
		tables.setIndex("tables");

		role.setName(fieldNames.Role);
		role.setIndex("role");

		table.setName(fieldNames.Table);
		table.setIndex("table");

		read.setName(fieldNames.Read);
		read.setIndex("read");
		read.setDisplayName(displayNames.Read);

		write.setName(fieldNames.Write);
		write.setIndex("write");
		write.setDisplayName(displayNames.Write);

		create.setName(fieldNames.Create);
		create.setIndex("create");
		create.setDisplayName(displayNames.Create);

		copy.setName(fieldNames.Copy);
		copy.setIndex("copy");
		copy.setDisplayName(displayNames.Copy);

		destroy.setName(fieldNames.Destroy);
		destroy.setIndex("destroy");
		destroy.setDisplayName(displayNames.Destroy);
	}

	@Override
	public void z8_beforeUpdate(guid recordId) {
		Field read = this.read.get();
		Field write = this.write.get();
		Field create = this.create.get();
		Field copy = this.copy.get();
		Field destroy = this.destroy.get();

		if(read.changed()) {
			if(!read.bool().get()) {
				write.set(bool.False);
				create.set(bool.False);
				copy.set(bool.False);
				destroy.set(bool.False);
			}
		} else if(write.changed()) {
			if(!write.bool().get()) {
				create.set(bool.False);
				copy.set(bool.False);
				destroy.set(bool.False);
			} else
				read.set(bool.True);
		} else if(create.changed()) {
			if(!create.bool().get())
				copy.set(bool.False);
			else {
				read.set(bool.True);
				write.set(bool.True);
			}
		} else if(copy.changed()) {
			if(copy.bool().get()) {
				read.set(bool.True);
				write.set(bool.True);
				create.set(bool.True);
			}
		} else if(destroy.changed()) {
			if(destroy.bool().get())
				read.set(bool.True);
		}

		super.z8_beforeUpdate(recordId);
	}

	@Override
	public void z8_afterUpdate(guid recordId) {
		super.z8_afterUpdate(recordId);

		Field role = this.role.get();
		if(readRecord(recordId, Arrays.asList(role)))
			Roles.notifyRoleChange(role.guid());
	}
}
