package org.zenframework.z8.server.base.table.system;

import org.zenframework.z8.server.base.form.Desktop;
import org.zenframework.z8.server.base.table.system.view.AuthorityCenterView;
import org.zenframework.z8.server.base.table.system.view.DomainsView;
import org.zenframework.z8.server.base.table.system.view.FilesView;
import org.zenframework.z8.server.base.table.system.view.InterconnectionCenterView;
import org.zenframework.z8.server.base.table.system.view.JobsView;
import org.zenframework.z8.server.base.table.system.view.RoleTableAccessView;
import org.zenframework.z8.server.base.table.system.view.SequencesView;
import org.zenframework.z8.server.base.table.system.view.TablesView;
import org.zenframework.z8.server.base.table.system.view.TransportQueueView;
import org.zenframework.z8.server.base.table.system.view.UsersView;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.db.generator.DBGenerateProcedure;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.security.BuiltinUsers;
import org.zenframework.z8.server.types.guid;


public class SystemTools extends Desktop {
	static public guid Id = new SystemTools.CLASS<SystemTools>().key();
	static public String ClassId = new SystemTools.CLASS<SystemTools>().classId();

	static public class strings {
		public final static String Title = "SystemTools.title";
	}

	static public class displayNames {
		public final static String Title = Resources.get(strings.Title);
	}

	public static class CLASS<T extends SystemTools> extends Desktop.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(SystemTools.class);
			setDisplayName(displayNames.Title);
		}

		@Override
		public Object newObject(IObject container) {
			return new SystemTools(container);
		}
	}

	public OBJECT.CLASS<? extends OBJECT> users = new UsersView.CLASS<UsersView>(this);
	public OBJECT.CLASS<? extends OBJECT> domains = new DomainsView.CLASS<DomainsView>(this);
	public OBJECT.CLASS<? extends OBJECT> jobs = new JobsView.CLASS<JobsView>(this);
	public OBJECT.CLASS<? extends OBJECT> sequences = new SequencesView.CLASS<SequencesView>(this);
	public OBJECT.CLASS<? extends OBJECT> exportMessages = new TransportQueueView.CLASS<TransportQueueView>(this);
	public OBJECT.CLASS<? extends OBJECT> files = new FilesView.CLASS<FilesView>(this);

	public OBJECT.CLASS<? extends OBJECT> tables = new TablesView.CLASS<TablesView>(this);
	public OBJECT.CLASS<? extends OBJECT> roles = new RoleTableAccessView.CLASS<RoleTableAccessView>(this);

	public OBJECT.CLASS<? extends OBJECT> authorityCenter = new AuthorityCenterView.CLASS<AuthorityCenterView>(this);
	public OBJECT.CLASS<? extends OBJECT> interconnectionCenter = new InterconnectionCenterView.CLASS<InterconnectionCenterView>(this);

	public OBJECT.CLASS<? extends OBJECT> generator = new DBGenerateProcedure.CLASS<DBGenerateProcedure>(this);

	public SystemTools(IObject container) {
		super(container);
	}

	@Override
	public void initMembers() {
		super.initMembers();

		if(ServerConfig.isLatestVersion()) {
			objects.add(users);
			objects.add(domains);
			objects.add(jobs);
			objects.add(sequences);

			objects.add(exportMessages);
			objects.add(files);

			objects.add(tables);
			objects.add(roles);

			guid user = ApplicationServer.getUser().id();

			if(user.equals(BuiltinUsers.Administrator.guid()) || user.equals(BuiltinUsers.System.guid())) {
				objects.add(authorityCenter);
				objects.add(interconnectionCenter);
			}
		}

		objects.add(generator);
	}
}
