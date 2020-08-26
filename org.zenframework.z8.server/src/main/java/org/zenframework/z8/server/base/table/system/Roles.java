package org.zenframework.z8.server.base.table.system;

import java.util.*;

import org.zenframework.z8.server.base.query.RecordLock;
import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.BoolField;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.db.sql.expressions.Equ;
import org.zenframework.z8.server.engine.Runtime;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.security.Access;
import org.zenframework.z8.server.security.IAccess;
import org.zenframework.z8.server.security.IRole;
import org.zenframework.z8.server.security.Role;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.exception;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;

public class Roles extends Table {
	final static public String TableName = "SystemRoles";

	static public class fieldNames {
		public final static String Role = "Role";
		public final static String Table = "Table";
		public final static String Read = "Read";
		public final static String Write = "Write";
		public final static String Create = "Create";
		public final static String Copy = "Copy";
		public final static String Destroy = "Destroy";
		public final static String Execute = "Execute";
	}

	static public class strings {
		public final static String Title = "Roles.title";
		public final static String Name = "Roles.name";
		public final static String Read = "Roles.read";
		public final static String Write = "Roles.write";
		public final static String Create = "Roles.create";
		public final static String Copy = "Roles.copy";
		public final static String Destroy = "Roles.destroy";
		public final static String Execute = "Roles.execute";
	}

	static public class displayNames {
		public final static String Title = Resources.get(strings.Title);
		public final static String Name = Resources.get(strings.Name);
		public final static String Read = Resources.get(strings.Read);
		public final static String Write = Resources.get(strings.Write);
		public final static String Create = Resources.get(strings.Create);
		public final static String Copy = Resources.get(strings.Copy);
		public final static String Destroy = Resources.get(strings.Destroy);
		public final static String Execute = Resources.get(strings.Execute);
	}

	static public class apiAttrs {
		public final static String Title = Resources.getOrNull(strings.Title + ".APIDescription");
		public final static String Name = Resources.getOrNull(strings.Name + ".APIDescription");
		public final static String Read = Resources.getOrNull(strings.Read + ".APIDescription");
		public final static String Write = Resources.getOrNull(strings.Write + ".APIDescription");
		public final static String Create = Resources.getOrNull(strings.Create + ".APIDescription");
		public final static String Copy = Resources.getOrNull(strings.Copy + ".APIDescription");
		public final static String Destroy = Resources.getOrNull(strings.Destroy + ".APIDescription");
		public final static String Execute = Resources.getOrNull(strings.Execute + ".APIDescription");
	}

	static public guid User = Role.User;
	static public guid Guest = Role.Guest;
	static public guid Administrator = Role.Administrator;

	public BoolField.CLASS<BoolField> read = new BoolField.CLASS<BoolField>(this);
	public BoolField.CLASS<BoolField> write = new BoolField.CLASS<BoolField>(this);
	public BoolField.CLASS<BoolField> create = new BoolField.CLASS<BoolField>(this);
	public BoolField.CLASS<BoolField> copy = new BoolField.CLASS<BoolField>(this);
	public BoolField.CLASS<BoolField> destroy = new BoolField.CLASS<BoolField>(this);
	public BoolField.CLASS<BoolField> execute = new BoolField.CLASS<BoolField>(this);

