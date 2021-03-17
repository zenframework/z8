package org.zenframework.z8.justintime.table;

import java.util.Collection;

import org.zenframework.z8.justintime.runtime.JustInTimeRuntime;
import org.zenframework.z8.justintime.runtime.Workspace;
import org.zenframework.z8.server.base.form.action.Action;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.guid;

public class CompileAction extends Action {

	static public class strings {
		public final static String Title = "CompileAction.title";
	}

	static public class displayNames {
		public final static String Title = Resources.get(strings.Title);
	}

	public static class CLASS<T extends CompileAction> extends Action.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(CompileAction.class);
			setDisplayName(displayNames.Title);
		}

		@Override
		public Object newObject(IObject container) {
			return new CompileAction(container);
		}
	}

	public CompileAction(IObject container) {
		super(container);
	}

	@Override
	public void execute(Collection<guid> records, Query context, Collection<guid> selected, Query query) {
		JustInTimeRuntime.instance().unloadDynamic();
		Workspace workspace = Workspace.workspace(ApplicationServer.getSchema());
		workspace.recompile((Source) query);
		JustInTimeRuntime.instance().loadDynamic();
	}

}
