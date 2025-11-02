package org.zenframework.z8.server.reports.poi;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

public class Util {

	private Util() {}

	public static interface CellVisitor {
		void visit(Row row, int colNum, Cell cell);
	}

	public static Block multiplyBlock(Sheet sheet, Block block, int count, Block.Direction direction) {
		if (count < 1)
			return block; // TODO Delete cells

		insertBlock(sheet, block.shift(direction, 1).stretch(direction, count - 1), direction);

		for (int i = 1; i < count; i++)
			copy(sheet, block, block.shift(direction, i));

		return block.stretch(direction, count);
	}

	public static void insertBlock(Sheet sheet, Block block, Block.Direction direction) {
		if (direction == Block.Direction.Horizontal)
			insertBlockHorizShift(sheet, block);
		else
			insertBlockVertShift(sheet, block);
	}

	private static void insertBlockHorizShift(Sheet sheet, Block block) {
		Block updatedRange = new Block(block.startRow(), block.startCol(),
				block.height(), sheet.getRow(block.startRow()).getLastCellNum() + 1 - block.startCol());

		for (int rowNum = block.startRow(), endRow = block.endRow(), endCol = block.endCol(); rowNum < endRow; rowNum++) {
			Row row = sheet.getRow(rowNum);

			if (row == null)
				row = sheet.createRow(rowNum);

			// Ensure columns exist
			for (int i = 0; i < block.width(); i++) {
				Cell cell = row.getCell(endCol + i);

				if (cell == null)
					cell = row.createCell(endCol + i, CellType.BLANK);
			}

			for (int colNum = endCol - 1; colNum >= block.startCol(); colNum--)
				copy(row, colNum, row, colNum + block.width());
		}

		clear(sheet, block);
		moveMergedRegions(sheet, updatedRange, 0, block.width());
	}

	private static void insertBlockVertShift(Sheet sheet, Block block) {
		Block updatedRange = new Block(block.startRow(), block.startCol(),
				sheet.getLastRowNum() + 1 - block.startRow(), block.width());

		// Ensure rows exist
		for (int i = 0; i < block.height(); i++) {
			Row row = sheet.getRow(updatedRange.endRow() + i);

			if (row != null)
				continue;

			row = sheet.createRow(updatedRange.endRow() + i);

			for (int colNum = 0; colNum < updatedRange.endCol(); colNum++)
				row.createCell(colNum, CellType.BLANK);
		}

		// Copy rows from end to start
		for (int rowNum = updatedRange.endRow() - 1; rowNum >= block.startRow(); rowNum--)
			copy(sheet.getRow(rowNum), block.startCol(), sheet.getRow(rowNum + block.height()), block.startCol(), block.width());

		clear(sheet, block);
		moveMergedRegions(sheet, updatedRange, block.height(), 0);
	}

	public static void clear(Sheet sheet, Block block) {
		for (int rowNum = block.startRow(), endRow = block.endRow(); rowNum < endRow; rowNum++) {
			Row row = sheet.getRow(rowNum);
			for (int colNum = block.startCol(), endCol = block.endCol(); colNum < endCol; colNum++) {
				Cell cell = row.getCell(colNum);
				if (cell != null)
					cell.setCellType(CellType.BLANK);
			}
		}
	}

	public static void copy(Sheet sheet, Block source, Block target) {
		for (int i = 0, n = source.height(); i < n; i++)
			copy(sheet.getRow(source.startRow() + i), source.startCol(), sheet.getRow(target.startRow() + i), target.startCol(), source.width());
	}

	public static void copy(Row source, int sourceStartCol, Row target, int targetStartCol, int colCount) {
		for (int i = 0; i < colCount; i++)
			copy(source, sourceStartCol + i, target, targetStartCol + i);
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

	public static void visitCells(Sheet sheet, Block block, CellVisitor visitor) {
		for (int rowNum = block.startRow(), endRow = block.endRow(), endCol = block.endCol(); rowNum < endRow; rowNum++) {
			Row row = sheet.getRow(rowNum);

			if (row == null)
				continue;

			for (int colNum = block.startCol(); colNum < endCol; colNum++)
				visitor.visit(row, colNum, row.getCell(colNum));
		}
	}

	private static void moveMergedRegions(Sheet sheet, Block block, int rowShift, int colShift) {
		List<CellRangeAddress> updated = new ArrayList<CellRangeAddress>(sheet.getNumMergedRegions());

		for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
			CellRangeAddress region = sheet.getMergedRegion(i);
			Block regionRange = new Block(region);

			boolean in = regionRange.in(block);
			boolean out = regionRange.out(block);

			if (!in && !out)
				throw new RuntimeException("PoiReport.updateMergedRegions(): " + regionRange.toAddress());

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
