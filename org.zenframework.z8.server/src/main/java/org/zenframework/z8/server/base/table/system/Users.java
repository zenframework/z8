package org.zenframework.z8.server.base.table.system;

import java.util.LinkedHashMap;

import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.BoolField;
import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.base.table.value.Link;
import org.zenframework.z8.server.base.table.value.StringField;
import org.zenframework.z8.server.base.table.value.TextField;
import org.zenframework.z8.server.crypto.MD5;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.engine.IAuthorityCenter;
import org.zenframework.z8.server.engine.Runtime;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.RLinkedHashMap;
import org.zenframework.z8.server.security.BuiltinUsers;
import org.zenframework.z8.server.security.IUser;
import org.zenframework.z8.server.security.SecurityGroup;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.exception;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;

public class Users extends Table {
	final static public String TableName = "SystemUsers";

	static public final guid System = BuiltinUsers.System.guid();
	static public final guid Administrator = BuiltinUsers.Administrator.guid();

	static private String defaultPassword = MD5.get("");

	static public class names {
		public final static String Password = "Password";
		public final static String SecurityGroup = "SecurityGroup";
		public final static String SecurityGroups = "SecurityGroups";
		public final static String Blocked = "Blocked";
		public final static String Phone = "Phone";
		public final static String Email = "Email";
		public final static String Settings = "Settings";
	}

	static public class strings {
		public final static String Title = "Users.title";
		public final static String Name = "Users.name";
		public final static String Description = "Users.description";
		public final static String Password = "Users.password";
		public final static String SecurityGroup = "Users.securityGroup";
		public final static String Blocked = "Users.blocked";
		public final static String Phone = "Users.phone";
		public final static String Email = "Users.email";
		public final static String Settings = "Users.settings";

		public final static String DefaultName = "Users.name.default";
	}

	static public class displayNames {
		public final static String Name = Resources.get(strings.Name);
		public final static String Password = Resources.get(strings.Password);
		public final static String SecurityGroup = Resources.get(strings.SecurityGroup);
		public final static String Blocked = Resources.get(strings.Blocked);
		public final static String Phone = Resources.get(strings.Phone);
		public final static String Email = Resources.get(strings.Email);
		public final static String Settings = Resources.get(strings.Settings);
		public final static String DefaultName = Resources.get(strings.DefaultName);
		public final static String Title = Resources.get(strings.Title);
		public final static String Description = Resources.get(strings.Description);

		public final static String SystemName = Resources.get("BuiltinUsers.System.name");
		public final static String SystemDescription = Resources.get("BuiltinUsers.System.description");

		public final static String AdministratorName = Resources.get("BuiltinUsers.Administrator.name");
		public final static String AdministratorDescription = Resources.get("BuiltinUsers.Administrator.description");
	}

	public static class CLASS<T extends Users> extends Table.CLASS<T> {

		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(Users.class);
			setName(TableName);
			setDisplayName(displayNames.Title);
		}

		@Override
		public Object newObject(IObject container) {
			return new Users(container);
		}
	}

	static public Users newInstance() {
		return (Users)Runtime.instance().getTableByName(Users.TableName).newInstance();
	}

	public SecurityGroups.CLASS<SecurityGroups> securityGroups = new SecurityGroups.CLASS<SecurityGroups>(this);

	public Link.CLASS<Link> securityGroup = new Link.CLASS<Link>(this);

	public StringField.CLASS<StringField> password = new StringField.CLASS<StringField>(this);
	public StringField.CLASS<StringField> phone = new StringField.CLASS<StringField>(this);
	public StringField.CLASS<StringField> email = new StringField.CLASS<StringField>(this);
	public BoolField.CLASS<BoolField> blocked = new BoolField.CLASS<BoolField>(this);
	public TextField.CLASS<TextField> settings = new TextField.CLASS<TextField>(this);

	public Users() {
		this(null);
	}

	public Users(IObject container) {
		super(container);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		securityGroups.setIndex("securityGroups");

		name.setDisplayName(displayNames.Name);
		name.setGendb_updatable(false);
		name.get().length = new integer(IAuthorityCenter.MaxLoginLength);
		name.get().unique = new bool(true);

		password.setName(names.Password);
		password.setDisplayName(displayNames.Password);
		password.setExportable(false);
		password.setIndex("password");
		password.get().length = new integer(IAuthorityCenter.MaxPasswordLength);

		description.setDisplayName(displayNames.Description);

		securityGroup.setName(names.SecurityGroup);
		securityGroup.setIndex("securityGroup");
		securityGroup.setDisplayName(displayNames.SecurityGroup);
		securityGroup.get().setDefault(SecurityGroup.Users.guid());
		securityGroup.get().operatorAssign(securityGroups);

		phone.setName(names.Phone);
		phone.setIndex("phone");
		phone.setDisplayName(displayNames.Phone);
		phone.get().length = new integer(128);

		email.setName(names.Email);
		email.setIndex("email");
		email.setDisplayName(displayNames.Email);
		email.get().length = new integer(128);

		blocked.setName(names.Blocked);
		blocked.setIndex("blocked");
		blocked.setDisplayName(displayNames.Blocked);

		settings.setName(names.Settings);
		settings.setIndex("settings");
		settings.setDisplayName(displayNames.Settings);

		registerDataField(securityGroup);
		registerDataField(password);
		registerDataField(phone);
		registerDataField(email);
		registerDataField(blocked);
		registerDataField(settings);

		queries.add(securityGroups);

		{
			LinkedHashMap<IField, primary> record = new LinkedHashMap<IField, primary>();
			record.put(name.get(), new string(displayNames.SystemName));
			record.put(description.get(), new string(displayNames.SystemDescription));
			record.put(securityGroup.get(), SecurityGroup.Users.guid());
			addRecord(BuiltinUsers.System.guid(), record);
		}
		{
			LinkedHashMap<IField, primary> record = new LinkedHashMap<IField, primary>();
			record.put(name.get(), new string(displayNames.AdministratorName));
			record.put(description.get(), new string(displayNames.AdministratorDescription));
			record.put(securityGroup.get(), SecurityGroup.Administrators.guid());
			addRecord(BuiltinUsers.Administrator.guid(), record);
		}
	}

	@Override
	public void onNew(guid recordId, guid parentId) {
		password.get().set(defaultPassword);
		super.onNew(recordId, parentId);
	}

	@Override
	public void beforeCreate(guid recordId, guid parentId) {
		super.beforeCreate(recordId, parentId);

		if(!ApplicationServer.events())
			return;

		StringField name = this.name.get();
		if((!name.changed() || name.string().isEmpty()) && !recordId.equals(guid.NULL))
			name.set(new string(displayNames.DefaultName + name.getSequencer().next()));
	}

	@Override
	public void beforeUpdate(guid recordId) {
		super.beforeUpdate(recordId);

		if((BuiltinUsers.Administrator.guid().equals(recordId) || BuiltinUsers.System.guid().equals(recordId)) && securityGroup.get().changed())
			throw new exception("Unable to change the security group of the builtin user.");
	}

	@Override
	public void beforeDestroy(guid recordId) {
		super.beforeDestroy(recordId);

		if(BuiltinUsers.Administrator.guid().equals(recordId) || BuiltinUsers.System.guid().equals(recordId))
			throw new exception("Unable to delete builtin user!");
	}

	public boolean getExtraParameters(IUser user, RLinkedHashMap<string, primary> parameters) {
		return z8_getParameters(user.id(), new string(user.name()), parameters).get();
	}

	public bool z8_getParameters(guid id, string name, RLinkedHashMap<string, primary> parameters) {
		return new bool(true);
	}
}
