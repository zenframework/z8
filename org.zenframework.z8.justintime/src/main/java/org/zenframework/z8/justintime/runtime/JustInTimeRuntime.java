package org.zenframework.z8.justintime.runtime;

import org.zenframework.z8.justintime.table.Source;
import org.zenframework.z8.justintime.table.SourcesView;
import org.zenframework.z8.server.runtime.AbstractRuntime;

public class JustInTimeRuntime extends AbstractRuntime {

	public JustInTimeRuntime() {
		addTable(new Source.CLASS<Source>(null));
		addSystemTool(new SourcesView.CLASS<SourcesView>(null));
	}

}
