package org.zenframework.z8.server.reports.poi;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.zenframework.z8.server.reports.poi.Range.Direction;

public class Util {

	private Util() {}

	public static interface CellVisitor {
		void visit(Row row, int colNum, Cell cell);
	}

	public static void multiplyBlock(Sheet sheet, CellRangeAddress address, int count, Range.Direction direction) {
		int width = address.getLastColumn() - address.getFirstColumn() + 1;
		int height = address.getLastRow() - address.getFirstRow() + 1;

		if (count < 1)
			return; // TODO Delete cells

		if (direction == Range.Direction.Horizontal)
			insertBlockHorizShift(sheet, address.getFirstRow(), address.getLastColumn() + 1, height, width * (count - 1));
		else
			insertBlockVertShift(sheet, address.getLastRow() + 1, address.getFirstColumn(), height * (count - 1), width);

		for (int i = 1; i < count; i++)
			copy(sheet, address, direction == Direction.Horizontal ? address.getFirstRow() : address.getFirstRow() + height * i,
					direction == Direction.Horizontal ? address.getFirstColumn() + width * i : address.getFirstColumn());
	}

	public static void insertBlock(Sheet sheet, int rowStart, int colStart, int rowCount, int colCount, Range.Direction direction) {
		if (direction == Range.Direction.Horizontal)
			insertBlockHorizShift(sheet, rowStart, colStart, rowCount, colCount);
		else
			insertBlockVertShift(sheet, rowStart, colStart, rowCount, colCount);
	}

	private static void insertBlockHorizShift(Sheet sheet, int rowStart, int colStart, int rowCount, int colCount) {
		if (colCount < 0)
			throw new RuntimeException("PoiReport.insertBlockHorizShift()");

		int colEnd = sheet.getRow(rowStart).getLastCellNum() + 1;
		int rowEnd = rowCount >= 0 ? rowStart + rowCount : sheet.getLastRowNum() + 1;

		for (int rowNum = rowStart; rowNum < rowEnd; rowNum++) {
			Row row = sheet.getRow(rowNum);

			if (row == null)
				row = sheet.createRow(rowNum);

			// Ensure columns exist
			for (int i = 0; i < colCount; i++) {
				Cell cell = row.getCell(colEnd + i);

				if (cell == null)
					cell = row.createCell(colEnd + i, CellType.BLANK);
			}

			for (int colNum = colEnd - 1; colNum >= colStart; colNum--)
				copy(row, colNum, row, colNum + colCount);

			for (int i = 0; i < colCount; i++) {
				Cell cell = row.getCell(colStart + i);
				if (cell != null)
					cell.setCellType(CellType.BLANK);
			}
		}

		updateMergedRegions(sheet, rowStart, colStart, rowEnd, colEnd, 0, colCount);
	}

	private static void insertBlockVertShift(Sheet sheet, int rowStart, int colStart, int rowCount, int colCount) {
		if (rowCount < 0)
			throw new RuntimeException("PoiReport.insertBlockVertShift()");

		int colEnd = colCount >= 0 ? colStart + colCount : sheet.getRow(rowStart).getLastCellNum() + 1;
		int rowEnd = sheet.getLastRowNum() + 1;

		// Ensure rows exist
		for (int i = 0; i < rowCount; i++) {
			Row row = sheet.getRow(rowEnd + i);

			if (row != null)
				continue;

			row = sheet.createRow(rowEnd + i);

			for (int colNum = 0; colNum < colEnd; colNum++)
				row.createCell(colNum, CellType.BLANK);
		}

		// Copy rows from bottom to top
		for (int rowNum = rowEnd - 1; rowNum >= rowStart; rowNum--)
			copy(sheet.getRow(rowNum), colStart, sheet.getRow(rowNum + rowCount), colStart, colCount);

		for (int i = 0; i < rowCount; i++)
			clear(sheet.getRow(rowStart + i), colStart, colEnd);

		updateMergedRegions(sheet, rowStart, colStart, rowEnd, colEnd, rowCount, 0);
	}

	public static void clear(Row row, int colStart, int colEnd) {
		for (int colNum = colStart; colNum < colEnd; colNum++) {
			Cell cell = row.getCell(colNum);
			if (cell != null)
				cell.setCellType(CellType.BLANK);
		}
	}

	public static void copy(Sheet sheet, CellRangeAddress source, int destRow, int destCol) {
		for (int i = 0, n = source.getLastRow() - source.getFirstRow() + 1; i < n; i++)
			copy(sheet.getRow(source.getFirstRow() + i), source.getFirstColumn(), sheet.getRow(destRow + i), destCol, source.getLastColumn() - source.getFirstColumn() + 1);
	}

