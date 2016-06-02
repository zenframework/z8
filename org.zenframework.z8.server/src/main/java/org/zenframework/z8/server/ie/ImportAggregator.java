package org.zenframework.z8.server.ie;

import org.zenframework.z8.server.types.primary;

public interface ImportAggregator {

	primary aggregate(primary oldValue, primary newValue);

}
