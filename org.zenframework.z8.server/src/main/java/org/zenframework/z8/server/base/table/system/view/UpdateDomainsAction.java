package org.zenframework.z8.server.base.table.system.view;

import org.zenframework.z8.server.base.form.action.Action;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.system.Domains;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.RCollection;

public class UpdateDomainsAction extends Action {
	static public class CLASS<T extends UpdateDomainsAction> extends Action.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(UpdateDomainsAction.class);
			setDisplayName(DomainsView.displayNames.UpdateDomains);
		}

		@Override
		public Object newObject(IObject container) {
			return new UpdateDomainsAction(container);
		}
	}

	public UpdateDomainsAction(IObject container) {
		super(container);
	}

	@Override
	@SuppressWarnings({ "rawtypes" })
	public void z8_execute(RCollection records, Query.CLASS<? extends Query> context, RCollection selected, Query.CLASS<? extends Query> query) {
		Domains.newInstance().updateDomains();
	}

}
