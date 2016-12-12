package org.zenframework.z8.server.runtime;

import org.zenframework.z8.server.base.table.system.Domains;
import org.zenframework.z8.server.base.table.system.Entries;
import org.zenframework.z8.server.base.table.system.Fields;
import org.zenframework.z8.server.base.table.system.Files;
import org.zenframework.z8.server.base.table.system.Jobs;
import org.zenframework.z8.server.base.table.system.Logs;
import org.zenframework.z8.server.base.table.system.MessageQueue;
import org.zenframework.z8.server.base.table.system.RoleTableAccess;
import org.zenframework.z8.server.base.table.system.SchedulerJobs;
import org.zenframework.z8.server.base.table.system.SecurityGroups;
import org.zenframework.z8.server.base.table.system.Sequences;
import org.zenframework.z8.server.base.table.system.SystemTools;
import org.zenframework.z8.server.base.table.system.Tables;
import org.zenframework.z8.server.base.table.system.TransportQueue;
import org.zenframework.z8.server.base.table.system.UserEntries;
import org.zenframework.z8.server.base.table.system.Users;

public class ServerRuntime extends AbstractRuntime {
	public ServerRuntime() {
		addTable(new Users.CLASS<Users>(null));
		addTable(new SecurityGroups.CLASS<SecurityGroups>(null));
		addTable(new Sequences.CLASS<Sequences>(null));

		addTable(new Tables.CLASS<Tables>(null));
		addTable(new Fields.CLASS<Fields>(null));

		addTable(new RoleTableAccess.CLASS<RoleTableAccess>(null));

		addTable(new Domains.CLASS<Domains>(null));

		addTable(new Entries.CLASS<Entries>(null));
		addTable(new UserEntries.CLASS<UserEntries>(null));

		addTable(new Jobs.CLASS<Jobs>(null));
		addTable(new SchedulerJobs.CLASS<SchedulerJobs>(null));
		addTable(new Logs.CLASS<Logs>(null));

		addTable(new Files.CLASS<Files>(null));

		addTable(new MessageQueue.CLASS<MessageQueue>(null));
		addTable(new TransportQueue.CLASS<TransportQueue>(null));

		addEntry(new SystemTools.CLASS<SystemTools>(null));
	}
}
