package org.zenframework.z8.server.base.table.system;

import java.util.Arrays;
import java.util.Optional;

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
		public final static String Role = "RoleFieldAccess.role";
		public final static String Field = "RoleFieldAccess.field";
		public final static String Read = "RoleFieldAccess.read";
		public final static String Write = "RoleFieldAccess.write";
	}

	static public class displayNames {
		public final static String Title = Resources.get(strings.Title);
		public final static String Read = Resources.get(strings.Read);
		public final static String Write = Resources.get(strings.Write);
	}

	static public class apiAttrs {
		public final static String Title = Resources.getOrNull(strings.Title + ".APIDescription");
		public final static String Role = Resources.getOrNull(strings.Role + ".APIDescription");
		public final static String Field = Resources.getOrNull(strings.Field + ".APIDescription");
		public final static String Read = Resources.getOrNull(strings.Read + ".APIDescription");
		public final static String Write = Resources.getOrNull(strings.Write + ".APIDescription");
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
			Optional.ofNullable(apiAttrs.Title)
					.ifPresent(attrVal -> setAttribute("APIDescription", attrVal));
		}

		@Override
		public Object newObject(IObject container) {
			return new RoleFieldAccess(container);
		}
	}

	public Roles.CLASS<Roles> roles = new Roles.CLASS<Roles>(this);
	public Fields.CLASS<Fields> fields = new Fields.CLASS<Fields>(this);

	public Link.CLASS<Link> role = new Link.CLASS<Link>(this);
	public Link.CLASS<Link> field = new Link.CLASS<Link>(this);

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
		role.get(IClass.Constructor1).operatorAssign(roles);
		field.get(IClass.Constructor1).operatorAssign(fields);
	}

	@Override
	public void initMembers() {
		super.initMembers();

		objects.add(role);
		objects.add(field);

		objects.add(read);
		objects.add(write);

		objects.add(roles);
		objects.add(fields);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		lock.get().setDefault(RecordLock.Destroy);

		roles.setIndex("roles");
		fields.setIndex("fields");

		role.setName(fieldNames.Role);
		role.setIndex("role");
		Optional.ofNullable(apiAttrs.Role)
				.ifPresent(attrVal -> role.setAttribute("APIDescription", attrVal));

		field.setName(fieldNames.Field);
		field.setIndex("field");
		Optional.ofNullable(apiAttrs.Field)
				.ifPresent(attrVal -> field.setAttribute("APIDescription", attrVal));

		read.setName(fieldNames.Read);
		read.setIndex("read");
		read.setDisplayName(displayNames.Read);
		Optional.ofNullable(apiAttrs.Read)
				.ifPresent(attrVal -> read.setAttribute("APIDescription", attrVal));

		write.setName(fieldNames.Write);
		write.setIndex("write");
		write.setDisplayName(displayNames.Write);
		Optional.ofNullable(apiAttrs.Write)
				.ifPresent(attrVal -> write.setAttribute("APIDescription", attrVal));
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

	private boolean notificationsDisabled = false;

	@Override
	public void z8_afterUpdate(guid recordId) {
		super.z8_afterUpdate(recordId);

		if(notificationsDisabled)
			return;

		Field role = this.role.get();
		if(readRecord(recordId, Arrays.asList(role)))
			Roles.notifyRoleChange(role.guid());
	}

	public void updateAccess(guid roleId, guid tableId, bool read, bool write) {
		if(read != null)
			this.read.get().set(read);

		if(write != null)
			this.write.get().set(write);

		notificationsDisabled = true;
		update(new And(new Equ(role.get(), roleId), new Equ(fields.get().table.get(), tableId)));
		notificationsDisabled = false;
	}
}
