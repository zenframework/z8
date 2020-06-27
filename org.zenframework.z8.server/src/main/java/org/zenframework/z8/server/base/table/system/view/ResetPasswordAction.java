package org.zenframework.z8.server.base.table.system.view;

import org.zenframework.z8.server.base.form.action.Action;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.system.Users;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.guid;

public class ResetPasswordAction extends Action {
	static public class CLASS<T extends ResetPasswordAction> extends Action.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(ResetPasswordAction.class);
			setDisplayName(Users.displayNames.ResetPassword);
		}

		@Override
		public Object newObject(IObject container) {
			return new ResetPasswordAction(container);
		}
	}

	public ResetPasswordAction(IObject container) {
		super(container);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void z8_execute(guid recordId, Query.CLASS<? extends Query> context, RCollection selected, Query.CLASS<? extends Query> query) {
		Users.resetPassword(recordId);
	}
}
