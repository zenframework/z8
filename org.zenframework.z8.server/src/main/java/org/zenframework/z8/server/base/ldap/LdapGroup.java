package org.zenframework.z8.server.base.ldap;

import java.util.Map;

import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.runtime.RLinkedHashMap;
import org.zenframework.z8.server.types.string;

public class LdapGroup extends OBJECT {
	public static class CLASS<T extends LdapGroup> extends OBJECT.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(LdapGroup.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new LdapGroup(container);
		}
	}

	public string domainName = new string();
	public string name = new string();
	public RCollection<string> members = new RCollection<string>();
	public RLinkedHashMap<string, string> parameters = new RLinkedHashMap<string, string>();

	public LdapGroup(IObject container) {
		super(container);
	}

	public static LdapGroup.CLASS<LdapGroup> newInstance(org.zenframework.z8.server.ldap.LdapGroup group) {
		if (group == null)
			return null;

		LdapGroup.CLASS<LdapGroup> ldapGroup = new LdapGroup.CLASS<LdapGroup>(null);

		ldapGroup.get().domainName = new string(group.getDomainName());
		ldapGroup.get().name = new string(group.getName());

		for (String member : group.getMembers())
			ldapGroup.get().members.add(new string(member));

		for (Map.Entry<String, String> entry : group.getParameters().entrySet())
			ldapGroup.get().parameters.put(new string(entry.getKey()), new string(entry.getValue()));

		return ldapGroup;
	}
}
