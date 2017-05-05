package org.zenframework.z8.server.base.table.system.view;

import org.zenframework.z8.server.base.table.system.Domains;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.integer;

public class DomainsView extends Domains {
	public static class CLASS<T extends DomainsView> extends Domains.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(DomainsView.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new DomainsView(container);
		}
	}

	public DomainsView(IObject container) {
		super(container);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		readOnly = new bool(!ApplicationServer.getUser().isAdministrator());

		colCount = new integer(4);

		registerControl(name);
		registerControl(address);
		registerControl(users.get().name);
		registerControl(owner);
		description.get().colSpan = new integer(4);
		registerControl(description);
	}
}