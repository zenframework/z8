package org.zenframework.z8.server.base.form.report;

import java.io.File;

import org.zenframework.z8.server.base.file.InputOnlyFileItem;
import org.zenframework.z8.server.reports.poi.PoiReport;
import org.zenframework.z8.server.reports.poi.ReportOptions;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.guid;

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

	public ExcelReport(IObject container) {
		super(container);
	}

	protected file z8_execute(guid recordId) {
		ReportOptions options = new ReportOptions();
		options.setTemplate(template.get());
		options.setName(name != null ? name.get() : null);

		File diskFile = new PoiReport(options).setRanges(Range.asPoiRanges(ranges, this)).execute();
		file file = new file(diskFile);
		file.set(new InputOnlyFileItem(diskFile, diskFile.getName()));

		return file;
	}
}
