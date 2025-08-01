package org.zenframework.z8.justintime.table;

import java.util.Collection;

import org.zenframework.z8.justintime.runtime.DynamicRuntime;
import org.zenframework.z8.justintime.runtime.ISource;
import org.zenframework.z8.justintime.runtime.Workspace;
import org.zenframework.z8.server.base.form.action.Action;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.request.IMonitor;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.bool;
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
		useTransaction = bool.False;
	}

	@Override
	public void execute(Collection<guid> records, Query context, Collection<guid> selected, Query query) {
		compileAll((Source) query, true);
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void z8_execute(RCollection records, Query.CLASS<? extends Query> context, RCollection selected, Query.CLASS<? extends Query> query) {
		execute((Collection<guid>)records, context.get(), (Collection<guid>)selected, query.get());
	}

	public static void compileAll(ISource source, boolean useTransaction) {
		IMonitor monitor = ApplicationServer.getMonitor();

		DynamicRuntime.instance().unloadDynamic();

		try {
			if (useTransaction)
				ConnectionManager.get().beginTransaction();
			Workspace.workspace(ApplicationServer.getSchema()).recompile(source);
			if (useTransaction)
				ConnectionManager.get().commit();
		} catch (Throwable e) {
			if (useTransaction)
				ConnectionManager.get().rollback();
			if (monitor != null)
				monitor.error(e);
		}

		DynamicRuntime.instance().loadDynamic();
	}

	public static void z8_compileAll(Source.CLASS<? extends Source> source) {
		compileAll(source.get(), false);
	}
}
