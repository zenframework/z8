package org.zenframework.z8.server.base.table.system;

import java.util.Arrays;

import org.zenframework.z8.server.base.query.RecordLock;
import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.BoolField;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.Link;
import org.zenframework.z8.server.db.sql.expressions.And;
import org.zenframework.z8.server.db.sql.expressions.Equ;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IClass;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.guid;

public class RoleFieldAccess extends Table {
	final static public String TableName = "SystemRoleFieldAccess";

	static public class fieldNames {
		public final static String Role = "Role";
		public final static String Field = "Field";
		public final static String Read = "Read";
		public final static String Write = "Write";
	}

	static public class strings {
		public final static String Title = "RoleFieldAccess.title";
		public final static String Read = "RoleFieldAccess.read";
		public final static String Write = "RoleFieldAccess.write";
	}

	static public class displayNames {
		public final static String Title = Resources.get(strings.Title);
		public final static String Read = Resources.get(strings.Read);
		public final static String Write = Resources.get(strings.Write);
	}

	public static class CLASS<T extends RoleFieldAccess> extends Table.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(RoleFieldAccess.class);
			setName(TableName);
			setDisplayName(displayNames.Title);
		}

		@Override
		public Object newObject(IObject container) {
			return new RoleFieldAccess(container);
		}
	}

	public Roles.CLASS<Roles> role = new Roles.CLASS<Roles>(this);
	public Fields.CLASS<Fields> field = new Fields.CLASS<Fields>(this);

	public Link.CLASS<Link> roleId = new Link.CLASS<Link>(this);
	public Link.CLASS<Link> fieldId = new Link.CLASS<Link>(this);

	public BoolField.CLASS<BoolField> read = new BoolField.CLASS<BoolField>(this);
	public BoolField.CLASS<BoolField> write = new BoolField.CLASS<BoolField>(this);

	public RoleFieldAccess() {
		this(null);
	}

	public RoleFieldAccess(IObject container) {
		super(container);
	}

	@Override
	public void constructor1() {
		roleId.get(IClass.Constructor1).operatorAssign(role);
		fieldId.get(IClass.Constructor1).operatorAssign(field);
	}

	@Override
	public void initMembers() {
		super.initMembers();

		objects.add(roleId);
		objects.add(fieldId);

		objects.add(read);
		objects.add(write);

		objects.add(role);
		objects.add(field);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		lock.get().setDefault(RecordLock.Destroy);

		role.setIndex("role");
		field.setIndex("field");

		roleId.setName(fieldNames.Role);
		roleId.setIndex("roleId");

		fieldId.setName(fieldNames.Field);
		fieldId.setIndex("fieldId");

		read.setName(fieldNames.Read);
		read.setIndex("read");
		read.setDisplayName(displayNames.Read);

		write.setName(fieldNames.Write);
		write.setIndex("write");
		write.setDisplayName(displayNames.Write);
	}

	@Override
	public void z8_beforeUpdate(guid recordId) {
		Field read = this.read.get();
		Field write = this.write.get();

		if(read.changed()) {
			if(!read.bool().get())
				write.set(bool.False);
		} else if(write.changed()) {
			if(write.bool().get())
				read.set(bool.True);
		}

		super.z8_beforeUpdate(recordId);
	}

	@Override
	public void onUpdateAction(guid recordId) {
		super.onUpdateAction(recordId);

		if(readRecord(recordId, Arrays.asList(roleId.get())))
			Roles.notifyRoleChange(roleId.get().guid());
	}

	public void updateAccess(guid roleId, guid tableId, bool read, bool write) {
		if(read != null)
			this.read.get().set(read);

		if(write != null)
			this.write.get().set(write);

		update(new And(new Equ(this.roleId.get(), roleId), new Equ(field.get().tableId.get(), tableId)));
	}
}
