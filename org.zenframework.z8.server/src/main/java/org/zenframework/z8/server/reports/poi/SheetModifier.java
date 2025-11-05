package org.zenframework.z8.server.reports.poi;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

public class SheetModifier {

	private final List<String> errors = new LinkedList<String>();

	private Sheet sheet;

	public SheetModifier() {}

	public static interface CellVisitor {
		void visit(Row row, int colNum, Cell cell);
	}

	public List<String> getErrors() {
		return errors;
	}

	public Sheet getSheet() {
		return sheet;
	}

	public SheetModifier setSheet(Sheet sheet) {
		this.sheet = sheet;
		return this;
	}

	public void insertBlock(Block block, Block.Direction direction) {
		if (direction == Block.Direction.Horizontal)
			insertBlockHorizShift(block);
		else
			insertBlockVertShift(block);
	}

	private void insertBlockHorizShift(Block block) {
		int endCol = getLastCellNum(block.startRow(), block.endRow());

		for (int rowNum = block.startRow(), endRow = block.endRow(); rowNum < endRow; rowNum++) {
			Row row = sheet.getRow(rowNum);

			if (row == null)
				row = sheet.createRow(rowNum);

			// Ensure columns exist
			for (int i = 0; i < block.width(); i++) {
				Cell cell = row.getCell(endCol + i);

				if (cell == null)
					cell = row.createCell(endCol + i, CellType.BLANK);
			}

			// Copy cols from end to start
			for (int colNum = endCol - 1; colNum >= block.startCol(); colNum--)
				copy(row, colNum, row, colNum + block.width());
		}

		clear(block);
	}

	private void insertBlockVertShift(Block block) {
		int endRow = sheet.getLastRowNum() + 1 - block.startRow();

		// Ensure rows exist
		for (int i = 0; i < block.height(); i++) {
			Row row = sheet.getRow(endRow + i);

			if (row != null)
				continue;

			row = sheet.createRow(endRow + i);

			for (int colNum = 0; colNum < block.endCol(); colNum++)
				row.createCell(colNum, CellType.BLANK);
		}

		// Copy rows from end to start
		for (int rowNum = endRow - 1; rowNum >= block.startRow(); rowNum--)
			copy(sheet.getRow(rowNum), block.startCol(), sheet.getRow(rowNum + block.height()), block.startCol(), block.width());

		clear(block);
	}

	public void clear(Block block) {
		for (int rowNum = block.startRow(), endRow = block.endRow(); rowNum < endRow; rowNum++) {
			Row row = sheet.getRow(rowNum);
			for (int colNum = block.startCol(), endCol = block.endCol(); colNum < endCol; colNum++) {
				Cell cell = row.getCell(colNum);
				if (cell != null)
					cell.setCellType(CellType.BLANK);
			}
		}
	}

	public void copy(Block source, Block target) {
		for (int i = 0, n = source.height(); i < n; i++)
			copy(sheet.getRow(source.startRow() + i), source.startCol(), sheet.getRow(target.startRow() + i), target.startCol(), source.width());
	}

	private void copy(Row source, int sourceStartCol, Row target, int targetStartCol, int colCount) {
		for (int i = 0; i < colCount; i++)
			copy(source, sourceStartCol + i, target, targetStartCol + i);
	}

	private void copy(Row sourceRow, int sourceCol, Row targetRow, int targetCol) {
		Cell sourceCell = sourceRow.getCell(sourceCol);
		Cell targetCell = targetRow.getCell(targetCol);

		if (targetCell == null)
			targetCell = targetRow.createCell(targetCol);

		targetCell.setCellType(sourceCell != null ? sourceCell.getCellTypeEnum() : CellType.BLANK);

		if (sourceCell == null)
			return;

		targetCell.setCellStyle(sourceCell.getCellStyle());

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

	public void visitCells(Block block, CellVisitor visitor) {
		for (int rowNum = block.startRow(), endRow = block.endRow(), endCol = block.endCol(); rowNum < endRow; rowNum++) {
			Row row = sheet.getRow(rowNum);

			if (row == null)
				continue;

			for (int colNum = block.startCol(); colNum < endCol; colNum++)
				visitor.visit(row, colNum, row.getCell(colNum));
		}
	}

	public void updateMergedRegions(Block boundaries, Block block, Block.Direction direction, int count) {
		List<CellRangeAddress> regions = new ArrayList<CellRangeAddress>(sheet.getNumMergedRegions());
		Block affected = direction == Block.Direction.Horizontal
				? new Block(block.startRow(), block.startCol(), block.height(), getLastCellNum(block.startRow(), block.endRow()) + 1 - block.startCol())
				: new Block(block.startRow(), block.startCol(), sheet.getLastRowNum() + 1 - block.startRow(), block.width());

		for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
			CellRangeAddress region = sheet.getMergedRegion(i);
			Block regionBlock = new Block(region);

			if (regionBlock.out(affected)) {
				// Merged region totally out of affected block -> leave unchanged
				regions.add(region);
			} else if (regionBlock.in(block)) {
				// Merged region totally in multiplied block -> copy
				regions.add(region);
				for (int j = 1; j < count; j++)
					regions.add(regionBlock.shift(block.vector(direction, j)).toCellRangeAddress());
			} else if (block.in(regionBlock)) {
				// Multiplied block totally in merged region  -> stretch
				regions.add(regionBlock.stretch(block.vector(direction, count - 1)).toCellRangeAddress());
			} else if (block.out(regionBlock) && regionBlock.in(affected)) {
				// Merged region out of multiplied block, but in affected block -> move
				regions.add(regionBlock.shift(block.vector(direction, count - 1)).toCellRangeAddress());
			} else {
				// Other -> remove
			}
		}

		for (int i = sheet.getNumMergedRegions() - 1; i >= 0; i--)
			sheet.removeMergedRegion(i);

		for (CellRangeAddress region : regions) {
			try {
				sheet.addMergedRegion(region);
			} catch (Throwable e) {
				errors.add(e.toString());
			}
		}
	}
/*
	private static void moveMergedRegions(Block block, int rowShift, int colShift) {
		List<CellRangeAddress> updated = new ArrayList<CellRangeAddress>(sheet.getNumMergedRegions());

		for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
			CellRangeAddress region = sheet.getMergedRegion(i);
			Block regionBlock = new Block(region);

			boolean in = regionBlock.in(block);
			boolean out = regionBlock.out(block);

			if (!in && !out)
				throw new RuntimeException("PoiReport.moveMergedRegions(): " + regionBlock.toAddress());

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
*/
	private int getLastCellNum(int startRow, int endRow) {
		int max = 0;

		for (int i = startRow; i < endRow; i++)
			max = Math.max(max, sheet.getRow(i).getLastCellNum());

		return max;
	}
}
