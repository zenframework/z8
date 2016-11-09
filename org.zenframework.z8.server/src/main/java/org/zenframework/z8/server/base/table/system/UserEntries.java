package org.zenframework.z8.server.base.table.system;

import java.util.LinkedHashMap;

import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.base.table.value.IntegerField;
import org.zenframework.z8.server.base.table.value.Link;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.security.BuiltinUsers;
import org.zenframework.z8.server.security.SecurityGroup;
import org.zenframework.z8.server.types.exception;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.primary;

public class UserEntries extends Table {
	final static public guid Id = new guid("00000000-0000-0000-0000-000000000001");
	final static public guid Id2 = new guid("00000000-0000-0000-0000-000000000002");
	final static public guid Id3 = new guid("00000000-0000-0000-0000-000000000003");

	final static public String TableName = "SystemUserEntries";

	static public class names {
		public final static String User = "UserId";
		public final static String Entry = "EntryId";
		public final static String Position = "Position";
		public final static String Entries = "Entries";
		public final static String Users = "Users";
	}

	static public class strings {
		public final static String Title = "UserEntries.title";
		public final static String User = "UserEntries.user";
		public final static String Entry = "UserEntries.entry";
		public final static String Position = "UserEntries.position";
	}

	static public class displayNames {
		public final static String Title = Resources.get(strings.Title);
		public final static String User = Resources.get(strings.User);
		public final static String Entry = Resources.get(strings.Entry);
		public final static String Position = Resources.get(strings.Position);
	}

	public static class CLASS<T extends UserEntries> extends Table.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(UserEntries.class);
			setName(TableName);
			setDisplayName(Resources.get(strings.Title));
		}

		@Override
		public Object newObject(IObject container) {
			return new UserEntries(container);
		}
	}

	public Users.CLASS<Users> users = new Users.CLASS<Users>(this);
	public Entries.CLASS<Entries> entries = new Entries.CLASS<Entries>(this);

	public Link.CLASS<Link> user = new Link.CLASS<Link>(this);
	public Link.CLASS<Link> entry = new Link.CLASS<Link>(this);

	public IntegerField.CLASS<IntegerField> position = new IntegerField.CLASS<IntegerField>(this);

	public UserEntries() {
		this(null);
	}

	public UserEntries(IObject container) {
		super(container);
	}

	@Override
	public void constructor1() {
		user.get(CLASS.Constructor1).operatorAssign(users);
		entry.get(CLASS.Constructor1).operatorAssign(entries);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		users.setIndex("users");
		entries.setIndex("entries");

		user.setName(names.User);
		user.setIndex("user");
		user.setDisplayName(Resources.get(strings.User));

		entry.setName(names.Entry);
		entry.setIndex("entry");
		entry.setDisplayName(Resources.get(strings.Entry));

		position.setName(names.Position);
		position.setIndex("position");
		position.setDisplayName(Resources.get(strings.Position));

		readOnly.set(ApplicationServer.getUser().securityGroup() != SecurityGroup.Administrators);

		registerDataField(user);
		registerDataField(entry);
		registerDataField(position);

		queries.add(users);
		queries.add(entries);

		links.add(user);
		links.add(entry);

		{
			LinkedHashMap<IField, primary> record = new LinkedHashMap<IField, primary>();
			record.put(user.get(), BuiltinUsers.Administrator.guid());
			record.put(entry.get(), SystemTools.Id);
			addRecord(Id, record);
		}

		{
			LinkedHashMap<IField, primary> record = new LinkedHashMap<IField, primary>();
			record.put(user.get(), BuiltinUsers.Administrator.guid());
			record.put(entry.get(), SystemTools.Id);
			addRecord(Id, record);
		}
	}

	@Override
	public void beforeUpdate(guid recordId) {
		super.beforeUpdate(recordId);

		guid userId = user.get().get().guid();

		if(userId.equals(UserEntries.Id) && entry.get().changed()) {
			guid new_entryId = entry.get().guid();

			if(!new_entryId.equals(SystemTools.Id.guid())) {
				throw new exception("Builtin user's entrypoints can not be removed. Add a new record instead.");
			}
		}
	}

	@Override
	public void beforeDestroy(guid recordId) {
		super.beforeDestroy(recordId);

		if(recordId.equals(UserEntries.Id))
			throw new exception("Builtin user's entrypoints can not be removed.");
	}
}
