package org.zenframework.z8.server.base.table.system;

import java.util.LinkedHashMap;

import org.zenframework.z8.server.base.query.RecordLock;
import org.zenframework.z8.server.base.security.LoginParameters;
import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.BoolField;
import org.zenframework.z8.server.base.table.value.DatetimeField;
import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.base.table.value.StringField;
import org.zenframework.z8.server.base.table.value.TextField;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.crypto.Digest;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.engine.IAuthorityCenter;
import org.zenframework.z8.server.engine.Runtime;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.RLinkedHashMap;
import org.zenframework.z8.server.security.BuiltinUsers;
import org.zenframework.z8.server.security.IUser;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.exception;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;

public class Users extends Table {
	final static public String TableName = "SystemUsers";

	static public final guid System = BuiltinUsers.System.guid();
	static public final guid Administrator = BuiltinUsers.Administrator.guid();

	static private String defaultPassword = Digest.md5("");

	static public class fieldNames {
		public final static String Password = "Password";
		public final static String FirstName = "First Name";
		public final static String MiddleName = "Middle Name";
		public final static String LastName = "Last Name";
		public final static String Banned = "Banned";
		public final static String ChangePassword = "Change Password";
		public final static String Phone = "Phone";
		public final static String Email = "Email";
		public final static String Settings = "Settings";
		public final static String Verification = "Verification";
		public final static String VerificationModAt = "Verification Modified At";
		public final static String Company = "Company";
		public final static String Position = "Position";
	}

	static public class strings {
		public final static String Title = "Users.title";
		public final static String Login = "Users.login";
		public final static String FirstName = "Users.firstName";
		public final static String MiddleName = "Users.middleName";
		public final static String LastName = "Users.lastName";
		public final static String Description = "Users.description";
		public final static String Banned = "Users.banned";
		public final static String ChangePassword = "Users.changePassword";
		public final static String ResetPassword = "Users.resetPassword";
		public final static String Phone = "Users.phone";
		public final static String Email = "Users.email";
		public final static String Settings = "Users.settings";
		public final static String Company = "Users.company";
		public final static String Position = "Users.position";

		public final static String DefaultName = "Users.name.default";
	}

	static public class displayNames {
		public final static String Login = Resources.get(strings.Login);
		public final static String FirstName = Resources.get(strings.FirstName);
		public final static String MiddleName = Resources.get(strings.MiddleName);
		public final static String LastName = Resources.get(strings.LastName);
		public final static String Banned = Resources.get(strings.Banned);
		public final static String ChangePassword = Resources.get(strings.ChangePassword);
		public final static String ResetPassword = Resources.get(strings.ResetPassword);
		public final static String Phone = Resources.get(strings.Phone);
		public final static String Email = Resources.get(strings.Email);
		public final static String Settings = Resources.get(strings.Settings);
		public final static String DefaultName = Resources.get(strings.DefaultName);
		public final static String Title = Resources.get(strings.Title);
		public final static String Description = Resources.get(strings.Description);
		public final static String Company = Resources.get(strings.Company);
		public final static String Position = Resources.get(strings.Position);

		public final static String SystemName = BuiltinUsers.displayNames.SystemName;
		public final static String AdministratorName = BuiltinUsers.displayNames.AdministratorName;
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

	public StringField.CLASS<StringField> password = new StringField.CLASS<StringField>(this);
	public StringField.CLASS<StringField> firstName = new StringField.CLASS<StringField>(this);
	public StringField.CLASS<StringField> middleName = new StringField.CLASS<StringField>(this);
	public StringField.CLASS<StringField> lastName = new StringField.CLASS<StringField>(this);
	public StringField.CLASS<StringField> phone = new StringField.CLASS<StringField>(this);
	public StringField.CLASS<StringField> email = new StringField.CLASS<StringField>(this);
	public StringField.CLASS<StringField> company = new StringField.CLASS<StringField>(this);
	public StringField.CLASS<StringField> position = new StringField.CLASS<StringField>(this);
	public BoolField.CLASS<BoolField> banned = new BoolField.CLASS<BoolField>(this);
	public BoolField.CLASS<BoolField> changePassword = new BoolField.CLASS<BoolField>(this);
	public TextField.CLASS<TextField> settings = new TextField.CLASS<TextField>(this);
	public StringField.CLASS<StringField> verification = new StringField.CLASS<StringField>(this);
	public DatetimeField.CLASS<DatetimeField> verificationModAt = new DatetimeField.CLASS<DatetimeField>(this);
	
	
	private boolean notifyBlock = false;

	public Users() {
		this(null);
	}

	public Users(IObject container) {
		super(container);
		setTransactive(true);
	}

