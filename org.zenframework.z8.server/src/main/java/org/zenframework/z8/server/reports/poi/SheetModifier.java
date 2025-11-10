package org.zenframework.z8.server.reports.poi;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.zenframework.z8.server.reports.poi.math.Block;
import org.zenframework.z8.server.reports.poi.math.Direction;
import org.zenframework.z8.server.reports.poi.math.Vector;

public class SheetModifier {

	private static class SearchConfig {
		final Block[] bandBefore = new Block[Direction.values().length];
		final Block[] bandAfter = new Block[Direction.values().length];
		final Block[] band = new Block[Direction.values().length];

		SearchConfig(Block source, Block boundaries) {
			for (Direction direction : Direction.values()) {
				bandBefore[direction.ordinal()] = boundaries.bandBefore(source, direction);
				bandAfter[direction.ordinal()] = boundaries.bandAfter(source, direction);
				band[direction.ordinal()] = boundaries.band(source, direction);
			}
		}

		boolean isBefore(Block region, Direction direction) {
			return region.in(bandBefore[direction.ordinal()]);
		}

		boolean isAfter(Block region, Direction direction) {
			return region.in(bandAfter[direction.ordinal()]);
		}

		boolean intersectsBand(Block region, Direction direction) {
			return region.intersects(band[direction.ordinal()]);
		}
	}

	private final List<String> errors = new LinkedList<String>();

	private int rowsCreated = 0, rowsCopied = 0, cellsCreated = 0, cellsRemoved = 0, cellsCopied = 0;

	private XSSFWorkbook workbook;
	private Sheet sheet, origin;

	public SheetModifier() {}

	public static interface CellVisitor {
		void visit(Row row, int colNum, Cell cell);
	}

	public List<String> getErrors() {
		return errors;
	}

	public SheetModifier setWorkbook(XSSFWorkbook workbook) {
		this.workbook = workbook;
		return this;
	}

	public SheetModifier setSheet(int sheetIndex) {
		if (origin != null)
			workbook.removeSheetAt(workbook.getSheetIndex(origin));

		sheet = sheetIndex >= 0 ? workbook.getSheetAt(sheetIndex) : null;
		origin = sheetIndex >= 0 ? workbook.cloneSheet(sheetIndex) : null;

		if (sheet != null)
			removeMergedRegions();

		return this;
	}

	public Sheet getSheet() {
		return sheet;
	}

	public SheetModifier close() {
		return setSheet(-1);
	}

	public String getStat() {
		return "{ rows (created/copied): " + rowsCreated + '/' + rowsCopied
				+ ", cells (created/removed/copied): " + cellsCreated + '/' + cellsRemoved + '/' + cellsCopied + " }";
	}

	public Block getBoundaries() {
		int rows = getLastRowNum();
		return new Block(0, 0, rows, getLastColNum(0, rows));
	}

	public SheetModifier clear(Block block) {
		for (int i = 0; i < block.height(); i++) {
			Row row = sheet.getRow(block.startRow() + i);
			if (row != null)
				removeCells(row, block.startCol(), block.width());
		}

		return this;
	}

	public SheetModifier copy(Block source, Vector target, boolean relative) {
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

	public SheetModifier copyMergedRegions(Block source, Vector target, boolean relative) {
		if (!relative)
			target = target.sub(source.start());

		for (int i = origin.getNumMergedRegions() - 1; i >= 0; i--) {
			Block region = new Block(origin.getMergedRegion(i));
			if (region.in(source))
				sheet.addMergedRegion(region.move(target).toCellRangeAddress());
		}

		return this;
	}

	public SheetModifier applyInnerMergedRegions(Block source, Vector shift, Collection<Block> exclusions) {
		for (int i = 0; i < origin.getNumMergedRegions(); i++) {
			Block region = new Block(origin.getMergedRegion(i));

			if (region.in(source) && !region.inOneOf(exclusions))
				addMergedRegion(region.move(shift));
		}

		return this;
	}

	public SheetModifier applyOuterMergedRegions(Block block, Block boundaries, Vector shift, Vector stretch) {
		SearchConfig search = new SearchConfig(block, boundaries);

		for (int i = 0; i < origin.getNumMergedRegions(); i++) {
			Block region = new Block(origin.getMergedRegion(i));

			if (region.in(block) || !region.in(boundaries))
				continue;

			boolean applied = false;

			for (Direction direction : Direction.values()) {
				if (search.isBefore(region, direction))
					region = region.move(shift);
				else if (search.isAfter(region, direction))
					region = region.move(shift).move(stretch.component(direction));
				else
					continue;

				addMergedRegion(region);

				applied = true;
				break;
			}

			if (applied)
				continue;

			for (Direction direction : Direction.values()) {
				if (search.intersectsBand(region, direction)) {
					addMergedRegion(region.move(shift).resize(stretch.component(direction)));
					break;
				}
			}
		}
/*
		// TODO Exclude existing merged regions
		for (Block part : targetBoundaries.bandExclusive(target, direction)) {
			try {
				addMergedRegion(part);
			} catch (IllegalStateException e) {}
		}
*/
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
