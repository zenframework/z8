package org.zenframework.z8.server.base.table.system;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.BoolField;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.Link;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.expressions.And;
import org.zenframework.z8.server.db.sql.expressions.Is;
import org.zenframework.z8.server.db.sql.functions.string.EqualsIgnoreCase;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.integer;

public class SystemDomains extends Table {

	public static final String TableName = "SystemDomains";

	static public class names {
		public final static String User = "UserId";
		public final static String Owner = "Owner";
	}

	static public class strings {
		public final static String Title = "SystemDomains.title";
		public final static String Id = "SystemDomains.id";
		public final static String User = "SystemDomains.user";
		public final static String UserDescription = "SystemDomains.userDescription";
		public final static String Owner = "SystemDomains.owner";
	}

	static public class displayNames {
		public final static String Title = Resources.get(strings.Title);
		public final static String Id = Resources.get(strings.Id);
		public final static String User = Resources.get(strings.User);
		public final static String UserDescription = Resources.get(strings.UserDescription);
		public final static String Owner = Resources.get(strings.Owner);
	}
	
	public static class CLASS<T extends SystemDomains> extends Table.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(SystemDomains.class);
			setName(TableName);
			setDisplayName(displayNames.Title);
		}

		@Override
		public Object newObject(IObject container) {
			return new SystemDomains(container);
		}
	}

	public final Users.CLASS<Users> users = new Users.CLASS<Users>(this);
	public final Link.CLASS<Link> userLink = new Link.CLASS<Link>(this);
	public final BoolField.CLASS<BoolField> owner = new BoolField.CLASS<BoolField>(this);

	static public SystemDomains newInstance() {
		return new SystemDomains.CLASS<SystemDomains>().get();
	}

	public SystemDomains(IObject container) {
		super(container);
	}

	@Override
	public void constructor1() {
		userLink.get(CLASS.Constructor1).operatorAssign(users);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		users.setIndex("users");

		id.setDisplayName(displayNames.Id);
		id.get().length = new integer(256);
		id.get().unique = new bool(true);

		userLink.setName(names.User);
		userLink.setIndex("userLink");
		userLink.setExportable(false);

		users.get().name.setDisplayName(displayNames.User);
		users.get().description.setDisplayName(displayNames.UserDescription);

		owner.setName(names.Owner);
		owner.setIndex("owner");
		owner.setDisplayName(displayNames.Owner);
		owner.setExportable(false);

		registerDataField(userLink);
		registerDataField(owner);

		registerFormField(id);
		registerFormField(users.get().name);
		registerFormField(users.get().description);
		registerFormField(owner);

		queries.add(users);

		links.add(userLink);
	}

	public Domain getDomain(String name) {
		Field id = this.id.get();
		Field login = users.get().name.get();
		
		SqlToken where = new EqualsIgnoreCase(id, name);
		
		if (!readFirst(Arrays.<Field> asList(id, login), where))
			return null;
		
		return new Domain(id.string(), login.string());
	}

	public boolean isOwner(String name) {
		Field owner = this.owner.get();
		Field id = this.id.get();
		
		SqlToken isOwner = new Is(owner);
		SqlToken nameEq = new EqualsIgnoreCase(id, name);
		SqlToken where = new And(isOwner, nameEq);
		
		return count(where) == 1;
	}

	public Collection<Domain> getLocalDomains() {
		Field id = this.id.get();
		Field login = users.get().name.get();
		Field owner = this.owner.get();
		
		read(Arrays.<Field> asList(id, login), new Is(owner));
		
		Collection<Domain> domains = new ArrayList<Domain>();
		
		while (next()) {
			Domain domain = new Domain(id.string(), login.string());
			domains.add(domain);
		}
		return domains;
	}

	public Collection<String> getNames() {
		Collection<Domain> domains = getLocalDomains();
		
		Collection<String> result = new ArrayList<String>();
		
		for(Domain domain : domains)
			result.add(domain.getName());
		
		return result;
	}
}
