package org.zenframework.z8.server.base.table.system;

import org.zenframework.z8.server.base.query.RecordLock;
import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.BoolField;
import org.zenframework.z8.server.base.table.value.Link;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IClass;
import org.zenframework.z8.server.runtime.IObject;

import java.util.Optional;

public class RoleRequestAccess extends Table {
	final static public String TableName = "SystemRoleRequestAccess";

	static public class fieldNames {
		public final static String Role = "Role";
		public final static String Request = "Request";
		public final static String Execute = "Execute";
	}

	static public class strings {
		public final static String Title = "RoleRequestAccess.title";
		public final static String Role = "RoleRequestAccess.role";
		public final static String Request = "RoleRequestAccess.request";
		public final static String Execute = "RoleRequestAccess.execute";
	}

	static public class displayNames {
		public final static String Title = Resources.get(strings.Title);
		public final static String Execute = Resources.get(strings.Execute);
	}

	static public class apiAttrs {
		public final static String Title = Resources.getOrNull(strings.Title + ".APIDescription");
		public final static String Role = Resources.getOrNull(strings.Role + ".APIDescription");
		public final static String Request = Resources.getOrNull(strings.Request + ".APIDescription");
		public final static String Execute = Resources.getOrNull(strings.Execute + ".APIDescription");
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
			Optional.ofNullable(apiAttrs.Title)
					.ifPresent(attrVal -> setAttribute("APIDescription", attrVal));
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
		Optional.ofNullable(apiAttrs.Role)
				.ifPresent(attrVal -> role.setAttribute("APIDescription", attrVal));

		request.setName(fieldNames.Request);
		request.setIndex("request");
		Optional.ofNullable(apiAttrs.Request)
				.ifPresent(attrVal -> request.setAttribute("APIDescription", attrVal));

		execute.setName(fieldNames.Execute);
		execute.setIndex("execute");
		execute.setDisplayName(displayNames.Execute);
		Optional.ofNullable(apiAttrs.Execute)
				.ifPresent(attrVal -> execute.setAttribute("APIDescription", attrVal));
	}
}
