package org.zenframework.z8.server.db.sql.functions;

import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;

public class CaseToken extends BaseCaseToken {
	public CaseToken() {
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) throws UnknownDatabaseException {
		String frm = "case " + (condition != null ? condition.format(vendor, options) : "");

		for(Pair<SqlToken, SqlToken> w : whens) {
			String when = w.getLeft().format(vendor, options, condition == null);
			frm += " when " + when + " then " + w.getRight().format(vendor, options, logicalContext);
		}

		frm += " else " + elseToken.format(vendor, options, logicalContext) + " end";
		return frm;
	}
}
