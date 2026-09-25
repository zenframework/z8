package org.zenframework.z8.server.reports.poi;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.db.Select;
import org.zenframework.z8.server.expression.Expression;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.types.decimal;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.reports.poi.math.Vector;

public class QuerySource extends DataSource {

	private final Map<String, Field> fields = new HashMap<String, Field>();
	private final Query query;

	private int count = -1;
	private boolean aggregated = false;

	public QuerySource(Query query) {
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

		if (range.isAggregation())
			aggregated = query.aggregate(fields.values());
		else
			query.read(fields.values(), query.sortFields(), null);
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
	public boolean next() {
		super.next();

		if (range.isAggregation()) {
			try {
				return aggregated;
			} finally {
				aggregated = false;
			}
		}

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
			public void visit(Row row, int colNum, Cell cell, Vector shift) {
				// TODO Use CellType with POI-16
				if (cell != null && cell.getCellType() == Cell.CELL_TYPE_STRING)
					getExpression().extractObjects(cell.getStringCellValue(), extractor);
			}
		};

		sheet.visitOriginCells(range.getBlock(), visitor);

		if (range.getSubtotalBlock() != null)
			sheet.visitOriginCells(range.getSubtotalBlock(), visitor);

		for (Field.CLASS<? extends Field> field : query.extraFields)
			fields.put(field.id(), field.get());
	}

	public Collection<Field> getFields() {
		return fields.values();
	}

	@Override
	public Object getCurrentValue(String id) {
		Field field = fields.get(id);

		if (field == null)
			return null;

		Select cursor = field.getCursor();

		return cursor == null || cursor.isClosed() ? null : field.get();
	}

	@Override
	public Collection<String> getCurrentValueIds() {
		return fields.keySet();
	}

	@Override
	public String normalize(String id) {
		return id;
	}

	@Override
	protected void fillAggregated(AggregatedSource source) {
		for (String id : getAggregatedIds()) {
			OBJECT member = getMember(source.getObject(), relative(id));

			if (!(member instanceof Field))
				continue;

			Field field = (Field) member;
			field.set(convert(field, source.getValue(id)));
		}
	}

	private static primary convert(Field field, Object value) {
		if (!(value instanceof primary))
			return null;

		primary result = (primary) value;

		switch (field.type()) {
		case Integer:
			return result instanceof decimal ? new integer((decimal) result) : result;
		case Decimal:
			return result instanceof integer ? new decimal((integer) result) : result;
		default:
			return result;
		}
	}

}
