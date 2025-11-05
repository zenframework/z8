package org.zenframework.z8.server.base.form.report;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.zenframework.z8.server.base.file.InputOnlyFileItem;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.reports.poi.PoiReport;
import org.zenframework.z8.server.reports.poi.ReportOptions;
import org.zenframework.z8.server.reports.poi.Util;
import org.zenframework.z8.server.request.IMonitor;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.string;

public class ExcelReport extends Report {
	static public class CLASS<T extends ExcelReport> extends Report.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(ExcelReport.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new ExcelReport(container);
		}
	}

	public RCollection<Range.CLASS<Range>> ranges = new RCollection<Range.CLASS<Range>>();
	public RCollection<string> hiddenColumns = new RCollection<string>();

	public ExcelReport(IObject container) {
		super(container);
	}

	protected file z8_execute(guid recordId) {
		ReportOptions options = new ReportOptions();
		options.setTemplate(template.get());
		options.setName(name != null ? name.get() : null);

		PoiReport report = new PoiReport(options, this).setRanges(Range.asPoiRanges(ranges))
				.setHiddenColumns(columnsToInt(hiddenColumns));

		File diskFile = report.execute();
		file file = new file(diskFile);
		file.set(new InputOnlyFileItem(diskFile, diskFile.getName()));

		IMonitor monitor = ApplicationServer.getMonitor();

		if (monitor != null) {
			for (String error : report.getErrors())
				monitor.warning(error);
		}

		return file;
	}

	private static Set<Integer> columnsToInt(Collection<string> columns) {
		Set<Integer> result = new HashSet<Integer>();

		for (string column : columns)
			result.add(Util.columnToInt(column.get()));

		return result;
	}
}
