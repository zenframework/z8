package org.zenframework.z8.compiler.error;

import org.zenframework.z8.compiler.workspace.Resource;

public interface IBuildMessageConsumer {
	int getErrorCount();

	int getWarningCount();

	void consume(BuildMessage message);

	void report(Resource resource, BuildMessage[] message);

	void clearMessages(Resource resource);
}
