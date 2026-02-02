package org.zenframework.z8.server.reports.poi;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.zenframework.z8.server.reports.poi.math.Axis;
import org.zenframework.z8.server.reports.poi.math.Block;
import org.zenframework.z8.server.reports.poi.math.Direction;
import org.zenframework.z8.server.reports.poi.math.Vector;

public class SheetModifier {

	private final List<String> errors = new LinkedList<String>();

	private int rowsCreated = 0, rowsCopied = 0, cellsCreated = 0, cellsRemoved = 0, cellsCopied = 0;

	private XSSFWorkbook workbook;
	private Sheet sheet, origin;

	public SheetModifier() {}

	public static interface CellVisitor {
		void visit(Row row, int colNum, Cell cell, Vector shift);
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

	public SheetModifier visitSheetCells(Vector shift, Block block, CellVisitor visitor) {
		return visitCells(sheet, shift, block, visitor);
	}

	public SheetModifier visitOriginCells(Block block, CellVisitor visitor) {
		return visitCells(origin, null, block, visitor);
	}

	private SheetModifier visitCells(Sheet sheet, Vector shift, Block block, CellVisitor visitor) {
		for (int rowNum = block.startRow(), endRow = block.endRow(), endCol = block.endCol(); rowNum < endRow; rowNum++) {
			Row row = sheet.getRow(rowNum);

			if (row == null)
				continue;

			for (int colNum = block.startCol(); colNum < endCol; colNum++)
				visitor.visit(row, colNum, row.getCell(colNum), shift);
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

	public SheetModifier applyGroupMerges(Vector groupStart, Vector groupResize, Set<Block> subtotalMerges, Block block,
			Axis axis) {
		if (subtotalMerges == null || subtotalMerges.isEmpty())
			return this;

		Vector stretchVector = groupResize.component(axis);

		if (stretchVector.mod() <= 0)
			return this;

		for (Block merge : subtotalMerges) {
			if (!merge.in(block))
				continue;

			Block movedMerge = merge.move(groupStart);
			Block stretchedMerge = movedMerge.resize(stretchVector);

			if (stretchedMerge.square() > 0)
				addMergedRegion(stretchedMerge);
		}

		return this;
	}

	public SheetModifier applyOuterMergedRegions(Block block, Block boundaries, Vector shift, Vector resize,
			Set<Block> boundariesMerge) {
		Map<Direction, Block> bands = boundaries.bands(block);

		for (int i = 0; i < origin.getNumMergedRegions(); i++) {
			Block region = new Block(origin.getMergedRegion(i));
			processOuterRegion(region, block, boundaries, bands, shift, resize);
		}

		if (boundariesMerge == null || boundariesMerge.isEmpty())
			return this;

		for (Block merge : boundariesMerge) {
			processOuterRegion(merge, block, boundaries, bands, shift, resize);
		}

		return this;
	}

	private void processOuterRegion(Block region, Block block, Block boundaries, Map<Direction, Block> bands,
			Vector shift, Vector resize) {
		if (region.in(block) || !region.in(boundaries))
			return;

		if (block.in(region)) {
			addMergedRegion(region.move(shift).resize(resize));
			return;
		}

		if (block.intersects(region))
			return;

		boolean inTop = region.in(bands.get(Direction.Top));
		boolean inBottom = region.in(bands.get(Direction.Bottom));
		boolean inLeft = region.in(bands.get(Direction.Left));
		boolean inRight = region.in(bands.get(Direction.Right));

		if (inLeft || inRight)
			region = region.resize(resize.component(Axis.Vertical));
		else if (inTop || inBottom)
			region = region.resize(resize.component(Axis.Horizontal));
		if (inRight)
			region = region.move(resize.component(Axis.Horizontal));
		if (inBottom)
			region = region.move(resize.component(Axis.Vertical));

		if (region.square() > 0)
			addMergedRegion(region.move(shift));
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
