package org.zenframework.z8.server.base.table.system;

import org.zenframework.z8.server.base.query.RecordLock;
import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.BoolField;
import org.zenframework.z8.server.base.table.value.Link;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IClass;
import org.zenframework.z8.server.runtime.IObject;

public class RoleRequestAccess extends Table {
	final static public String TableName = "SystemRoleRequestAccess";

	static public class fieldNames {
		public final static String Role = "Role";
		public final static String Request = "Request";
		public final static String Execute = "Execute";
	}

	static public class strings {
		public final static String Title = "RoleRequestAccess.title";
		public final static String Execute = "RoleRequestAccess.execute";
	}

	static public class displayNames {
		public final static String Title = Resources.get(strings.Title);
		public final static String Execute = Resources.get(strings.Execute);
	}

	public static class CLASS<T extends RoleRequestAccess> extends Table.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(RoleRequestAccess.class);
			setName(TableName);
			setDisplayName(displayNames.Title);
		}

		@Override
		public Object newObject(IObject container) {
			return new RoleRequestAccess(container);
		}
	}

	public Roles.CLASS<Roles> roles = new Roles.CLASS<Roles>(this);
	public Requests.CLASS<Requests> requests = new Requests.CLASS<Requests>(this);

	public Link.CLASS<Link> role = new Link.CLASS<Link>(this);
	public Link.CLASS<Link> request = new Link.CLASS<Link>(this);

	public BoolField.CLASS<BoolField> execute = new BoolField.CLASS<BoolField>(this);

	public RoleRequestAccess() {
		this(null);
	}

	public RoleRequestAccess(IObject container) {
		super(container);
	}

	@Override
	public void constructor1() {
		role.get(IClass.Constructor1).operatorAssign(roles);
		request.get(IClass.Constructor1).operatorAssign(requests);
	}

	@Override
	public void initMembers() {
		super.initMembers();

		objects.add(role);
		objects.add(request);

		objects.add(execute);

		objects.add(roles);
		objects.add(requests);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		lock.get().setDefault(RecordLock.Destroy);

		roles.setIndex("roles");
		requests.setIndex("requests");

		role.setName(fieldNames.Role);
		role.setIndex("role");

		request.setName(fieldNames.Request);
		request.setIndex("request");

		execute.setName(fieldNames.Execute);
		execute.setIndex("execute");
		execute.setDisplayName(displayNames.Execute);
	}
}
