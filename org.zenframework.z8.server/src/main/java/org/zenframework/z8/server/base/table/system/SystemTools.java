package org.zenframework.z8.server.base.table.system;

import org.zenframework.z8.server.base.form.Desktop;
import org.zenframework.z8.server.base.simple.Runnable;
import org.zenframework.z8.server.base.table.system.view.AuthorityCenterView;
import org.zenframework.z8.server.base.table.system.view.FilesView;
import org.zenframework.z8.server.base.table.system.view.InterconnectionCenterView;
import org.zenframework.z8.server.base.table.system.view.RoleTableAccessView;
import org.zenframework.z8.server.base.table.system.view.TablesView;
import org.zenframework.z8.server.base.table.system.view.TransportQueueView;
import org.zenframework.z8.server.base.table.system.view.UsersView;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.db.generator.DBGenerateProcedure;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.security.BuiltinUsers;
import org.zenframework.z8.server.types.guid;


public class SystemTools extends Desktop {
	static public guid Id = new guid("00000000-0000-0000-0000-000000000001");

	static public String ClassName = SystemTools.class.getCanonicalName();

	static public class strings {
		public final static String Title = "SystemTools.title";
	}

	static public class displayNames {
		public final static String Title = Resources.get(strings.Title);
	}

	public static class CLASS<T extends SystemTools> extends Desktop.CLASS<T> {
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

	public Runnable.CLASS<? extends Runnable> users = new UsersView.CLASS<UsersView>(this);
	public Runnable.CLASS<? extends Runnable> domains = new Domains.CLASS<Domains>(this);
	public Runnable.CLASS<? extends Runnable> tasks = new Logs.CLASS<Logs>(this);
	public Runnable.CLASS<? extends Runnable> sequences = new Sequences.CLASS<Sequences>(this);
	public Runnable.CLASS<? extends Runnable> exportMessages = new TransportQueueView.CLASS<TransportQueueView>(this);
	public Runnable.CLASS<? extends Runnable> files = new FilesView.CLASS<FilesView>(this);

	public Runnable.CLASS<? extends Runnable> tables = new TablesView.CLASS<TablesView>(this);
	public Runnable.CLASS<? extends Runnable> roles = new RoleTableAccessView.CLASS<RoleTableAccessView>(this);

	public Runnable.CLASS<? extends Runnable> authorityCenter = new AuthorityCenterView.CLASS<AuthorityCenterView>(this);
	public Runnable.CLASS<? extends Runnable> interconnectionCenter = new InterconnectionCenterView.CLASS<InterconnectionCenterView>(this);

	public Runnable.CLASS<? extends Runnable> generator = new DBGenerateProcedure.CLASS<DBGenerateProcedure>(this);

	public SystemTools(IObject container) {
		super(container);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		if(ServerConfig.isSystemInstalled()) {
			runnables.add(users);
			runnables.add(domains);
			runnables.add(tasks);
			runnables.add(sequences);

			runnables.add(exportMessages);
			runnables.add(files);

			runnables.add(tables);
			runnables.add(roles);

			guid user = ApplicationServer.getUser().id();

			if(user.equals(BuiltinUsers.Administrator.guid()) || user.equals(BuiltinUsers.System.guid())) {
				runnables.add(authorityCenter);
				runnables.add(interconnectionCenter);
			}
		}

		runnables.add(generator);
	}
}
