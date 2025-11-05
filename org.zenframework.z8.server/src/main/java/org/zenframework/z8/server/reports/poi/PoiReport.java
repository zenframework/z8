package org.zenframework.z8.server.reports.poi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.expression.Expression;
import org.zenframework.z8.server.utils.IOUtils;

public class PoiReport {

	private final ReportOptions options;

	public PoiReport(ReportOptions options) {
		this.options = options;
	}

	public Expression getExpression() {
		return options.getExpression();
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
		return options.getExpression().getErrors();
	}

	private File run() throws IOException, InvalidFormatException {
		File templateFile = options.getTemplateFile();
		File outputFile = options.getOutputFile();

		FileUtils.copyFile(templateFile, outputFile);

		XSSFWorkbook workbook = loadXlsx(outputFile);

		for (Range range : options.getRanges())
			range.apply(workbook);

		hideColumns(workbook);

		saveXlsx(workbook, outputFile);

		return outputFile;
	}

	private void hideColumns(XSSFWorkbook workbook) {
		Sheet sheet = workbook.getSheetAt(0);

		for (int hiddenColumn : options.getHiddenColumns())
			sheet.setColumnHidden(hiddenColumn, true);
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
