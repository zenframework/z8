package org.zenframework.z8.server.base.table.site;

import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.system.Users;
import org.zenframework.z8.server.base.table.value.BoolField;
import org.zenframework.z8.server.base.table.value.Link;
import org.zenframework.z8.server.base.table.value.StringField;
import org.zenframework.z8.server.crypto.MD5;
import org.zenframework.z8.server.engine.IAuthorityCenter;
import org.zenframework.z8.server.engine.Runtime;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IClass;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.string;

public class Accounts extends Table {
	final static public String TableName = "SiteAccounts";

	static private String defaultPassword = MD5.get("");

	static public class names {
		public final static String Login = "Login";
		public final static String Password = "Password";
		public final static String FirstName = "First Name";
		public final static String LastName = "Last Name";
		public final static String Banned = "Banned";
		public final static String User = "User";
	}

	static public class strings {
		public final static String Title = "SiteAccounts.title";
		public final static String Login = "SiteAccounts.login";
		public final static String FirstName = "SiteAccounts.firstName";
		public final static String LastName = "SiteAccounts.lastName";
		public final static String Banned = "SiteAccounts.banned";
	}

	static public class displayNames {
		public final static String Title = Resources.get(strings.Title);
		public final static String Login = Resources.get(strings.Login);
		public final static String FirstName = Resources.get(strings.FirstName);
		public final static String LastName = Resources.get(strings.LastName);
		public final static String Banned = Resources.get(strings.Banned);
	}

	public static class CLASS<T extends Accounts> extends Table.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(Accounts.class);
			setName(TableName);
			setDisplayName(displayNames.Title);
		}

		@Override
		public Object newObject(IObject container) {
			return new Accounts(container);
		}
	}

	static public Accounts newInstance() {
		return (Accounts)Runtime.instance().getTableByName(Accounts.TableName).newInstance();
	}

	public Users.CLASS<Users> users = new Users.CLASS<Users>(this);

	public StringField.CLASS<StringField> login = new StringField.CLASS<StringField>(this);
	public StringField.CLASS<StringField> password = new StringField.CLASS<StringField>(this);
	public StringField.CLASS<StringField> firstName = new StringField.CLASS<StringField>(this);
	public StringField.CLASS<StringField> lastName = new StringField.CLASS<StringField>(this);
	public BoolField.CLASS<BoolField> banned = new BoolField.CLASS<BoolField>(this);

	public Link.CLASS<Link> user = new Link.CLASS<Link>(this);

	public Accounts() {
		this(null);
	}

	public Accounts(IObject container) {
		super(container);
	}

	@Override
	public void constructor1() {
		user.get(IClass.Constructor1).operatorAssign(users);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		users.setIndex("users");

		user.setName(names.User);
		user.setIndex("user");

		login.setName(names.Login);
		login.setIndex("login");
		login.setDisplayName(displayNames.Login);
		login.get().length = new integer(IAuthorityCenter.MaxLoginLength);
		login.get().unique = bool.True;

		password.setName(names.Password);
		password.setExportable(false);
		password.setIndex("password");
		password.setSystem(true);
		password.get().length = new integer(IAuthorityCenter.MaxPasswordLength);
		password.get().setDefault(new string(defaultPassword));

		firstName.setName(names.FirstName);
		firstName.setIndex("firstName");
		firstName.setDisplayName(displayNames.FirstName);
		firstName.get().length = new integer(100);

		lastName.setName(names.LastName);
		lastName.setIndex("lastName");
		lastName.setDisplayName(displayNames.LastName);
		lastName.get().length = new integer(100);

		banned.setName(names.Banned);
		banned.setIndex("banned");
		banned.setDisplayName(displayNames.Banned);

		registerDataField(login);
		registerDataField(password);
		registerDataField(firstName);
		registerDataField(lastName);
		registerDataField(banned);
		registerDataField(user);

		queries.add(users);
	}
}
