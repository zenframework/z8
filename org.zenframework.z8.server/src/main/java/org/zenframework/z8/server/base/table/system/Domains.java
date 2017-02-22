package org.zenframework.z8.server.base.table.system;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.BoolField;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.Link;
import org.zenframework.z8.server.base.table.value.StringField;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.expressions.And;
import org.zenframework.z8.server.db.sql.expressions.Is;
import org.zenframework.z8.server.db.sql.functions.string.EqualsIgnoreCase;
import org.zenframework.z8.server.engine.Rmi;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IClass;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.security.Domain;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.string;

public class Domains extends Table {

	static public string DefaultDomain = new string(Users.displayNames.SystemName + " at " + Rmi.localhost);
	static public String TableName = "SystemDomains";

	static public class fieldNames {
		public final static String Address = "Address";
		public final static String User = "User";
		public final static String Owner = "Owner";
	}

	static public class strings {
		public final static String Title = "Domains.title";
		public final static String Name = "Domains.name";
		public final static String Address = "Domains.address";
		public final static String User = "Domains.user";
		public final static String UserDescription = "Domains.user.description";
		public final static String Owner = "Domains.owner";

		public final static String DefaultAddress = "Domains.address.default";
	}

	static public class displayNames {
		public final static String Title = Resources.get(strings.Title);
		public final static String Name = Resources.get(strings.Name);
		public final static String Address = Resources.get(strings.Address);
		public final static String User = Resources.get(strings.User);
		public final static String UserDescription = Resources.get(strings.UserDescription);
		public final static String Owner = Resources.get(strings.Owner);

		public final static String DefaultAddress = Resources.get(strings.DefaultAddress);
	}

	public static class CLASS<T extends Domains> extends Table.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(Domains.class);
			setName(TableName);
			setDisplayName(displayNames.Title);
		}

		@Override
		public Object newObject(IObject container) {
			return new Domains(container);
		}
	}

	public final Users.CLASS<Users> users = new Users.CLASS<Users>(this);

	public final StringField.CLASS<StringField> address = new StringField.CLASS<StringField>(this);
	public final Link.CLASS<Link> userLink = new Link.CLASS<Link>(this);
	public final BoolField.CLASS<BoolField> owner = new BoolField.CLASS<BoolField>(this);

	static public Domains newInstance() {
		return new Domains.CLASS<Domains>().get();
	}

	public Domains(IObject container) {
		super(container);
	}

	@Override
	public void constructor1() {
		userLink.get(IClass.Constructor1).operatorAssign(users);
	}

	@Override
	public void initMembers() {
		super.initMembers();

		objects.add(address);
		objects.add(userLink);
		objects.add(owner);

		objects.add(users);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		users.setIndex("users");

		name.setDisplayName(displayNames.Name);

		address.setIndex("address");
		address.setName(fieldNames.Address);
		address.setDisplayName(displayNames.Address);
		address.get().length = new integer(256);
		address.get().unique = bool.True;

		userLink.setName(fieldNames.User);
		userLink.setIndex("userLink");
		userLink.setExportable(false);

		users.get().name.setDisplayName(displayNames.User);
		users.get().description.setDisplayName(displayNames.UserDescription);

		owner.setIndex("owner");
		owner.setName(fieldNames.Owner);
		owner.setDisplayName(displayNames.Owner);
		owner.setExportable(false);

		registerControl(name);
		registerControl(address);
		registerControl(description);
		registerControl(users.get().name);
		registerControl(users.get().description);
		registerControl(owner);
	}

	@Override
	public void onNew(guid recordId, guid parentId) {
		super.onNew(recordId, parentId);

		Field address = this.address.get();
		address.set(new string(displayNames.DefaultAddress + address.getSequencer().next()));
	}

	public Domain getDomain(String domain) {
		Field address = this.address.get();
		Field user = this.userLink.get();
		Field owner = this.owner.get();

		SqlToken where = new EqualsIgnoreCase(address, domain);

		if(!readFirst(Arrays.<Field>asList(address, user, owner), where))
			return Domain.system();

		return new Domain(address.string(), user.guid(), owner.bool());
	}

	public boolean isOwner(String domain) {
		Field owner = this.owner.get();
		Field address = this.address.get();

		SqlToken isOwner = new Is(owner);
		SqlToken addressEq = new EqualsIgnoreCase(address, domain);
		SqlToken where = new And(isOwner, addressEq);

		return count(where) == 1;
	}

	public Collection<Domain> getLocalDomains() {
		Field address = this.address.get();
		Field user = userLink.get();
		Field owner = this.owner.get();

		read(Arrays.<Field>asList(address, user, owner), new Is(owner));

		Collection<Domain> domains = new ArrayList<Domain>();

		domains.add(Domain.system());

		while(next()) {
			Domain domain = new Domain(address.string(), user.guid(), owner.bool());
			domains.add(domain);
		}

		return domains;
	}

	public Collection<String> getAddresses() {
		Collection<Domain> domains = getLocalDomains();

		Collection<String> result = new ArrayList<String>();

		for(Domain domain : domains)
			result.add(domain.getAddress());

		return result;
	}
}