	public static class CLASS<T extends Roles> extends Table.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(Roles.class);
			setName(Roles.TableName);
			setDisplayName(displayNames.Title);
			Optional.ofNullable(apiAttrs.Title)
					.ifPresent(attrVal -> setAttribute("APIDescription", attrVal));
		}

		@Override
		public Object newObject(IObject container) {
			return new Roles(container);
		}
	}

	public Roles(IObject container) {
		super(container);
	}

	@Override
	public void initMembers() {
		super.initMembers();

		objects.add(read);
		objects.add(write);
		objects.add(create);
		objects.add(copy);
		objects.add(destroy);
		objects.add(execute);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		name.setDisplayName(displayNames.Name);
		name.get().length = new integer(50);
		Optional.ofNullable(apiAttrs.Name)
				.ifPresent(attrVal -> name.setAttribute("APIDescription", attrVal));

		IAccess access = Access.guest();

		read.setName(fieldNames.Read);
		read.setIndex("read");
		read.setDisplayName(displayNames.Read);
		read.get().setDefault(new bool(access.read()));
		Optional.ofNullable(apiAttrs.Read)
				.ifPresent(attrVal -> read.setAttribute("APIDescription", attrVal));

		write.setName(fieldNames.Write);
		write.setIndex("write");
		write.setDisplayName(displayNames.Write);
		write.get().setDefault(new bool(access.write()));
		Optional.ofNullable(apiAttrs.Write)
				.ifPresent(attrVal -> write.setAttribute("APIDescription", attrVal));

		create.setName(fieldNames.Create);
		create.setIndex("create");
		create.setDisplayName(displayNames.Create);
		create.get().setDefault(new bool(access.create()));
		Optional.ofNullable(apiAttrs.Create)
				.ifPresent(attrVal -> create.setAttribute("APIDescription", attrVal));

		copy.setName(fieldNames.Copy);
		copy.setIndex("copy");
		copy.setDisplayName(displayNames.Copy);
		copy.get().setDefault(new bool(access.copy()));
		Optional.ofNullable(apiAttrs.Copy)
				.ifPresent(attrVal -> copy.setAttribute("APIDescription", attrVal));

		destroy.setName(fieldNames.Destroy);
		destroy.setIndex("destroy");
		destroy.setDisplayName(displayNames.Destroy);
		destroy.get().setDefault(new bool(access.destroy()));
		Optional.ofNullable(apiAttrs.Destroy)
				.ifPresent(attrVal -> destroy.setAttribute("APIDescription", attrVal));

		execute.setName(fieldNames.Execute);
		execute.setIndex("execute");
		execute.setDisplayName(displayNames.Execute);
		execute.get().setDefault(new bool(access.execute()));
		Optional.ofNullable(apiAttrs.Execute)
				.ifPresent(attrVal -> execute.setAttribute("APIDescription", attrVal));

		this.setTransactive(true);
	}

	@Override
	public void initStaticRecords() {
		initStaticRecord(Role.displayNames.Administrator, Role.administrator());
		initStaticRecord(Role.displayNames.User, Role.user());
		initStaticRecord(Role.displayNames.Guest, Role.guest());
	}

	private void initStaticRecord(String displayName, IRole role) {
		IAccess access = role.access();

		LinkedHashMap<IField, primary> values = new LinkedHashMap<IField, primary>();
		values.put(name.get(), new string(displayName));
		values.put(description.get(), new string(displayName));
		values.put(read.get(), new bool(access.read()));
		values.put(write.get(), new bool(access.write()));
		values.put(create.get(), new bool(access.create()));
		values.put(copy.get(), new bool(access.copy()));
		values.put(destroy.get(), new bool(access.destroy()));
		values.put(execute.get(), new bool(access.execute()));
		values.put(lock.get(), RecordLock.Destroy);
		addRecord(role.id(), values);
	}

	@Override
	public void z8_onCopy(guid recordId) {
		super.z8_onCopy(recordId);
		lock.get().set(RecordLock.None);
	}

	@Override
	public void z8_afterCreate(guid recordId) {
		super.z8_afterCreate(recordId);

		RoleTableAccess rta = new RoleTableAccess.CLASS<RoleTableAccess>().get();

		for(guid table : Runtime.instance().tableKeys()) {
			rta.role.get().set(recordId);
			rta.table.get().set(table);
			rta.create();
		}

		Fields fields = new Fields.CLASS<Fields>().get();
		RoleFieldAccess rfa = new RoleFieldAccess.CLASS<RoleFieldAccess>().get();

		fields.read(Arrays.asList(fields.primaryKey()));

		while(fields.next()) {
			rfa.role.get().set(recordId);
			rfa.field.get().set(fields.recordId());
			rfa.create();
		}

		Requests requests = new Requests.CLASS<Requests>().get();
		RoleRequestAccess rra = new RoleRequestAccess.CLASS<RoleRequestAccess>().get();

		requests.read(Arrays.asList(requests.primaryKey()));

		while(requests.next()) {
			rra.role.get().set(recordId);
			rra.request.get().set(requests.recordId());
			rra.create();
		}
}

	@Override
	public void z8_beforeDestroy(guid recordId) {
		super.z8_beforeDestroy(recordId);

		if(Role.Administrator.equals(recordId) || Role.User.equals(recordId) ||
				Role.Guest.equals(recordId))
			throw new exception("Builtin roles can not be deleted");

		RoleFieldAccess rfa = new RoleFieldAccess.CLASS<RoleFieldAccess>().get();
		rfa.destroy(new Equ(rfa.role.get(), recordId));

		RoleTableAccess rta = new RoleTableAccess.CLASS<RoleTableAccess>().get();
		rta.destroy(new Equ(rta.role.get(), recordId));

		RoleRequestAccess rra = new RoleRequestAccess.CLASS<RoleRequestAccess>().get();
		rra.destroy(new Equ(rra.role.get(), recordId));
	}

	public Collection<IRole> get() {
		Field read = this.read.get();
		Field write = this.write.get();
		Field create = this.create.get();
		Field copy = this.copy.get();
		Field destroy = this.destroy.get();
		Field execute = this.execute.get();

		read(Arrays.asList(read, write, create, copy, destroy, execute));

		Collection<IRole> roles = new ArrayList<IRole>();

		while(next()) {
			IAccess access = new Access();
			access.setRead(read.bool().get());
			access.setWrite(write.bool().get());
			access.setCreate(create.bool().get());
			access.setCopy(copy.bool().get());
			access.setDestroy(destroy.bool().get());
			access.setExecute(execute.bool().get());

			roles.add(new Role(recordId(), access));
		}

		return roles;
	}

	static public void notifyRoleChange(guid role) {
		try {
			ServerConfig.authorityCenter().roleChanged(role);
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}

}
