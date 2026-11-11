package org.zenframework.z8.server.base.form.report;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.base.json.parser.JsonArray;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.Aggregation;
import org.zenframework.z8.server.reports.poi.CustomSource;
import org.zenframework.z8.server.reports.poi.JsonSource;
import org.zenframework.z8.server.reports.poi.QuerySource;
import org.zenframework.z8.server.reports.poi.SimpleSource;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.string;

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

	private final Map<String, Metadata.CLASS<Metadata>> metadata = new HashMap<String, Metadata.CLASS<Metadata>>();

	public org.zenframework.z8.server.reports.poi.DataSource get() {
		return source;
	}

	public DataSource set(org.zenframework.z8.server.reports.poi.DataSource source) {
		this.source = source.setMetadata(this);
		return this;
	}

	public void operatorAssign(Query.CLASS<? extends Query> source) {
		set(new QuerySource(source.get()));
	}

	public void operatorAssign(JsonArray.CLASS<? extends JsonArray> source) {
		set(new JsonSource(source.get()));
	}

	public void operatorAssign(CustomData.CLASS<? extends CustomData> source) {
		set(new CustomSource(source.get()));
	}

	public void operatorAssign(OBJECT.CLASS<? extends OBJECT> source) {
		set(new SimpleSource(source.get()));
	}

	public integer z8_getIndex() {
		return new integer(source.getIndex());
	}

	public Metadata.CLASS<Metadata> z8_metadata(string id) {
		return metadata(id.get());
	}

	public Metadata.CLASS<Metadata> metadata(String id) {
		Metadata.CLASS<Metadata> result = metadata.get(id);

		if (result == null) {
			result = new Metadata.CLASS<Metadata>(this);
			result.setIndex(id);
			metadata.put(id, result);
		}

		return result;
	}

	public Collection<String> getMetadataIds() {
		return metadata.keySet();
	}

	public Aggregation getAggregation(String id, Aggregation defaultAggregation) {
		Metadata.CLASS<Metadata> meta = metadata.get(id);

		if (meta == null)
			return defaultAggregation;

		Aggregation aggregation = meta.get().aggregation;

		return aggregation != Aggregation.None ? aggregation : defaultAggregation;
	}

	public static DataSource.CLASS<DataSource> newDefault() {
		DataSource.CLASS<DataSource> source = new DataSource.CLASS<DataSource>(null);
		source.get().set(SimpleSource.newDefault());
		return source;
	}
}
