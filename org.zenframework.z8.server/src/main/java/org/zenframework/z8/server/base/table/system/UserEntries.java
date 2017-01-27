package org.zenframework.z8.server.base.table.system;

import java.util.LinkedHashMap;

import org.zenframework.z8.server.base.query.RecordLock;
import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.base.table.value.IntegerField;
import org.zenframework.z8.server.base.table.value.Link;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IClass;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.security.BuiltinUsers;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.primary;

public class UserEntries extends Table {
	final static public String TableName = "SystemUserEntries";

	final static public guid System = new guid("4A50E864-CFD0-4439-88E9-E4AC2A9B08A5");
	final static public guid Administrator = new guid("D53F3EBE-4D11-4ACE-99D7-07608B68B6C2");

	static public class names {
		public final static String User = "UserId";
		public final static String Entry = "EntryId";
		public final static String Position = "Position";
	}

	static public class strings {
		public final static String Title = "UserEntries.title";
		public final static String Position = "UserEntries.position";
	}

	static public class displayNames {
		public final static String Title = Resources.get(strings.Title);
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
			setDisplayName(displayNames.Title);
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

	public IntegerField.CLASS<? extends IntegerField> position = new IntegerField.CLASS<IntegerField>(this);

	public UserEntries() {
		this(null);
	}

	public UserEntries(IObject container) {
		super(container);
	}

	@Override
	public void constructor1() {
		user.get(IClass.Constructor1).operatorAssign(users);
		entry.get(IClass.Constructor1).operatorAssign(entries);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		users.setIndex("users");
		entries.setIndex("entries");

		user.setName(names.User);
		user.setIndex("user");

		entry.setName(names.Entry);
		entry.setIndex("entry");

		position.setName(names.Position);
		position.setIndex("position");
		position.setDisplayName(displayNames.Position);

		readOnly = new bool(!ApplicationServer.getUser().isAdministrator());

		registerDataField(user);
		registerDataField(entry);
		registerDataField(position);

		objects.add(users);
		objects.add(entries);
	}

	@Override
	public void initStaticRecords() {
		{
			LinkedHashMap<IField, primary> record = new LinkedHashMap<IField, primary>();
			record.put(user.get(), BuiltinUsers.Administrator.guid());
			record.put(entry.get(), SystemTools.Id);
			record.put(lock.get(), RecordLock.Destroy);
			addRecord(Administrator, record);
		}

		{
			LinkedHashMap<IField, primary> record = new LinkedHashMap<IField, primary>();
			record.put(user.get(), BuiltinUsers.System.guid());
			record.put(entry.get(), SystemTools.Id);
			record.put(lock.get(), RecordLock.Destroy);
			addRecord(System, record);
		}
	}
}
