package org.zenframework.z8.justintime.runtime;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;

import org.zenframework.z8.compiler.error.BuildError;
import org.zenframework.z8.compiler.error.BuildMessage;
import org.zenframework.z8.compiler.error.BuildWarning;
import org.zenframework.z8.compiler.error.IBuildMessageConsumer;
import org.zenframework.z8.compiler.workspace.Resource;

public class JustInTimeListener implements IBuildMessageConsumer, DiagnosticListener<JavaFileObject> {

	private final Map<String, List<BuildMessage>> blMessages = new HashMap<String, List<BuildMessage>>();
	private final List<Diagnostic<? extends JavaFileObject>> javaDiagnostics = new LinkedList<Diagnostic<? extends JavaFileObject>>();
	private int blErrors = 0, blWarnings = 0;

	@Override
	public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
		javaDiagnostics.add(diagnostic);
	}

	@Override
	public int getErrorCount() {
		return blErrors;
	}

	@Override
	public int getWarningCount() {
		return blWarnings;
	}

	@Override
	public void consume(BuildMessage message) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void report(Resource resource, BuildMessage[] messages) {
		String path = resource.getSourceRelativePath().toString();
		List<BuildMessage> resourceMessages = blMessages.get(path);
		if (resourceMessages == null)
			blMessages.put(path, (resourceMessages = new LinkedList<BuildMessage>()));
		for (BuildMessage message : messages) {
			resourceMessages.add(message);
			if (message instanceof BuildError)
				blErrors++;
			else if (message instanceof BuildWarning)
				blWarnings++;
		}
	}

	@Override
	public void clearMessages(Resource resource) {
		String path = resource.getSourceRelativePath().toString();
		List<BuildMessage> resourceMessages = blMessages.get(path);
		if (resourceMessages != null)
			resourceMessages.clear();
	}

	public void writeMessages(ISource source) {
		source.writeMessages(blMessages);
	}

	public void clear() {
		blMessages.clear();
		javaDiagnostics.clear();
		blErrors = 0;
		blWarnings = 0;
	}

}
