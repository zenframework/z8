package org.zenframework.z8.server.db.sql.functions.string;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.functions.conversion.ToString;

public class Like extends SqlToken {
	private SqlToken string;
	private SqlToken like;
	private SqlToken escape;

	public Like(Field field, SqlToken like) {
		this(new SqlField(field), like, null);
	}

	public Like(Field field, SqlToken like, SqlToken escape) {
		this(new SqlField(field), like, escape);
	}

	public Like(SqlToken string, SqlToken like) {
		this(string, like, null);
	}
	
	public Like(SqlToken string, SqlToken like, SqlToken escape) {
		this.string = string;
		this.like = like;
		this.escape = escape;
	}

	@Override
	public void collectFields(Collection<IField> fields) {
		string.collectFields(fields);
		like.collectFields(fields);
		if(escape != null)
			escape.collectFields(fields);
	}

	@Override
	public FieldType type() {
		return FieldType.Boolean;
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		SqlToken valueToken = string.type() != FieldType.String ? new ToString(string) : string;
		SqlToken likeToken = like.type() != FieldType.String ? new ToString(like) : like;
		SqlToken escapeToken = escape != null && escape.type() != FieldType.String ? new ToString(escape) : escape;

		return valueToken.format(vendor, options) + " LIKE " + likeToken.format(vendor, options) + (escapeToken == null ? "" : " ESCAPE " + escapeToken.format(vendor, options));
	}
}
