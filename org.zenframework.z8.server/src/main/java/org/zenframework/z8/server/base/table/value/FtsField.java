package org.zenframework.z8.server.base.table.value;

import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.db.sql.fts.Fts;
import org.zenframework.z8.server.db.sql.fts.FtsLike;
import org.zenframework.z8.server.db.sql.fts.FtsQuery;
import org.zenframework.z8.server.db.sql.fts.FtsRank;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.types.sql.sql_bool;
import org.zenframework.z8.server.types.sql.sql_decimal;
import org.zenframework.z8.server.types.sql.sql_string;

public class FtsField extends Field {
	static public int DefaultLength = 30;

	public static class CLASS<T extends FtsField> extends Field.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(FtsField.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new FtsField(container);
		}
	}

	public Fts.CLASS<? extends Fts> fts = new Fts.CLASS<Fts>(this);

	public FtsField(IObject container) {
		super(container);
		setDefault(new string(""));
		readOnly = bool.True;
		indexed = bool.True;
		aggregation = Aggregation.None;
	}

	@Override
	public FieldType type() {
		return FieldType.FtsVector;
	}

	public void set(String value) {
		set(new string(value));
	}

	@Override
	public string get() {
		return (string)internalGet();
	}

	public Fts getFts() {
		return fts.get();
	}

	@Override
	public primary parse(String value) {
		return new string(value);
	}

	@Override
	public String sqlPlaceholder(DatabaseVendor vendor) {
		return vendor.dialect().formatSqlPlaceholder(type(), fts.get());
	}

	public FtsField.CLASS<? extends FtsField> operatorAssign(string value) {
		set(value);
		return (FtsField.CLASS<?>)this.getCLASS();
	}

	public sql_string sql_string() {
		return new sql_string(new SqlField(this));
	}

	public sql_bool z8_ftsLike(sql_string query) {
		return new sql_bool(new FtsLike(new SqlField(this), new FtsQuery(query, getFts())));
	}

	public sql_decimal z8_ftsRank(sql_string query) {
		return new sql_decimal(new FtsRank(new SqlField(this), new FtsQuery(query, getFts()), getFts()));
	}
}
