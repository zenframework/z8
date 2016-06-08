package org.zenframework.z8.server.base.table.system;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.BoolField;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.Link;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.expressions.Operation;
import org.zenframework.z8.server.db.sql.expressions.Rel;
import org.zenframework.z8.server.db.sql.functions.string.Lower;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.security.User;
import org.zenframework.z8.server.types.sql.sql_bool;
import org.zenframework.z8.server.types.sql.sql_string;

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
		public final static String UserDesc = "SystemDomains.userDesc";
		public final static String Owner = "SystemDomains.owner";
		public final static String ExportUrl = "SystemDomains.exportUrl";
	}

	public static class CLASS<T extends SystemDomains> extends Table.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(SystemDomains.class);
			setName(TableName);
			setDisplayName(Resources.get(strings.Title));
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

		id.setDisplayName(Resources.get(strings.Id));
		id.get().length.set(256);
		id.get().unique.set(true);

		userLink.setName(names.User);
		userLink.setIndex("userLink");
		userLink.setExportable(false);

		users.get().name.setDisplayName(Resources.get(strings.User));
		users.get().description.setDisplayName(Resources.get(strings.UserDesc));

		owner.setName(names.Owner);
		owner.setIndex("owner");
		owner.setDisplayName(Resources.get(strings.Owner));
		userLink.setExportable(false);

		registerDataField(userLink);
		registerDataField(owner);

		registerFormField(id);
		registerFormField(users.get().name);
		registerFormField(users.get().description);
		registerFormField(owner);

		queries.add(users);

		links.add(userLink);
	}

	public Domain getDomain(String address) {
		if (!readFirst(Arrays.<Field> asList(id.get(), users.get().name.get()), getWhere(address)))
			return null;
		return getDomain();
	}

	public boolean isOwner(String address) {
		return readFirst(Arrays.<Field> asList(owner.get()), getWhere(address)) && owner.get().get().bool().get();
	}

	public Collection<Domain> getLocalDomains() {
		read(Arrays.<Field> asList(id.get(), users.get().name.get()), new Rel(owner.get(), Operation.Eq, new sql_bool(true)));
		Collection<Domain> domains = new LinkedList<Domain>();
		while (next()) {
			domains.add(getDomain());
		}
		return domains;
	}

	public Collection<String> getLocalAddresses() {
		read(Arrays.<Field> asList(id.get()), new Rel(owner.get(), Operation.Eq, new sql_bool(true)));
		Collection<String> domains = new LinkedList<String>();
		while (next()) {
			domains.add(id.get().get().string().get());
		}
		return domains;
	}

	private Domain getDomain() {
		return new Domain(recordId(), id.get().get().string().get(), User.load(this.users.get().name.get().string().get()));
	}

	private SqlToken getWhere(String address) {
		return new Rel(new Lower(id.get()), Operation.Eq, new sql_string(address.toLowerCase()));
	}

}
