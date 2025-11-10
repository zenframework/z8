package org.zenframework.z8.server.reports.poi;

import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.expression.Expression;
import org.zenframework.z8.server.runtime.OBJECT;

public class QuerySource extends DataSource {

	private final Map<String, Field> fields = new HashMap<String, Field>();
	private final Query query;

	private int count = -1;

	public QuerySource(Range range, Query query) {
		super(range);
		this.query = query;
	}

	@Override
	public OBJECT getObject() {
		return query;
	}

	@Override
	public void prepare(SheetModifier sheet) {
		super.prepare(sheet);
		collectFields(sheet);
	}

	@Override
	public void open() {
		super.open();
		query.saveState();
		query.read(fields.values(), query.sortFields(), null);
		//query.read(fields.values(), query.sortFields(), query.groupFields(), null, null, 0, -1);
	}

	@Override
	public void close() {
		super.close();
		query.restoreState();
	}

	@Override
	public int count() {
		return count >= 0 ? count : (count = query.count());
	}

	@Override
	protected boolean internalNext() {
		return query.next();
	}

	private void collectFields(SheetModifier sheet) {
		Expression.Extractor extractor = new Expression.Extractor() {
			@Override
			public void onObject(OBJECT object) {
				if (object instanceof Field && object.id().startsWith(query.id()))
					fields.put(object.id(), (Field) object);
			}
		};

		SheetModifier.CellVisitor visitor = new SheetModifier.CellVisitor() {
			@Override
			public void visit(Row row, int colNum, Cell cell) {
				if (cell != null && cell.getCellTypeEnum() == CellType.STRING)
					getExpression().extractObjects(cell.getStringCellValue(), extractor);
			}
		};

		sheet.visitCells(range.getBlock(), visitor);

		for (Field.CLASS<? extends Field> field : query.extraFields)
			fields.put(field.id(), field.get());
	}
}
