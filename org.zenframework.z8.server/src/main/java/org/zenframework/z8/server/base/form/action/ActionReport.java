package org.zenframework.z8.server.base.form.action;

import org.zenframework.z8.server.base.form.report.Report;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.string;

public class ActionReport extends Action {
	static public class CLASS<T extends ActionReport> extends Action.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(ActionReport.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new ActionReport(container);
		}
	}

	public Report.CLASS<? extends Report> report;

	public ActionReport(IObject container) {
		super(container);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		parameters.add(Parameter.z8_create(new string(Json.format), new string(Report.Pdf)));
	}

	@Override
	public void writeMeta(JsonWriter writer, Query query, Query context) {
		super.writeMeta(writer, query, context);
		writer.writeProperty(Json.report, true);
	}

	@SuppressWarnings("rawtypes")
	public void z8_execute(guid record, Query.CLASS<? extends Query> context, RCollection selected, Query.CLASS<? extends Query> query) {
		Report report = this.report.get();
		Parameter parameter = (Parameter)getParameter(Json.format.get());
		report.format = parameter != null ? parameter.string() : Report.Pdf;
		ApplicationServer.getMonitor().print(report.run(record));
	}
}
