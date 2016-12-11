package org.zenframework.z8.server.db.sql.functions;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IValue;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;

public class If extends SqlToken {
	private SqlToken condition;
	private SqlToken trueToken;
	private SqlToken falseToken;

	public If(SqlToken condition, SqlToken trueToken, SqlToken falseToken) {
		this.condition = condition;
		this.trueToken = trueToken;
		this.falseToken = falseToken;
	}

	@Override
	public void collectFields(Collection<IValue> fields) {
		condition.collectFields(fields);
		trueToken.collectFields(fields);
		falseToken.collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		CaseToken CaseToken = new CaseToken();
		CaseToken.addWhen(condition, trueToken);
		CaseToken.setElse(falseToken);

		String result = CaseToken.format(vendor, options);

		if(logicalContext) {
			result = "(" + result + ")=1"; // Where
		}

		return result;
	}

	@Override
	public FieldType type() {
		return trueToken.type();
	}
}
