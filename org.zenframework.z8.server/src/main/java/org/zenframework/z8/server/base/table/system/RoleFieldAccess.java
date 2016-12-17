package org.zenframework.z8.server.base.table.system;

import org.zenframework.z8.server.base.query.RecordLock;
import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.BoolField;
import org.zenframework.z8.server.base.table.value.Link;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.bool;

public class RoleFieldAccess extends Table {
	final static public String TableName = "SystemRoleFieldAccess";

	static public class names {
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
		role.get(CLASS.Constructor1).operatorAssign(roles);
		field.get(CLASS.Constructor1).operatorAssign(fields);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		locked.get().setDefault(RecordLock.Destroy);

		roles.setIndex("roles");
		fields.setIndex("fields");

		role.setName(names.Role);
		role.setIndex("role");

		field.setName(names.Field);
		field.setIndex("field");

		read.setName(names.Read);
		read.setIndex("read");
		read.setDisplayName(displayNames.Read);
		read.get().setDefault(bool.True);

		write.setName(names.Write);
		write.setIndex("write");
		write.setDisplayName(displayNames.Write);
		write.get().setDefault(bool.True);

		registerDataField(role);
		registerDataField(field);

		registerDataField(read);
		registerDataField(write);

		queries.add(roles);
		queries.add(fields);
	}
}
