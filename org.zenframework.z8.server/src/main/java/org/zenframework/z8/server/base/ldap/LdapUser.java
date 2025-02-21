package org.zenframework.z8.server.base.ldap;

import java.util.Map;

import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.runtime.RLinkedHashMap;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.string;

public class LdapUser extends OBJECT {
	public static class CLASS<T extends LdapUser> extends OBJECT.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(LdapUser.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new LdapUser(container);
		}
	}

	public string domainName = new string();
	public string login = new string();
	public RCollection<string> memberOf = new RCollection<string>();
	public bool locked = new bool();
	public RLinkedHashMap<string, string> parameters = new RLinkedHashMap<string, string>();

	public LdapUser(IObject container) {
		super(container);
	}

	public static LdapUser.CLASS<LdapUser> newInstance(org.zenframework.z8.server.ldap.LdapUser user) {
		if (user == null)
			return null;

		LdapUser.CLASS<LdapUser> ldapUser = new LdapUser.CLASS<LdapUser>(null);

		ldapUser.get().domainName = new string(user.getDomainName());
		ldapUser.get().login = new string(user.getLogin());
		ldapUser.get().locked = new bool(user.isLocked());

		for (String memberOf : user.getMemberOf())
			ldapUser.get().memberOf.add(new string(memberOf));

		for (Map.Entry<String, String> entry : user.getParameters().entrySet())
			ldapUser.get().parameters.put(new string(entry.getKey()), new string(entry.getValue()));

		return ldapUser;
	}
}
