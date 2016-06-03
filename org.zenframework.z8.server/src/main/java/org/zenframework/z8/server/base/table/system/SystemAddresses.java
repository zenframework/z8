package org.zenframework.z8.server.base.table.system;

import java.util.ArrayList;
import java.util.Collection;

import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.Link;
import org.zenframework.z8.server.base.table.value.StringField;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.expressions.Operation;
import org.zenframework.z8.server.db.sql.expressions.Rel;
import org.zenframework.z8.server.db.sql.functions.string.Lower;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.security.BuiltinUsers;
import org.zenframework.z8.server.security.IUser;
import org.zenframework.z8.server.security.User;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.sql.sql_string;

public class SystemAddresses extends Table {
	public static final String TableName = "SystemAddresses";

	static public class names {
		public final static String User = "UserId";
	}

	static public class strings {
		public final static String Title = "SystemAddresses.title";
		public final static String User = "SystemAddresses.user";
		public final static String Id = "SystemAddresses.id";
	}

	public static class CLASS<T extends SystemAddresses> extends Table.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(SystemAddresses.class);
			setName(TableName);
			setDisplayName(Resources.get(strings.Title));
		}

		@Override
		public Object newObject(IObject container) {
			return new SystemAddresses(container);
		}
	}

	public Users.CLASS<Users> users = new Users.CLASS<Users>(this);
	public Link.CLASS<Link> user = new Link.CLASS<Link>(this);

	static public SystemAddresses newInstance() {
		return new SystemAddresses.CLASS<SystemAddresses>().get();
	}

	public SystemAddresses(IObject container) {
		super(container);
	}

	@Override
	public void constructor1() {
		user.get(CLASS.Constructor1).operatorAssign(users);
	}

	@Override
	public void constructor2() {
		super.constructor2();

        users.setIndex("users");
		
		id.setDisplayName(Resources.get(strings.Id));
		id.get().length.set(256);
		id.get().unique.set(true);

		user.setName(names.User);
		user.setIndex("user");
		user.setDisplayName(Resources.get(strings.User));

		registerDataField(user);

		registerFormField(id);
		registerFormField(users.get().name);
		registerFormField(users.get().description);
		
        queries.add(users);

        links.add(user);
	}

	public void onNew(guid recordId, guid parentId, guid modelRecordId) {
		user.get().set(BuiltinUsers.System.guid());

		super.onNew(recordId, parentId, modelRecordId);
	}

	static public IUser getDefaultUser(String address) {
		SystemAddresses addresses = newInstance();
		
		StringField name = addresses.users.get().name.get();
		StringField id = addresses.id.get();
		
		Collection<Field> fields = new ArrayList<Field>();
		fields.add(name);
		
		SqlToken where = new Rel(new Lower(id), Operation.Eq, new sql_string(address.toLowerCase()));
		
		String login = addresses.readFirst(fields, where) ? name.string().get() : BuiltinUsers.System.name();
		return User.load(login);
	}
}
