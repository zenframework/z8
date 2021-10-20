package org.zenframework.z8.server.base.table.system;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.zenframework.z8.server.base.form.Desktop;
import org.zenframework.z8.server.base.table.system.view.AuthorityCenterView;
import org.zenframework.z8.server.base.table.system.view.InterconnectionCenterView;
import org.zenframework.z8.server.db.generator.SchemaGenerator;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.engine.Runtime;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
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
			setIcon("fa-cog");
		}

		@Override
		public Object newObject(IObject container) {
			return new SystemTools(container);
		}
	}

	private static Comparator<OBJECT.CLASS<? extends OBJECT>> SystemToolComparator = new Comparator<OBJECT.CLASS<? extends OBJECT>>() {

		@Override
		public int compare(OBJECT.CLASS<? extends OBJECT> o1, OBJECT.CLASS<? extends OBJECT> o2) {
			return Integer.compare(systemToolIndex(o1), systemToolIndex(o2));
		}

	};

	private List<OBJECT.CLASS<? extends OBJECT>> systemTools;

	public OBJECT.CLASS<? extends OBJECT> generator = new SchemaGenerator.CLASS<SchemaGenerator>(this);

	public SystemTools(IObject container) {
		super(container);

		List<OBJECT.CLASS<? extends OBJECT>> systemTools = new ArrayList<OBJECT.CLASS<? extends OBJECT>>(Runtime.instance().systemTools());

		Collections.sort(systemTools, SystemToolComparator);

		this.systemTools = new ArrayList<OBJECT.CLASS<? extends OBJECT>>(systemTools.size() + 3);
		for (OBJECT.CLASS<? extends OBJECT> systemTool : systemTools)
			this.systemTools.add(systemTool.clone(this));

		if(ApplicationServer.getUser().isAdministrator()) {
			this.systemTools.add(new AuthorityCenterView.CLASS<AuthorityCenterView>(this));
			this.systemTools.add(new InterconnectionCenterView.CLASS<InterconnectionCenterView>(this));
		}
	}

	@Override
	public void initMembers() {
		super.initMembers();

		if (ApplicationServer.getDatabase().isLatestVersion()) {
			for (OBJECT.CLASS<? extends OBJECT> systemTool : systemTools)
				objects.add(systemTool);
		}

		objects.add(generator);
	}

	public void constructor2() {
		super.constructor2();

		if (ApplicationServer.getDatabase().isLatestVersion()) {
			int index = 0;
			for (OBJECT.CLASS<? extends OBJECT> systemTool : systemTools)
				systemTool.setIndex("systemTool" + index++);
		}

		generator.setIndex("generator");
	}

	private static int systemToolIndex(OBJECT.CLASS<? extends OBJECT> cls) {
		try {
			return Integer.parseInt(cls.getAttribute(SystemTool));
		} catch (Throwable e) {
			return 1000;
		}
	}

}
