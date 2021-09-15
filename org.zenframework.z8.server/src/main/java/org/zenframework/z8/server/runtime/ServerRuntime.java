package org.zenframework.z8.server.runtime;

import org.zenframework.z8.server.base.table.system.Domains;
import org.zenframework.z8.server.base.table.system.Entries;
import org.zenframework.z8.server.base.table.system.Fields;
import org.zenframework.z8.server.base.table.system.Files;
import org.zenframework.z8.server.base.table.system.Jobs;
import org.zenframework.z8.server.base.table.system.MessageQueue;
import org.zenframework.z8.server.base.table.system.Requests;
import org.zenframework.z8.server.base.table.system.RoleFieldAccess;
import org.zenframework.z8.server.base.table.system.RoleRequestAccess;
import org.zenframework.z8.server.base.table.system.RoleTableAccess;
import org.zenframework.z8.server.base.table.system.Roles;
import org.zenframework.z8.server.base.table.system.ScheduledJobLogs;
import org.zenframework.z8.server.base.table.system.ScheduledJobs;
import org.zenframework.z8.server.base.table.system.Sequences;
import org.zenframework.z8.server.base.table.system.Settings;
import org.zenframework.z8.server.base.table.system.SystemTools;
import org.zenframework.z8.server.base.table.system.Tables;
import org.zenframework.z8.server.base.table.system.TransportQueue;
import org.zenframework.z8.server.base.table.system.UserEntries;
import org.zenframework.z8.server.base.table.system.UserRoles;
import org.zenframework.z8.server.base.table.system.Users;
import org.zenframework.z8.server.base.table.system.view.DomainsView;
import org.zenframework.z8.server.base.table.system.view.FilesView;
import org.zenframework.z8.server.base.table.system.view.JobsView;
import org.zenframework.z8.server.base.table.system.view.RoleTableAccessView;
import org.zenframework.z8.server.base.table.system.view.SequencesView;
import org.zenframework.z8.server.base.table.system.view.TablesView;
import org.zenframework.z8.server.base.table.system.view.TransportQueueView;
import org.zenframework.z8.server.base.table.system.view.UsersView;

public class ServerRuntime extends AbstractRuntime {
	public ServerRuntime() {
		addTable(new Users.CLASS<Users>(null));
		addTable(new Roles.CLASS<Roles>(null));
		addTable(new Sequences.CLASS<Sequences>(null));
		addTable(new Settings.CLASS<Settings>(null));

		addTable(new Tables.CLASS<Tables>(null));
		addTable(new Fields.CLASS<Fields>(null));
		addTable(new Requests.CLASS<Requests>(null));

		addTable(new RoleTableAccess.CLASS<RoleTableAccess>(null));
		addTable(new RoleFieldAccess.CLASS<RoleFieldAccess>(null));
		addTable(new RoleRequestAccess.CLASS<RoleRequestAccess>(null));

		addTable(new UserRoles.CLASS<UserRoles>(null));

		addTable(new Domains.CLASS<Domains>(null));

		addTable(new Entries.CLASS<Entries>(null));
		addTable(new UserEntries.CLASS<UserEntries>(null));

		addTable(new Jobs.CLASS<Jobs>(null));
		addTable(new ScheduledJobs.CLASS<ScheduledJobs>(null));
		addTable(new ScheduledJobLogs.CLASS<ScheduledJobLogs>(null));

		addTable(new Files.CLASS<Files>(null));

		addTable(new MessageQueue.CLASS<MessageQueue>(null));
		addTable(new TransportQueue.CLASS<TransportQueue>(null));

		addEntry(new SystemTools.CLASS<SystemTools>(null));

		addSystemTool(new UsersView.CLASS<UsersView>(null));
		addSystemTool(new RoleTableAccessView.CLASS<RoleTableAccessView>(null));
		addSystemTool(new JobsView.CLASS<JobsView>(null));
		addSystemTool(new TablesView.CLASS<TablesView>(null));
		addSystemTool(new SequencesView.CLASS<SequencesView>(null));
		addSystemTool(new FilesView.CLASS<FilesView>(null));
		addSystemTool(new DomainsView.CLASS<DomainsView>(null));
		addSystemTool(new TransportQueueView.CLASS<TransportQueueView>(null));
	}
}
