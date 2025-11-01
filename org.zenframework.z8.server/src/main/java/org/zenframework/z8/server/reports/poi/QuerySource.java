package org.zenframework.z8.server.reports.poi;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.expression.Expression;
import org.zenframework.z8.server.runtime.CLASS;
import org.zenframework.z8.server.runtime.OBJECT;

public class QuerySource extends DataSource {

	private final Query query;

	public QuerySource(Range range, Query query) {
		super(range);
		this.query = query;
	}

	@Override
	public OBJECT getObject() {
		return query;
	}

	@Override
	public void prepare(XSSFWorkbook workbook) {
		super.prepare(workbook);

		expression.setGetter(new Expression.Getter() {
			@Override
			@SuppressWarnings("rawtypes")
			public Object getValue(Object value) {
				if (value instanceof CLASS)
					value = ((CLASS) value).get();
				return value instanceof Field ? ((Field) value).get() : value;
			}
		});

		query.read(collectFields(workbook));
	}

	@Override
	public int count() {
		return query.count();
	}

	@Override
	protected boolean internalNext() {
		return query.next();
	}

	private Collection<Field> collectFields(XSSFWorkbook workbook) {
		Map<String, Field> fields = new HashMap<String, Field>();

		Expression.Extractor extractor = new Expression.Extractor() {
			@Override
			public void onObject(OBJECT object) {
				if (object instanceof Field)
					fields.put(object.id(), (Field) object);
			}
		};

		Util.CellVisitor visitor = new Util.CellVisitor() {
			@Override
			public void visit(Row row, int colNum, Cell cell) {
				if (cell != null && cell.getCellTypeEnum() == CellType.STRING)
					expression.extractObjects(cell.getStringCellValue(), extractor);
			}
		};

		Util.visitCells(workbook.getSheetAt(range.getSheetIndex()), range.getAddress(), visitor);

		return fields.values();
	}
}
