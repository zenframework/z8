package org.zenframework.z8.server.reports.poi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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

		SheetModifier sheet = new SheetModifier().open(workbook);

		Block boundaries = new Block();
		Block.Vector shift = new Block.Vector();

		try {
			for (Range range : options.getRanges()) {
				Block modified = range.apply(sheet.setSheet(range.getSheetIndex()), shift);
				boundaries = Block.boundaries(boundaries, modified);
				// TODO Correct shift calculation
				shift = shift.add(modified.diffSize(range.getBoundaries(sheet)).component(Block.Direction.Vertical));
			}
		} finally {
			sheet.close();
		}

		hideColumns(workbook);

		saveXlsx(workbook, outputFile);

		return outputFile;
	}

	private void hideColumns(XSSFWorkbook workbook) {
		for (Map.Entry<Integer, Collection<Integer>> entry : options.getHiddenColumns().entrySet()) {
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