	public static void copy(Row source, int sourceColStart, Row target, int targetColStart, int colCount) {
		for (int i = 0; i < colCount; i++)
			copy(source, sourceColStart + i, target, targetColStart + i);
	}

	public static void copy(Row sourceRow, int sourceCol, Row targetRow, int targetCol) {
		Cell sourceCell = sourceRow.getCell(sourceCol);
		Cell targetCell = targetRow.getCell(targetCol);

		if (targetCell == null)
			targetCell = targetRow.createCell(targetCol);

		targetCell.setCellType(sourceCell != null ? sourceCell.getCellTypeEnum() : CellType.BLANK);

		if (sourceCell == null)
			return;

		switch (sourceCell.getCellTypeEnum()) {
		case BOOLEAN:
			targetCell.setCellValue(sourceCell.getBooleanCellValue());
			break;
		case ERROR:
			targetCell.setCellErrorValue(sourceCell.getErrorCellValue());
			break;
		case FORMULA:
			targetCell.setCellFormula(sourceCell.getCellFormula());
			break;
		case NUMERIC:
			targetCell.setCellValue(sourceCell.getNumericCellValue());
			break;
		case STRING:
			targetCell.setCellValue(sourceCell.getStringCellValue());
			break;
		case BLANK:
		case _NONE:
			break;
		}
	}

	public static void visitCells(Sheet sheet, CellRangeAddress address, CellVisitor visitor) {
		visitCells(sheet, address.getFirstRow(), address.getFirstColumn(), address.getLastRow() + 1, address.getLastColumn() + 1, visitor);
	}

	public static void visitCells(Sheet sheet, int rowStart, int colStart, int rowEnd, int colEnd, CellVisitor visitor) {
		for (int rowNum = rowStart; rowNum < rowEnd; rowNum++) {
			Row row = sheet.getRow(rowNum);

			if (row == null)
				continue;

			for (int colNum = colStart; colNum < colEnd; colNum++)
				visitor.visit(row, colNum, row.getCell(colNum));
		}
	}

	public static boolean regionIn(CellRangeAddress region, int rowStart, int colStart, int rowEnd, int colEnd) {
		return region.getFirstColumn() >= colStart && region.getFirstRow() >= rowStart
				&& region.getLastColumn() < colEnd && region.getLastRow() < rowEnd;
	}

	public static boolean regionOut(CellRangeAddress region, int rowStart, int colStart, int rowEnd, int colEnd) {
		return region.getFirstColumn() >= colEnd || region.getLastColumn() < colStart
				|| region.getFirstRow() >= rowEnd || region.getLastRow() < rowStart;
	}

	public static String toString(CellRangeAddress region) {
		return new StringBuilder(100).append('[').append(toLetter(region.getFirstColumn())).append(region.getFirstRow() + 1)
				.append(':').append(toLetter(region.getLastColumn())).append(region.getLastRow() + 1).append(']').toString();
	}

	public static String toLetter(int n) {
		StringBuilder str = new StringBuilder(10);
		while (n > 0) {
			str.insert(0, (char) ('A' + (n % 26)));
			n = n / 26;
		}
		return str.toString();
	}

	private static void updateMergedRegions(Sheet sheet, int rowStart, int colStart, int rowEnd, int colEnd,
			int rowShift, int colShift) {
		List<CellRangeAddress> updated = new ArrayList<CellRangeAddress>(sheet.getNumMergedRegions());

		for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
			CellRangeAddress region = sheet.getMergedRegion(i);

			boolean in = regionIn(region, rowStart, colStart, rowEnd, colEnd);
			boolean out = regionOut(region, rowStart, colStart, rowEnd, colEnd);

			if (!in && !out)
				throw new RuntimeException("PoiReport.updateMergedRegions(): " + toString(region));

			if (in) {
				region.setFirstColumn(region.getFirstColumn() + colShift);
				region.setLastColumn(region.getLastColumn() + colShift);
				region.setFirstRow(region.getFirstRow() + rowShift);
				region.setLastRow(region.getLastRow() + rowShift);
			}

			updated.add(region);
		}

		for (int i = sheet.getNumMergedRegions() - 1; i >= 0; i--)
			sheet.removeMergedRegion(i);

		for (CellRangeAddress region : updated)
			sheet.addMergedRegion(region);
	}
}