	@Override
	public void initMembers() {
		super.initMembers();

		objects.add(password);
		objects.add(firstName);
		objects.add(middleName);
		objects.add(lastName);
		objects.add(phone);
		objects.add(email);
		objects.add(company);
		objects.add(position);
		objects.add(banned);
		objects.add(changePassword);
		objects.add(settings);
		objects.add(verification);
		objects.add(verificationModAt);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		name.setDisplayName(displayNames.Login);
		name.get().length = new integer(IAuthorityCenter.MaxLoginLength);
		name.get().unique = bool.True;

		password.setName(fieldNames.Password);
		password.setExportable(false);
		password.setIndex("password");
		password.setSystem(true);
		password.get().length = new integer(IAuthorityCenter.MaxPasswordLength);
		password.get().setDefault(new string(defaultPassword));

		firstName.setName(fieldNames.FirstName);
		firstName.setIndex("firstName");
		firstName.setDisplayName(displayNames.FirstName);
		firstName.get().length = new integer(100);

		middleName.setName(fieldNames.MiddleName);
		middleName.setIndex("middleName");
		middleName.setDisplayName(displayNames.MiddleName);
		middleName.get().length = new integer(100);

		lastName.setName(fieldNames.LastName);
		lastName.setIndex("lastName");
		lastName.setDisplayName(displayNames.LastName);
		lastName.get().length = new integer(100);

		description.setDisplayName(displayNames.Description);

		phone.setName(fieldNames.Phone);
		phone.setIndex("phone");
		phone.setDisplayName(displayNames.Phone);
		phone.get().length = new integer(128);

		email.setName(fieldNames.Email);
		email.setIndex("email");
		email.setDisplayName(displayNames.Email);
		email.get().length = new integer(128);
		
		company.setName(fieldNames.Company);
		company.setIndex("company");
		company.setDisplayName(displayNames.Company);
		company.get().length = new integer(100);
		
		position.setName(fieldNames.Position);
		position.setIndex("position");
		position.setDisplayName(displayNames.Position);
		position.get().length = new integer(100);

		banned.setName(fieldNames.Banned);
		banned.setIndex("banned");
		banned.setDisplayName(displayNames.Banned);

		changePassword.setName(fieldNames.ChangePassword);
		changePassword.setIndex("changePassword");
		changePassword.setDisplayName(displayNames.ChangePassword);

		settings.setName(fieldNames.Settings);
		settings.setIndex("settings");
		settings.setDisplayName(displayNames.Settings);
		
		verification.setName(fieldNames.Verification);
		verification.get().length = new integer(IAuthorityCenter.MaxPasswordLength);
		verification.setIndex("verification");
		
		verificationModAt.setName(fieldNames.VerificationModAt);
		verificationModAt.setIndex("verificationModAt");
	}

	@Override
	public void initStaticRecords() {
		{
			LinkedHashMap<IField, primary> record = new LinkedHashMap<IField, primary>();
			record.put(name.get(), new string(displayNames.AdministratorName));
			record.put(lock.get(), RecordLock.Destroy);
			addRecord(BuiltinUsers.Administrator.guid(), record);
		}
		{
			LinkedHashMap<IField, primary> record = new LinkedHashMap<IField, primary>();
			record.put(name.get(), new string(displayNames.SystemName));
			record.put(lock.get(), RecordLock.Destroy);
			addRecord(BuiltinUsers.System.guid(), record);
		}
	}

	@Override
	public void onNew() {
		super.onNew();
		if(!password.get().changed())
			password.get().set(defaultPassword);
		if(!changePassword.get().changed())
			changePassword.get().set(bool.True);
	}

	@Override
	public void beforeCreate(guid recordId) {
		super.beforeCreate(recordId);

		if (!ApplicationServer.userEventsEnabled())
			return;

		StringField name = this.name.get();
		if((!name.changed() || name.string().isEmpty()) && !recordId.equals(guid.Null))
			name.set(new string(displayNames.DefaultName + name.getSequencer().next()));
		if (this.verification.get().changed())
			this.verificationModAt.get().set(new date());
	}

	@Override
	public void beforeUpdate(guid recordId) {
		super.beforeUpdate(recordId);

		if (!ApplicationServer.userEventsEnabled())
			return;

		if(banned.get().changed() && isSystemUser(recordId)) {
			boolean ban = banned.get().bool().get();
			if(ban && (System.equals(recordId) || Administrator.equals(recordId)))
				throw new exception("Builtin users ban state can not be changed");
		}
		
		if (this.verification.get().changed())
			this.verificationModAt.get().set(new date());
	}

	@Override
	public void afterUpdate(guid recordId) {
		if(!notifyBlock)
			notifyUserChange(recordId, changePassword.get().changed() && changePassword.get().bool().get());
	}

	@Override
	public void beforeDestroy(guid recordId) {
		super.beforeDestroy(recordId);

		if(isSystemUser(recordId))
			throw new exception("Builtin users can not be deleted");
	}

	@Override
	public void afterDestroy(guid recordId) {
		notifyUserChange(recordId);
	}

	static public void saveSettings(guid user, String settings) {
		Users users = new Users.CLASS<Users>().get();
		users.settings.get().set(new string(settings));
		users.notifyBlock = true;
		users.update(user);
	}

	static public void changePassword(guid user, String password) {
		Users users = new Users.CLASS<Users>().get();
		users.password.get().set(new string(password));
		users.changePassword.get().set(bool.False);
		users.update(user);
	}

	static public void resetPassword(guid user) {
		Users users = new Users.CLASS<Users>().get();
		users.password.get().set(new string(defaultPassword));
		users.changePassword.get().set(bool.True);
		users.update(user);
	}

	private boolean isSystemUser(guid recordId) {
		return Administrator.equals(recordId) || System.equals(recordId);
	}

	static public void notifyUserChange(guid user) {
		notifyUserChange(user, false);
	}

	static public void notifyUserChange(guid userId, boolean force) {
		try {
			IUser user = ApplicationServer.getUser();
			if(force || !user.id().equals(userId) && !System.equals(userId) && !Administrator.equals(userId))
				ServerConfig.authorityCenter().userChanged(userId, user.database().schema());
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public boolean getExtraParameters(org.zenframework.z8.server.security.LoginParameters loginParameters, RLinkedHashMap<string, primary> parameters) {
		return z8_getParameters(LoginParameters.newInstance(loginParameters), parameters).get();
	}

	@SuppressWarnings("rawtypes")
	public bool z8_getParameters(guid id, string name, RLinkedHashMap parameters) {
		return bool.True;
	}

	@SuppressWarnings("rawtypes")
	public bool z8_getParameters(LoginParameters.CLASS<? extends LoginParameters> loginParameters, RLinkedHashMap parameters) {
		return z8_getParameters(loginParameters.get().z8_userId(), loginParameters.get().z8_login(), parameters);
	}
}
