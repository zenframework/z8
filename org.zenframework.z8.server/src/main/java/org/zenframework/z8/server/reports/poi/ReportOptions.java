package org.zenframework.z8.server.reports.poi;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.zenframework.z8.server.base.file.Folders;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.db.Select;
import org.zenframework.z8.server.expression.Expression;
import org.zenframework.z8.server.expression.ObjectContext;
import org.zenframework.z8.server.reports.ReportUtils;
import org.zenframework.z8.server.reports.Reports;
import org.zenframework.z8.server.runtime.OBJECT;

public class ReportOptions {

	public static final String Xlsx = "xlsx";

	private static final String Ext = "." + Xlsx;

	private final List<Range> ranges = new LinkedList<Range>();
	private final Set<Integer> hiddenColumns = new HashSet<Integer>();

	private String templateFolder = Folders.Reports;
	private String template = Reports.DefaultDesign;
	private String name = null;
	private OBJECT context;

	private Expression expression;

	public String getTemplateFolder() {
		return templateFolder;
	}

	public ReportOptions setTemplateFolder(String templateFolder) {
		this.templateFolder = templateFolder;
		return this;
	}

	public String getTemplate() {
		return template;
	}

	public ReportOptions setTemplate(String template) {
		this.template = template;
		return this;
	}

	public File getTemplateFile() {
		String template = this.template.toLowerCase().endsWith(Ext) ? this.template : this.template + Ext;
		return FileUtils.getFile(Folders.ApplicationPath, templateFolder, template);
	}

	public String getName() {
		return name != null ? name : !template.toLowerCase().endsWith(Ext) ? template
				: template.substring(0, template.length() - Ext.length());
	}

	public ReportOptions setName(String name) {
		this.name = name;
		return this;
	}

	public File getOutputFile() {
		File outputFolder = Folders.ReportsOutput;
		outputFolder.mkdirs();
		return ReportUtils.getUniqueFileName(outputFolder, getName(), ReportOptions.Xlsx);
	}

	public List<Range> getRanges() {
		return ranges;
	}

	public ReportOptions setRanges(List<Range> ranges) {
		this.ranges.clear();
		this.ranges.addAll(ranges);

		for (Range range : ranges)
			range.setReport(this);

		return this;
	}

	public Set<Integer> getHiddenColumns() {
		return hiddenColumns;
	}

	public ReportOptions setHiddenColumns(Collection<Integer> hiddenColumns) {
		this.hiddenColumns.clear();
		this.hiddenColumns.addAll(hiddenColumns);
		return this;
	}

	public ReportOptions addHiddenColumn(int hiddenColumn) {
		this.hiddenColumns.add(hiddenColumn);
		return this;
	}

	public ReportOptions addHiddenColumn(String hiddenColumn) {
		return addHiddenColumn(Util.columnToInt(hiddenColumn));
	}

	public ReportOptions setContext(OBJECT context) {
		this.context = context;
		return this;
	}

	public OBJECT getContext() {
		return context;
	}

	public Expression getExpression() {
		if (expression == null)
			expression = new Expression()
					.setContext(new ObjectContext(context))
					.setGetter(new Expression.Getter() {
						@Override
						@SuppressWarnings("rawtypes")
						public Object getValue(Object value) {
							if (value instanceof Wrapper)
								return ((Wrapper) value).get();

							if (value instanceof Field) {
								Field field = (Field) value;
								Select cursor = field.getCursor();
								return cursor == null || cursor.isClosed() ? field : field.get();
							}

							return value;
						}
					});

		return expression;
	}
}
