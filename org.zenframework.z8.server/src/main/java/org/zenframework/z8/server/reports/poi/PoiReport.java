package org.zenframework.z8.server.reports.poi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
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

	private final Map<Integer, Range> ranges = new HashMap<Integer, Range>();
	private final Map<Integer, Collection<Integer>> hiddenColumns = new HashMap<Integer, Collection<Integer>>();
	private final ReportOptions options;

	private OBJECT context;

	private Expression expression;

	public PoiReport(ReportOptions options) {
		this.options = options;
	}

	public ReportOptions getOptions() {
		return options;
	}

	public Map<Integer, Collection<Integer>> getHiddenColumns() {
		return hiddenColumns;
	}

	public PoiReport setHiddenColumns(Map<Integer, Collection<Integer>> hiddenColumns) {
		this.hiddenColumns.clear();
		this.hiddenColumns.putAll(hiddenColumns);
		return this;
	}

	public PoiReport addHiddenColumn(int sheet, int hiddenColumn) {
		Collection<Integer> sheetColumns = hiddenColumns.get(sheet);

		if (sheetColumns == null)
			hiddenColumns.put(sheet, sheetColumns = new HashSet<Integer>());

		sheetColumns.add(hiddenColumn);

		return this;
	}

	public PoiReport addHiddenColumn(int sheet, String hiddenColumn) {
		return addHiddenColumn(sheet, Util.columnToInt(hiddenColumn));
	}

	public Range getRange(int sheet) {
		return ranges.get(sheet);
	}

	public PoiReport addRange(int sheet, Range range) {
		Range sheetRange = ranges.get(sheet);

		if (sheetRange == null)
			ranges.put(sheet, sheetRange = new Range().setReport(this).setName("Sheet[" + sheet + ']').setSource(new OBJECT.CLASS<OBJECT>(null).get()));

		sheetRange.addRange(range);

		return this;
	}

	public PoiReport setContext(OBJECT context) {
		this.context = context;
		return this;
	}

	public OBJECT getContext() {
		return context;
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
		return getExpression().getErrors();
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

	private File run() throws IOException, InvalidFormatException {
		File templateFile = options.getTemplateFile();
		File outputFile = options.getOutputFile();

		FileUtils.copyFile(templateFile, outputFile);

		XSSFWorkbook workbook = loadXlsx(outputFile);

		SheetModifier sheet = new SheetModifier().setWorkbook(workbook);

		try {
			for (Map.Entry<Integer, Range> entry : ranges.entrySet()) {
				entry.getValue().apply(sheet.setSheet(entry.getKey()));
			}
		} finally {
			sheet.close();
		}

		hideColumns(workbook);

		saveXlsx(workbook, outputFile);

		return outputFile;
	}

	private void hideColumns(XSSFWorkbook workbook) {
		for (Map.Entry<Integer, Collection<Integer>> entry : hiddenColumns.entrySet()) {
			Sheet sheet = workbook.getSheetAt(entry.getKey());
			for (int hiddenColumn : entry.getValue())
				sheet.setColumnHidden(hiddenColumn, true);
		}
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
