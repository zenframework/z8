package org.zenframework.z8.server.runtime;

import org.zenframework.z8.server.base.table.system.Domains;
import org.zenframework.z8.server.base.table.system.Entries;
import org.zenframework.z8.server.base.table.system.Files;
import org.zenframework.z8.server.base.table.system.Jobs;
import org.zenframework.z8.server.base.table.system.Logs;
import org.zenframework.z8.server.base.table.system.MessageQueue;
import org.zenframework.z8.server.base.table.system.Properties;
import org.zenframework.z8.server.base.table.system.Property;
import org.zenframework.z8.server.base.table.system.SchedulerJobs;
import org.zenframework.z8.server.base.table.system.SecurityGroups;
import org.zenframework.z8.server.base.table.system.Sequences;
import org.zenframework.z8.server.base.table.system.SystemTools;
import org.zenframework.z8.server.base.table.system.TransportQueue;
import org.zenframework.z8.server.base.table.system.UserEntries;
import org.zenframework.z8.server.base.table.system.Users;

public class ServerRuntime extends AbstractRuntime {

	public static final Property DbSchemeControlSumProperty = new Property("C0CDFF6D-9357-41FA-94B1-61D131CC0C09",
			"z8.database.schemeControlSum", "000.000.0000", "Контрольная сумма схемы базы данных");

	public static final Property PreserveMessagesQueueProperty = new Property("8D9C727A-34FC-4DCD-9AB0-5A2AF8E676E0",
			"z8.transport.preserveExportMessages", "false",
			"Сохранять локальную очередь экспортируемых сообщений (true / false)");

	public ServerRuntime() {
		addTable(new Users.CLASS<Users>(null));
		addTable(new SecurityGroups.CLASS<SecurityGroups>(null));
		addTable(new Sequences.CLASS<Sequences>(null));
		addTable(new Entries.CLASS<Entries>(null));

		addTable(new Domains.CLASS<Domains>(null));
		addTable(new UserEntries.CLASS<UserEntries>(null));

		addTable(new Jobs.CLASS<Jobs>(null));
		addTable(new SchedulerJobs.CLASS<SchedulerJobs>(null));
		addTable(new Logs.CLASS<Logs>(null));

		addTable(new Files.CLASS<Files>(null));
		addTable(new Properties.CLASS<Properties>(null));

		addTable(new MessageQueue.CLASS<MessageQueue>(null));
		addTable(new TransportQueue.CLASS<TransportQueue>(null));

		addEntry(new SystemTools.CLASS<SystemTools>(null));

		addActivator(new Properties.PropertiesActivator.CLASS<Properties.PropertiesActivator>(null));

		addProperty(DbSchemeControlSumProperty);
		addProperty(PreserveMessagesQueueProperty);
	}

}
