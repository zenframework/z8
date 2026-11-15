package org.zenframework.z8.server.base.table.system.view;

import org.zenframework.z8.server.base.table.system.Settings;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.integer;

public class SettingsView extends Settings {
	public static class CLASS<T extends SettingsView> extends Settings.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(SettingsView.class);
			setAttribute(SystemTool, Integer.toString(700));
		}

		@Override
		public Object newObject(IObject container) {
			return new SettingsView(container);
		}
	}

	public SettingsView(IObject container) {
		super(container);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		readOnly = new bool(!ApplicationServer.getUser().isAdministrator());

		colCount = new integer(1);

		description.get().flex = new integer(1);
		value.get().flex = new integer(2);

		names.add(name);

		columns.add(name);
		columns.add(value);

		controls.add(name);
		controls.add(description);
		controls.add(value);
	}
}
