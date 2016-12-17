package org.zenframework.z8.server.base.table.system;

import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.BoolField;
import org.zenframework.z8.server.base.table.value.Link;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;

public class UserFieldAccess extends Table {
	final static public String TableName = "SystemUserFieldAccess";

	static public class names {
		public final static String User = "User";
		public final static String Field = "Field";
		public final static String Read = "Read";
		public final static String Write = "Write";
	}

	static public class strings {
		public final static String Title = "UserFieldAccess.title";
		public final static String Read = "UserFieldAccess.read";
		public final static String Write = "UserFieldAccess.write";
	}

	static public class displayNames {
		public final static String Title = Resources.get(strings.Title);
		public final static String Read = Resources.get(strings.Read);
		public final static String Write = Resources.get(strings.Write);
	}

	public static class CLASS<T extends UserFieldAccess> extends Table.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(UserFieldAccess.class);
			setName(TableName);
			setDisplayName(displayNames.Title);
		}

		@Override
		public Object newObject(IObject container) {
			return new UserFieldAccess(container);
		}
	}

	public Users.CLASS<Users> users = new Users.CLASS<Users>(this);
	public Fields.CLASS<Fields> fields = new Fields.CLASS<Fields>(this);

	public Link.CLASS<Link> user = new Link.CLASS<Link>(this);
	public Link.CLASS<Link> field = new Link.CLASS<Link>(this);

	public BoolField.CLASS<BoolField> read = new BoolField.CLASS<BoolField>(this);
	public BoolField.CLASS<BoolField> write = new BoolField.CLASS<BoolField>(this);

	public UserFieldAccess() {
		this(null);
	}

	public UserFieldAccess(IObject container) {
		super(container);
	}

	@Override
	public void constructor1() {
		user.get(CLASS.Constructor1).operatorAssign(users);
		field.get(CLASS.Constructor1).operatorAssign(fields);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		users.setIndex("users");
		fields.setIndex("fields");

		user.setName(names.User);
		user.setIndex("user");

		field.setName(names.Field);
		field.setIndex("field");

		read.setName(names.Read);
		read.setIndex("read");
		read.setDisplayName(displayNames.Read);

		write.setName(names.Write);
		write.setIndex("write");
		write.setDisplayName(displayNames.Write);

		registerDataField(user);
		registerDataField(field);

		registerDataField(read);
		registerDataField(write);

		queries.add(users);
		queries.add(fields);
	}
}
