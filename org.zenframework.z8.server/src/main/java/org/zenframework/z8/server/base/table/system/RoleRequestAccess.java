package org.zenframework.z8.server.base.table.system;

import java.util.Arrays;

import org.zenframework.z8.server.base.query.RecordLock;
import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.BoolField;
import org.zenframework.z8.server.base.table.value.Link;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IClass;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.guid;

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

	public Roles.CLASS<Roles> role = new Roles.CLASS<Roles>(this);
	public Requests.CLASS<Requests> request = new Requests.CLASS<Requests>(this);

	public Link.CLASS<Link> roleId = new Link.CLASS<Link>(this);
	public Link.CLASS<Link> requestId = new Link.CLASS<Link>(this);

	public BoolField.CLASS<BoolField> execute = new BoolField.CLASS<BoolField>(this);

	public RoleRequestAccess() {
		this(null);
	}

	public RoleRequestAccess(IObject container) {
		super(container);
	}

	@Override
	public void constructor1() {
		roleId.get(IClass.Constructor1).operatorAssign(role);
		requestId.get(IClass.Constructor1).operatorAssign(request);
	}

	@Override
	public void initMembers() {
		super.initMembers();

		objects.add(roleId);
		objects.add(requestId);

		objects.add(execute);

		objects.add(role);
		objects.add(request);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		lock.get().setDefault(RecordLock.Destroy);

		role.setIndex("role");
		request.setIndex("request");

		roleId.setName(fieldNames.Role);
		roleId.setIndex("roleId");

		requestId.setName(fieldNames.Request);
		requestId.setIndex("requestId");

		execute.setName(fieldNames.Execute);
		execute.setIndex("execute");
		execute.setDisplayName(displayNames.Execute);
	}

	@Override
	public void onUpdateAction(guid recordId) {
		super.onUpdateAction(recordId);

		if(readRecord(recordId, Arrays.asList(roleId.get())))
			Roles.notifyRoleChange(roleId.get().guid());
	}
}
