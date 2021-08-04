package org.zenframework.z8.justintime.runtime;

import java.util.List;
import java.util.Map;

import org.zenframework.z8.compiler.error.BuildMessage;

public interface ISource {

	void exportSources(Workspace workspace);

	void writeMessages(Map<String, List<BuildMessage>> messages);

}
