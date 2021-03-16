package org.zenframework.z8.justintime.runtime;

import org.zenframework.z8.justintime.table.JustInTimeTools;
import org.zenframework.z8.justintime.table.Source;
import org.zenframework.z8.justintime.table.SourceType;
import org.zenframework.z8.server.runtime.AbstractRuntime;

public class JustInTimeRuntime extends AbstractRuntime {

	public JustInTimeRuntime() {
		addTable(new SourceType.CLASS<SourceType>(null));
		addTable(new Source.CLASS<Source>(null));

		addEntry(new JustInTimeTools.CLASS<JustInTimeTools>(null));
	}

}
