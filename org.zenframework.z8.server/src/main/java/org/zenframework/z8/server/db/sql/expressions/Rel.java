package org.zenframework.z8.server.db.sql.expressions;

import java.util.HashMap;

import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.db.sql.SqlStringToken;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.functions.If;
import org.zenframework.z8.server.db.sql.functions.date.TruncDay;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.sql.sql_bool;
import org.zenframework.z8.server.types.sql.sql_integer;

public class Rel extends Expression {
	final static HashMap<Operation, String> operations = new HashMap<Operation, String>();

	static {
		operations.put(Operation.Eq, "=");
		operations.put(Operation.NotEq, "<>");
		operations.put(Operation.LT, "<");
		operations.put(Operation.GT, ">");
		operations.put(Operation.LE, "<=");
		operations.put(Operation.GE, ">=");
	}

	public Rel(Field left, Operation operation, Field right) {
		this(new SqlField(left), operation, new SqlField(right));
	}

	public Rel(Field left, Operation operation, SqlToken right) {
		this(new SqlField(left), operation, right);
	}

	public Rel(SqlToken left, Operation operation, Field right) {
		this(left, operation, new SqlField(right));
	}

	public Rel(SqlToken left, Operation operation, SqlToken right) {
		super(left, operation, right);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		String rel = doFormat(vendor, options);

		if(!logicalContext)
			return new If(new SqlStringToken(rel, FieldType.Boolean), sql_integer.One, sql_integer.Zero).format(vendor, options);

		return rel;
	}

	protected String doFormat(DatabaseVendor vendor, FormatOptions options) {
		if(left.type() == FieldType.Date && right.type() == FieldType.Date) {
			boolean leftConst = left.isConst();
			boolean rightConst = right.isConst();

			SqlToken result;
			if(leftConst && rightConst) {
				int compare = left.date().truncDay().compare(right.date().truncDay());// ? sql_bool.True : sql_bool.False;
				switch(operation) {
				case Eq:
					result = compare == 0 ? sql_bool.True : sql_bool.False;
					break;
				case NotEq:
					result = compare != 0 ? sql_bool.True : sql_bool.False;
					break;
				case LT:
					result = compare < 0 ? sql_bool.True : sql_bool.False;
					break;
				case LE:
					result = compare <= 0 ? sql_bool.True : sql_bool.False;
					break;
				case GT:
					result = compare > 0 ? sql_bool.True : sql_bool.False;
					break;
				case GE:
					result = compare >= 0 ? sql_bool.True : sql_bool.False;
					break;
				default:
					throw new UnsupportedOperationException();
				}
				return result.format(vendor, options);
			}
			else if(leftConst && !rightConst) {
				date dt = left.date().truncDay();
				date dt1 = dt.addDay(1);
				String field = right.format(vendor, options);

				switch(operation) {
				case Eq:
					return "(" + dt.getTicks() + " <= " + field + " and " + field + " < " + dt1.getTicks() + ")";
				case NotEq:
					return "(" + field + " < " + dt.getTicks() + " or " + dt1.getTicks() + " <= " + field + ")";
				case LT:
					return "(" + dt1.getTicks() + " <= " + field + ")";
				case LE:
					return "(" + dt.getTicks() + " <= " + field + ")";
				case GT:
					return "(" + dt.getTicks() + " > " + field + ")";
				case GE:
					return "(" + dt1.getTicks() + " > " + field + ")";
				default:
					throw new UnsupportedOperationException();
				}
			} else if(!leftConst && rightConst) {
				date dt = right.date().truncDay();
				date dt1 = dt.addDay(1);
				String field = left.format(vendor, options);

				switch(operation) {
				case Eq:
					return "(" + dt.getTicks() + " <= " + field + " and " + field + " < " + dt1.getTicks() + ")";
				case NotEq:
					return "(" + field + " < " + dt.getTicks() + " or " + dt1.getTicks() + " <= " + field + ")";
				case LT:
					return "(" + field + " < " + dt.getTicks() + ")";
				case LE:
					return "(" + field + " < " + dt1.getTicks() + ")";
				case GT:
					return "(" + field + " >= " + dt1.getTicks() + ")";
				case GE:
					return "(" + field + " >= " + dt.getTicks() + ")";
				default:
					throw new UnsupportedOperationException();
				}
			} else {
				left = new TruncDay(left);
				right = new TruncDay(right);
			}
		}

		return left.format(vendor, options) + operations.get(this.operation) + right.format(vendor, options);
	}

	@Override
	public FieldType type() {
		return FieldType.Boolean;
	}
}
