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

	static public class fieldNames {
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

	public Users.CLASS<Users> user = new Users.CLASS<Users>(this);
	public Entries.CLASS<Entries> entry = new Entries.CLASS<Entries>(this);

	public Link.CLASS<Link> userId = new Link.CLASS<Link>(this);
	public Link.CLASS<Link> entryId = new Link.CLASS<Link>(this);

	public IntegerField.CLASS<? extends IntegerField> position = new IntegerField.CLASS<IntegerField>(this);

	public UserEntries() {
		this(null);
	}

	public UserEntries(IObject container) {
		super(container);
	}

	@Override
	public void constructor1() {
		userId.get(IClass.Constructor1).operatorAssign(user);
		entryId.get(IClass.Constructor1).operatorAssign(entry);
	}

	@Override
	public void initMembers() {
		super.initMembers();

		objects.add(userId);
		objects.add(entryId);
		objects.add(position);

		objects.add(user);
		objects.add(entry);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		user.setIndex("user");
		entry.setIndex("entry");

		userId.setName(fieldNames.User);
		userId.setIndex("userId");

		entryId.setName(fieldNames.Entry);
		entryId.setIndex("entryId");

		position.setName(fieldNames.Position);
		position.setIndex("position");
		position.setDisplayName(displayNames.Position);

		readOnly = new bool(!ApplicationServer.getUser().isAdministrator());
	}

	@Override
	public void initStaticRecords() {
		{
			LinkedHashMap<IField, primary> record = new LinkedHashMap<IField, primary>();
			record.put(userId.get(), BuiltinUsers.Administrator.guid());
			record.put(entryId.get(), SystemTools.Id);
			record.put(lock.get(), RecordLock.Destroy);
			addRecord(Administrator, record);
		}

		{
			LinkedHashMap<IField, primary> record = new LinkedHashMap<IField, primary>();
			record.put(userId.get(), BuiltinUsers.System.guid());
			record.put(entryId.get(), SystemTools.Id);
			record.put(lock.get(), RecordLock.Destroy);
			addRecord(System, record);
		}
	}
}
