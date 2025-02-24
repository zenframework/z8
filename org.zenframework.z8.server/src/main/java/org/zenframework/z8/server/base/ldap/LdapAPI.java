package org.zenframework.z8.server.base.ldap;

import java.util.Collection;

import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.string;

@SuppressWarnings("all")
public class LdapAPI extends OBJECT {
	public static class CLASS<T extends LdapAPI> extends OBJECT.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(LdapAPI.class);
		}

		public Object newObject(IObject container) {
			return new LdapAPI(container);
		}
	}

	private org.zenframework.z8.server.ldap.LdapAPI ldap;

	public LdapAPI(IObject container) {
		super(container);
	}

	public LdapUser.CLASS<? extends LdapUser> z8_getUser(string login) {
		return LdapUser.newInstance(ldap.getUser(login.get()));
	}

	public LdapUser.CLASS<? extends LdapUser> z8_getUser(string searchBase, string searchFilter) {
		return LdapUser.newInstance(ldap.getUser(searchBase.get(), searchFilter.get()));
	}

	public RCollection z8_getUsers() {
		Collection<org.zenframework.z8.server.ldap.LdapUser> ldapUsers = ldap.getUsers();
		RCollection result = new RCollection(ldapUsers.size(), false);

		for (org.zenframework.z8.server.ldap.LdapUser ldapUser : ldapUsers)
			result.add(LdapUser.newInstance(ldapUser));

		return result;
	}

	public RCollection z8_getUsers(string searchBase, string searchFilter) {
		Collection<org.zenframework.z8.server.ldap.LdapUser> ldapUsers = ldap.getUsers(searchBase.get(), searchFilter.get());
		RCollection result = new RCollection(ldapUsers.size(), false);

		for (org.zenframework.z8.server.ldap.LdapUser ldapUser : ldapUsers)
			result.add(LdapUser.newInstance(ldapUser));

		return result;
	}

	public LdapGroup.CLASS<? extends LdapGroup> z8_getGroup(string name) {
		return LdapGroup.newInstance(ldap.getGroup(name.get()));
	}

	public LdapGroup.CLASS<? extends LdapGroup> z8_getGroup(string searchBase, string searchFilter) {
		return LdapGroup.newInstance(ldap.getGroup(searchBase.get(), searchFilter.get()));
	}

	public RCollection z8_getGroups() {
		Collection<org.zenframework.z8.server.ldap.LdapGroup> ldapGroups = ldap.getGroups();
		RCollection result = new RCollection();

		for (org.zenframework.z8.server.ldap.LdapGroup ldapGroup : ldapGroups)
			result.add(LdapGroup.newInstance(ldapGroup));

		return result;
	}

	public RCollection z8_getGroups(string searchBase, string searchFilter) {
		Collection<org.zenframework.z8.server.ldap.LdapGroup> ldapGroups = ldap.getGroups(searchBase.get(), searchFilter.get());
		RCollection result = new RCollection();

		for (org.zenframework.z8.server.ldap.LdapGroup ldapGroup : ldapGroups)
			result.add(LdapGroup.newInstance(ldapGroup));

		return result;
	}

	public void z8_close() {
		if (ldap != null)
			ldap.close();
	}

	public static LdapAPI.CLASS<LdapAPI> z8_getLdapAPI(string url, string principalName, string credentials) {
		return z8_getLdapAPI(url, principalName, credentials, string.Empty);
	}

	public static LdapAPI.CLASS<LdapAPI> z8_getLdapAPI(string url, string principalName, string credentials, string config) {
		LdapAPI.CLASS<LdapAPI> connector = new LdapAPI.CLASS<LdapAPI>(null);
		connector.get().ldap = new org.zenframework.z8.server.ldap.LdapAPI(url.get(), principalName.get(), credentials.get(), config.get());
		return connector;
	}
}
