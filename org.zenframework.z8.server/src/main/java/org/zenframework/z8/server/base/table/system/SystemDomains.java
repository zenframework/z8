package org.zenframework.z8.server.base.table.system;

import java.util.ArrayList;
import java.util.Collection;

import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.BoolField;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.Link;
import org.zenframework.z8.server.base.table.value.StringExpression;
import org.zenframework.z8.server.base.table.value.StringField;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.expressions.Operation;
import org.zenframework.z8.server.db.sql.expressions.Rel;
import org.zenframework.z8.server.db.sql.functions.string.Lower;
import org.zenframework.z8.server.ie.Export;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.security.IUser;
import org.zenframework.z8.server.security.User;
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

	public static class ExportUrlExpression extends StringExpression {

		public static class CLASS<T extends ExportUrlExpression> extends StringExpression.CLASS<T> {
			public CLASS() {
				this(null);
			}

			public CLASS(IObject container) {
				super(container);
				setJavaClass(ExportUrlExpression.class);
			}

			@Override
			public Object newObject(IObject container) {
				return new ExportUrlExpression(container);
			}
		}

		public ExportUrlExpression(IObject container) {
			super(container);
		}

		@Override
		protected sql_string z8_expression() {
			SystemDomains container = (SystemDomains) this.getContainer();
			return container.owner.get().sql_bool()
					.z8_IIF(new sql_string(Export.LOCAL_PROTOCOL), new sql_string(Export.REMOTE_PROTOCOL))
					.operatorAdd(new sql_string(":")).operatorAdd(container.id.get().sql_string());
		}

	}

	public final Users.CLASS<Users> users = new Users.CLASS<Users>(this);
	public final Link.CLASS<Link> userLink = new Link.CLASS<Link>(this);
	public final BoolField.CLASS<BoolField> owner = new BoolField.CLASS<BoolField>(this);
	public final ExportUrlExpression.CLASS<ExportUrlExpression> exportUrl = new ExportUrlExpression.CLASS<ExportUrlExpression>(
			this);

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

		exportUrl.setIndex("exportUrl");
		exportUrl.setDisplayName(Resources.get(strings.ExportUrl));

		registerDataField(userLink);
		registerDataField(owner);
		registerDataField(exportUrl);

		registerFormField(id);
		registerFormField(users.get().name);
		registerFormField(users.get().description);
		registerFormField(owner);
		registerFormField(exportUrl);

		queries.add(users);

		links.add(userLink);
	}

	static public IUser getDefaultUser(String address) {
		SystemDomains domains = newInstance();

		StringField name = domains.users.get().name.get();
		StringField id = domains.id.get();

		Collection<Field> fields = new ArrayList<Field>();
		fields.add(name);

		SqlToken where = new Rel(new Lower(id), Operation.Eq, new sql_string(address.toLowerCase()));

		if (!domains.readFirst(fields, where))
			return null;

		return User.load(name.string().get());
	}
}
