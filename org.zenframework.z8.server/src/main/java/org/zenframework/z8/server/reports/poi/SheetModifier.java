package org.zenframework.z8.server.reports.poi;

import java.util.LinkedList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class SheetModifier {

	private final List<String> errors = new LinkedList<String>();

	private int rowsCreated = 0, rowsCopied = 0, cellsCreated = 0, cellsRemoved = 0, cellsCopied = 0;

	private XSSFWorkbook workbook;
	private Sheet sheet, origin, origins[];

	public SheetModifier() {}

	public static interface CellVisitor {
		void visit(Row row, int colNum, Cell cell);
	}

	public List<String> getErrors() {
		return errors;
	}

	public SheetModifier open(XSSFWorkbook workbook) {
		if (this.workbook != null) {
			for (Sheet origin : origins)
				this.workbook.removeSheetAt(this.workbook.getSheetIndex(origin));
			sheet = origin = null;
		}

		this.workbook = workbook;

		if (workbook != null) {
			origins = new Sheet[workbook.getNumberOfSheets()];
			for (int i = 0; i < origins.length; i++) {
				origins[i] = workbook.cloneSheet(i);
				Sheet sheet = workbook.getSheetAt(i);
				for (int j = sheet.getNumMergedRegions() - 1; j >= 0; j--)
					sheet.removeMergedRegion(j);
			}
		}

		return this;
	}

	public SheetModifier setSheet(int sheetIndex) {
		sheet = workbook.getSheetAt(sheetIndex);
		origin = origins[sheetIndex];
		return this;
	}

	public Sheet getSheet() {
		return sheet;
	}

	public SheetModifier close() {
		return open(null);
	}

	public String getStat() {
		return "{ rows (created/copied): " + rowsCreated + '/' + rowsCopied
				+ ", cells (created/removed/copied): " + cellsCreated + '/' + cellsRemoved + '/' + cellsCopied + " }";
	}

	public Block getBoundaries() {
		int rows = getLastRowNum();
		return new Block(0, 0, rows, getLastColNum(0, rows));
	}

	public Block getBoundariesAfter(Block block, Block.Direction direction) {
		Block boundaries = getBoundaries();
		return direction == Block.Direction.Horizontal ? new Block(block.startRow(), block.endCol(), block.height(), boundaries.width() - block.endCol())
				: new Block(block.endRow(), block.startCol(), boundaries.height() - block.endRow(), block.width());
	}

	public SheetModifier insertBlock(Block block, Block.Direction direction) {
		Block moved = direction == Block.Direction.Horizontal
				? new Block(block.startRow(), block.startCol(), block.height(), getLastColNum(block.startRow(), block.endRow()) - block.startCol())
				: new Block(block.startRow(), block.startCol(), getLastRowNum() - block.startRow(), block.width());

		return copy(moved, moved.vector(direction, 1), true).clear(block); // TODO check
	}

	public SheetModifier clear(Block block) {
		for (int rowNum = block.startRow(), endRow = block.endRow(); rowNum < endRow; rowNum++) {
			Row row = sheet.getRow(rowNum);

			if (row == null)
				continue;

			for (int colNum = block.startCol(), endCol = block.endCol(); colNum < endCol; colNum++) {
				Cell cell = row.getCell(colNum);
				if (cell != null)
					row.removeCell(cell);
					// cell.setCellType(CellType.BLANK);
			}
		}

		return this;
	}

	public SheetModifier copy(Block source, Block.Vector target, boolean relative) {
		if (relative)
			target = target.add(source.start());

		for (int i = 0; i < source.height(); i++) {
			Row sourceRow = origin.getRow(source.startRow() + i);
			Row targetRow = sheet.getRow(target.row() + i);

			if (sourceRow == null) {
				if (targetRow != null)
					removeCells(targetRow, target.col(), source.width());
				continue;
			}

			if (targetRow == null)
				targetRow = createRow(target.row() + i);

			copyNonNull(sourceRow, source.startCol(), targetRow, target.col(), source.width());
		}

		return this;
	}

	public SheetModifier visitCells(Block block, CellVisitor visitor) {
		for (int rowNum = block.startRow(), endRow = block.endRow(), endCol = block.endCol(); rowNum < endRow; rowNum++) {
			Row row = sheet.getRow(rowNum);

			if (row == null)
				continue;

			for (int colNum = block.startCol(); colNum < endCol; colNum++)
				visitor.visit(row, colNum, row.getCell(colNum));
		}

		return this;
	}

	public SheetModifier addMergedRegion(Block block) {
		sheet.addMergedRegion(block.toCellRangeAddress());
		return this;
	}

	public SheetModifier removeMergedRegions() {
		for (int i = sheet.getNumMergedRegions() - 1; i >= 0; i--)
			sheet.removeMergedRegion(i);

		return this;
	}

	public SheetModifier removeMergedRegions(Block block) {
		for (int i = sheet.getNumMergedRegions() - 1; i >= 0; i--)
			if (new Block(sheet.getMergedRegion(i)).intersects(block))
				sheet.removeMergedRegion(i);

		return this;
	}

	public SheetModifier copyMergedRegions(Block source, Block.Vector target, boolean relative) {
		if (!relative)
			target = target.sub(source.start());

		for (int i = origin.getNumMergedRegions() - 1; i >= 0; i--) {
			Block region = new Block(origin.getMergedRegion(i));
			if (region.in(source))
				sheet.addMergedRegion(region.shift(target).toCellRangeAddress());
		}

		return this;
	}

	public SheetModifier applyMergedRegions(Block sourceBoundaries, Block source, Block targetBoundaries, Block target, Block.Direction direction) {
		Block bandBefore = sourceBoundaries.bandBefore(source, direction);
		Block bandAfter = sourceBoundaries.bandAfter(source, direction);
		Block band = sourceBoundaries.band(source, direction);
		Block.Vector shift = targetBoundaries.diffStart(sourceBoundaries);
		Block.Vector stretch = targetBoundaries.diffSize(sourceBoundaries);

		for (int i = 0; i < origin.getNumMergedRegions(); i++) {
			Block region = new Block(origin.getMergedRegion(i));

			if (!region.in(sourceBoundaries) || region.in(source))
				continue;

			if (region.in(bandBefore))
				addMergedRegion(region.shift(shift));
			else if (region.in(bandAfter))
				addMergedRegion(region.shift(shift).shift(stretch.component(direction)));
			else if (region.intersects(band))
				addMergedRegion(region.shift(shift).stretch(stretch.component(direction)));
		}

		return this;
	}

	private void copyNonNull(Row sourceRow, int sourceStartCol, Row targetRow, int targetStartCol, int count) {
		rowsCopied++;

		for (int i = 0; i < count; i++) {
			Cell sourceCell = sourceRow.getCell(sourceStartCol + i);
			Cell targetCell = targetRow.getCell(targetStartCol + i);

			if (sourceCell == null) {
				if (targetCell != null)
					targetRow.removeCell(targetCell);
				continue;
			}

			if (targetCell == null)
				targetCell = createCell(targetRow, targetStartCol + i);

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

	private int getLastColNum(int startRow, int endRow) {
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
