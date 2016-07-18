package org.zenframework.z8.server.base.table.system;

import org.zenframework.z8.server.base.form.Desktop;
import org.zenframework.z8.server.base.table.system.view.AuthorityCenterView;
import org.zenframework.z8.server.base.table.system.view.FilesView;
import org.zenframework.z8.server.base.table.system.view.InterconnectionCenterView;
import org.zenframework.z8.server.base.table.system.view.MessagesQueueView;
import org.zenframework.z8.server.base.table.system.view.UserEntriesView;
import org.zenframework.z8.server.db.generator.DBGenerateProcedure;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.ie.TransportRoutes;
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

	public final UserEntriesView.CLASS<UserEntriesView> userEntries = new UserEntriesView.CLASS<UserEntriesView>(this);
	public final Logs.CLASS<Logs> taskLogs = new Logs.CLASS<Logs>(this);
	public final Sequences.CLASS<Sequences> sequences = new Sequences.CLASS<Sequences>(this);
	public final DBGenerateProcedure.CLASS<DBGenerateProcedure> generator = new DBGenerateProcedure.CLASS<DBGenerateProcedure>(this);
	public final Properties.CLASS<Properties> properties = new Properties.CLASS<Properties>(this);
	public final MessagesQueueView.CLASS<MessagesQueueView> exportMessages = new MessagesQueueView.CLASS<MessagesQueueView>(this);
	public final TransportRoutes.CLASS<TransportRoutes> transportRoutes = new TransportRoutes.CLASS<TransportRoutes>(this);
	public final FilesView.CLASS<FilesView> files = new FilesView.CLASS<FilesView>(this);
	public final Domains.CLASS<Domains> addresses = new Domains.CLASS<Domains>(this);

	public final AuthorityCenterView.CLASS<AuthorityCenterView> authorityCenter = new AuthorityCenterView.CLASS<AuthorityCenterView>(this);
	public final InterconnectionCenterView.CLASS<InterconnectionCenterView> interconnectionCenter = new InterconnectionCenterView.CLASS<InterconnectionCenterView>(this);

	public SystemTools(IObject container) {
		super(container);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		runnables.add(userEntries);
		runnables.add(taskLogs);
		runnables.add(sequences);
		runnables.add(generator);
		runnables.add(properties);
		runnables.add(exportMessages);
		runnables.add(transportRoutes);
		runnables.add(files);
		runnables.add(addresses);

		guid user = ApplicationServer.getUser().id();
		
		if(user.equals(BuiltinUsers.Administrator.guid()) || user.equals(BuiltinUsers.System.guid())) {
			runnables.add(authorityCenter);
			runnables.add(interconnectionCenter);
		}

		dataSets.add(userEntries);
		dataSets.add(taskLogs);
		dataSets.add(sequences);
		dataSets.add(properties);
		dataSets.add(exportMessages);
		dataSets.add(transportRoutes);
		dataSets.add(files);
		dataSets.add(addresses);
	}
}
