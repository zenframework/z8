package org.zenframework.z8.server.db.sql.fts;

import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.string;

public class FtsConfig extends OBJECT {

	public static class CLASS<T extends FtsConfig> extends OBJECT.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(FtsConfig.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new FtsConfig(container);
		}
	}

	public static final FtsConfig.CLASS<FtsConfig> Default = new FtsConfig.CLASS<FtsConfig>(null);

	public FtsConfig(IObject container) {
		super(container);
	}

	public string config = null;
	public FtsQueryType queryType = FtsQueryType.Plain;
	@SuppressWarnings("rawtypes")
	public RCollection weight = null;
	public integer normalization = new integer(0);
	public bool coatingDensity = bool.False;

}
