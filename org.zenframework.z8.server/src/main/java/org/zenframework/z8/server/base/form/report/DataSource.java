package org.zenframework.z8.server.base.form.report;

import org.zenframework.z8.server.base.json.parser.JsonArray;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.reports.poi.CustomSource;
import org.zenframework.z8.server.reports.poi.JsonSource;
import org.zenframework.z8.server.reports.poi.QuerySource;
import org.zenframework.z8.server.reports.poi.SimpleSource;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.types.integer;

public class DataSource extends OBJECT {
	static public class CLASS<T extends DataSource> extends OBJECT.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(DataSource.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new DataSource(container);
		}
	}

	public DataSource(IObject container) {
		super(container);
	}

	private org.zenframework.z8.server.reports.poi.DataSource source;

	public org.zenframework.z8.server.reports.poi.DataSource get() {
		return source;
	}

	public DataSource set(org.zenframework.z8.server.reports.poi.DataSource source) {
		this.source = source;
		return this;
	}

	public void operatorAssign(Query.CLASS<? extends Query> source) {
		this.source = new QuerySource(source.get());
	}

	public void operatorAssign(JsonArray.CLASS<? extends JsonArray> source) {
		this.source = new JsonSource(source.get());
	}

	public void operatorAssign(CustomData.CLASS<? extends CustomData> source) {
		this.source = new CustomSource(source.get());
	}

	public void operatorAssign(OBJECT.CLASS<? extends OBJECT> source) {
		this.source = new SimpleSource(source.get());
	}

	public integer z8_getIndex() {
		return new integer(source.getIndex());
	}

	public static DataSource.CLASS<DataSource> newDefault() {
		DataSource.CLASS<DataSource> source = new DataSource.CLASS<DataSource>(null);
		source.get().set(SimpleSource.newDefault());
		return source;
	}
}
