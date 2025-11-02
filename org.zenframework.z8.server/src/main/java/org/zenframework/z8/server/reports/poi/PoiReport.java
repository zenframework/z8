package org.zenframework.z8.server.reports.poi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.db.Select;
import org.zenframework.z8.server.expression.Expression;
import org.zenframework.z8.server.expression.ObjectContext;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.utils.IOUtils;

public class PoiReport {

	private final List<Range> ranges = new LinkedList<Range>();
	private final ReportOptions options;
	private final Expression expression;

	public PoiReport(ReportOptions options, OBJECT context) {
		this.options = options;
		this.expression = new Expression()
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
	}

	public PoiReport setRanges(List<Range> ranges) {
		this.ranges.clear();
		this.ranges.addAll(ranges);

		for (Range range : ranges)
			range.setReport(this);

		return this;
	}

	public Expression getExpression() {
		return expression;
	}

	public File execute() {
		Connection connection = ConnectionManager.get();
		connection.beginTransaction(); // for large cursors

		try {
			return run();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			connection.rollback();
		}
	}

	public List<String> getErrors() {
		return expression.getErrors();
	}

	private File run() throws IOException, InvalidFormatException {
		File templateFile = options.getTemplateFile();
		File outputFile = options.getOutputFile();

		FileUtils.copyFile(templateFile, outputFile);

		XSSFWorkbook workbook = loadXlsx(outputFile);

		for (Range range : ranges)
			range.apply(workbook);

		saveXlsx(workbook, outputFile);

		return outputFile;
	}

	private static XSSFWorkbook loadXlsx(File file) throws IOException {
		FileInputStream fis = new FileInputStream(file);
		try {
			return new XSSFWorkbook(fis);
		} finally {
			IOUtils.closeQuietly(fis);
		}
	}

	private static void saveXlsx(XSSFWorkbook workbook, File file) throws IOException {
		FileOutputStream fos = new FileOutputStream(file);
		try {
			workbook.write(fos);
		} finally {
			IOUtils.closeQuietly(fos);
			IOUtils.closeQuietly(workbook);
		}
	}
}
