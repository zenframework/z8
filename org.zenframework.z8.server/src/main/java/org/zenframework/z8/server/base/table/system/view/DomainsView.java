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
			setAttribute(SystemTool, Integer.toString(700));
		}

		@Override
		public Object newObject(IObject container) {
			return new DomainsView(container);
		}
	}

	protected UpdateDomainsAction.CLASS<UpdateDomainsAction> updateDomains = new UpdateDomainsAction.CLASS<UpdateDomainsAction>(this);

	public DomainsView(IObject container) {
		super(container);
	}

	@Override
	public void initMembers() {
		super.initMembers();

		objects.add(updateDomains);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		readOnly = new bool(!ApplicationServer.getUser().isAdministrator());

		colCount = new integer(4);

		description.get().colSpan = new integer(4);

		names.add(name);
		names.add(expiration);

		updateDomains.setIndex("updateDomains");

		actions.add(updateDomains);

		registerControl(name);
		registerControl(address);
		registerControl(users.get().name);
		registerControl(owner);
		registerControl(lastMessageAt);
		registerControl(lastSendAt);
		registerControl(expiration);
		registerControl(idleTimeout);
		registerControl(description);
	}
}