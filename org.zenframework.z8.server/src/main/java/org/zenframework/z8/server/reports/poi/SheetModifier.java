package org.zenframework.z8.server.reports.poi;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.zenframework.z8.server.utils.Counter;

public class SheetModifier {

	private final List<String> errors = new LinkedList<String>();

	private int rowsCreated = 0, rowsCopied = 0, cellsCreated = 0, cellsRemoved = 0, cellsCopied = 0;

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

	public String getStat() {
		return "{ rows (created/copied): " + rowsCreated + '/' + rowsCopied
				+ ", cells (created/removed/copied): " + cellsCreated + '/' + cellsRemoved + '/' + cellsCopied + " }";
	}

	public void insertBlock(Block block, Block.Direction direction) {
		Block moved = direction == Block.Direction.Horizontal
				? new Block(block.startRow(), block.startCol(), block.height(), getLastCellNum(block.startRow(), block.endRow()) - block.startCol())
				: new Block(block.startRow(), block.startCol(), getLastRowNum() - block.startRow(), block.width());

		int shift = direction == Block.Direction.Horizontal ? block.width() : block.height();

		copy(moved, moved.shift(direction, shift));
		clear(block);
	}

	public void clear(Block block) {
		for (int rowNum = block.startRow(), endRow = block.endRow(); rowNum < endRow; rowNum++) {
			Row row = sheet.getRow(rowNum);

			if (row == null)
				continue;

			for (int colNum = block.startCol(), endCol = block.endCol(); colNum < endCol; colNum++) {
				Cell cell = row.getCell(colNum);
				if (cell != null)
					cell.setCellType(CellType.BLANK);
			}
		}
	}

	public void copy(Block source, Block target) {
		// Copy direction (start-to-end / end-to-start)
		Counter counter = source.startRow() < target.startRow() ? new Counter(source.height() - 1, -1, source.height()) : new Counter(0, 1, source.height());

		while (counter.next()) {
			Row sourceRow = sheet.getRow(source.startRow() + counter.get());
			Row targetRow = sheet.getRow(target.startRow() + counter.get());

			if (sourceRow == null) {
				if (targetRow != null)
					removeCells(targetRow, target.startCol(), source.width());
				continue;
			}

			if (targetRow == null)
				targetRow = createRow(target.startRow() + counter.get());

			copyNonNull(sourceRow, source.startCol(), targetRow, target.startCol(), source.width());
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
				: new Block(block.startRow(), block.startCol(), getLastRowNum() - block.startRow(), block.width());

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
	private void copyNonNull(Row sourceRow, int sourceStartCol, Row targetRow, int targetStartCol, int count) {
		rowsCopied++;

		// Copy direction (start-to-end / end-to-start)
		Counter counter = sourceStartCol < targetStartCol ? new Counter(count - 1, -1, count) : new Counter(0, 1, count);

		while (counter.next()) {
			Cell sourceCell = sourceRow.getCell(sourceStartCol + counter.get());
			Cell targetCell = targetRow.getCell(targetStartCol + counter.get());

			if (sourceCell == null) {
				if (targetCell != null)
					targetRow.removeCell(targetCell);
				continue;
			}

			if (targetCell == null)
				targetCell = createCell(targetRow, targetStartCol + counter.get());

			copyNonNull(sourceCell, targetCell);
		}
	}

	private void copyNonNull(Cell source, Cell target) {
		cellsCopied++;

		target.setCellType(source.getCellTypeEnum());
		target.setCellStyle(source.getCellStyle());

		switch (source.getCellTypeEnum()) {
		case BOOLEAN:
			target.setCellValue(source.getBooleanCellValue());
			break;
		case ERROR:
			target.setCellErrorValue(source.getErrorCellValue());
			break;
		case FORMULA:
			target.setCellFormula(source.getCellFormula());
			break;
		case NUMERIC:
			target.setCellValue(source.getNumericCellValue());
			break;
		case STRING:
			target.setCellValue(source.getStringCellValue());
			break;
		case BLANK:
		case _NONE:
			break;
		}
	}

	private Row createRow(int rowNum) {
		rowsCreated++;
		return sheet.createRow(rowNum);
	}

	private Cell createCell(Row row, int col) {
		cellsCreated++;
		return row.createCell(col);
	}

	private int getLastRowNum() {
		return Math.min(sheet.getLastRowNum() + 1, 1000);
	}

	private int getLastCellNum(int startRow, int endRow) {
		int max = 0;

		for (int i = startRow; i < endRow; i++) {
			Row row = sheet.getRow(i);
			if (row != null)
				max = Math.max(max, row.getLastCellNum());
		}

		return Math.min(max + 1, 1000);
	}

	private void removeCells(Row row, int start, int count) {
		for (int i = 0; i < count; i++) {
			Cell cell = row.getCell(start + i);
			if (cell != null) {
				row.removeCell(cell);
				cellsRemoved++;
			}
		}
	}
}
