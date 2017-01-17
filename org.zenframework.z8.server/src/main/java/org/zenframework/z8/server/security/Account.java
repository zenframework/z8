package org.zenframework.z8.server.security;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;

import org.zenframework.z8.server.base.table.site.Accounts;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.expressions.Equ;
import org.zenframework.z8.server.db.sql.functions.string.EqualsIgnoreCase;
import org.zenframework.z8.server.engine.RmiIO;
import org.zenframework.z8.server.exceptions.AccessDeniedException;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.utils.IOUtils;
import org.zenframework.z8.server.utils.NumericUtils;

public class Account implements IAccount {
	private static final long serialVersionUID = -1028802521806145513L;

	private guid id;

	private String login;
	private String password;

	private String firstName;
	private String lastName;

	private boolean banned;

	private IUser user;

	public Account() {
	}

	@Override
	public guid id() {
		return id;
	}

	@Override
	public String login() {
		return login;
	}

	@Override
	public String password() {
		return password;
	}

	@Override
	public String firstName() {
		return firstName;
	}

	@Override
	public String lastName() {
		return lastName;
	}

	@Override
	public boolean banned() {
		return banned;
	}

	@Override
	public IUser user() {
		return user;
	}

	static public IAccount load(String login) {
		return load(new string(login));
	}

	static public IAccount load(guid account) {
		return load((primary)account);
	}

	static public IAccount load(string login) {
		return load((primary)login);
	}

	static private IAccount load(primary loginOrId) {
		if(!ServerConfig.isSystemInstalled())
			return null;

		Account account = new Account();

		if(loginOrId instanceof string)
			account.readInfo((string)loginOrId);
		else
			account.readInfo((guid)loginOrId);

		ConnectionManager.release();

		return account;
	}

	static public IAccount load(String login, String password) {
		IAccount account = load(login);

		if(password == null || !password.equals(account.password()) || account.banned())
			throw new AccessDeniedException();

		return account;
	}

	private void readInfo(guid id) {
		Accounts accounts = Accounts.newInstance();
		SqlToken where = new Equ(accounts.recordId.get(), id);
		readInfo(accounts, where);
	}

	private void readInfo(string login) {
		Accounts accounts = Accounts.newInstance();
		SqlToken where = new EqualsIgnoreCase(accounts.login.get(), login);
		readInfo(accounts, where);
	}

	private void readInfo(Accounts accounts, SqlToken where) {
		Collection<Field> fields = new ArrayList<Field>();
		fields.add(accounts.recordId.get());
		fields.add(accounts.login.get());
		fields.add(accounts.password.get());

		fields.add(accounts.banned.get());
		fields.add(accounts.firstName.get());
		fields.add(accounts.lastName.get());
		fields.add(accounts.user.get());

		accounts.read(fields, where);

		if(accounts.next()) {
			this.id = accounts.recordId.get().guid();
			this.login = accounts.login.get().string().get();
			this.password = accounts.password.get().string().get();
			this.banned = accounts.banned.get().bool().get();
			this.firstName = accounts.firstName.get().string().get();
			this.lastName = accounts.lastName.get().string().get();
			this.user = User.read(accounts.user.get().guid());
		} else
			throw new AccessDeniedException();
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		serialize(out);
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		deserialize(in);
	}

	@Override
	public void serialize(ObjectOutputStream out) throws IOException {
		RmiIO.writeLong(out, serialVersionUID);

		ByteArrayOutputStream bytes = new ByteArrayOutputStream(32 * NumericUtils.Kilobyte);
		ObjectOutputStream objects = new ObjectOutputStream(bytes);

		RmiIO.writeGuid(objects, id);

		RmiIO.writeString(objects, login);
		RmiIO.writeString(objects, password);

		RmiIO.writeString(objects, firstName);
		RmiIO.writeString(objects, lastName);

		RmiIO.writeBoolean(objects, banned);

		objects.writeObject(user);

		objects.close();

		RmiIO.writeBytes(out, IOUtils.zip(bytes.toByteArray()));
	}

	@Override
	public void deserialize(ObjectInputStream in) throws IOException, ClassNotFoundException {
		@SuppressWarnings("unused")
		long version = RmiIO.readLong(in);

		ByteArrayInputStream bytes = new ByteArrayInputStream(IOUtils.unzip(RmiIO.readBytes(in)));
		ObjectInputStream objects = new ObjectInputStream(bytes);

		id = RmiIO.readGuid(objects);

		login = RmiIO.readString(objects);
		password = RmiIO.readString(objects);

		firstName = RmiIO.readString(objects);
		lastName = RmiIO.readString(objects);

		banned = RmiIO.readBoolean(objects);

		user = (IUser)objects.readObject();

		objects.close();
	}
}
