package org.zenframework.z8.server.db.sql.fts;

import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.string;

public class Fts extends OBJECT {

	public static class CLASS<T extends Fts> extends OBJECT.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(Fts.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new Fts(container);
		}
	}

	private static final string DefaultFtsConfiguration = defaultFtsConfiguration();

	// (по умолчанию): длина документа не учитывается
	public static final integer NormDefault = new integer(0);
	// ранг документа делится на 1 + логарифм длины документа
	public static final integer NormLenLog = new integer(1);
	// ранг документа делится на его длину
	public static final integer NormLen = new integer(2);
	// ранг документа делится на среднее гармоническое расстояние между блоками (это реализовано только в ts_rank_cd)
	public static final integer NormHarmonic = new integer(4);
	// ранг документа делится на число уникальных слов в документе
	public static final integer NormUniqueWords = new integer(8);
	// ранг документа делится на 1 + логарифм числа уникальных слов в документе
	public static final integer NormUniqueWordsLog = new integer(16);
	// ранг делится своё же значение + 1
	public static final integer NormDivSelf = new integer(32);

	public Fts(IObject container) {
		super(container);
	}

	public string configuration = DefaultFtsConfiguration;
	public FtsQueryType queryType = FtsQueryType.Plain;
	@SuppressWarnings("rawtypes")
	public RCollection weight = null;
	public integer normalization = new integer(0);
	public bool coatingDensity = bool.False;

	private static string defaultFtsConfiguration() {
		String ftsConfiguration = ServerConfig.ftsConfiguration();
		return ftsConfiguration != null ? new string(ftsConfiguration) : null;
	}
}
