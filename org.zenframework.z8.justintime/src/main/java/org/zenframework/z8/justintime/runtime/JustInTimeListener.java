package org.zenframework.z8.justintime.runtime;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;

import org.zenframework.z8.compiler.error.BuildError;
import org.zenframework.z8.compiler.error.BuildMessage;
import org.zenframework.z8.compiler.error.BuildWarning;
import org.zenframework.z8.compiler.error.IBuildMessageConsumer;
import org.zenframework.z8.compiler.parser.grammar.lexer.Position;
import org.zenframework.z8.compiler.workspace.Resource;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.request.IMonitor;

public class JustInTimeListener {

	private final Map<String, List<BuildMessage>> messages = new HashMap<String, List<BuildMessage>>();
	private String javaSourcesPath;
	private int errors = 0, warnings = 0;

	private class BlBuildMessageConsumer implements IBuildMessageConsumer {

		@Override
		public int getErrorCount() {
			return errors;
		}

		@Override
		public int getWarningCount() {
			return warnings;
		}

		@Override
		public void consume(BuildMessage message) {
			JustInTimeListener.this.report(message);
		}

		@Override
		public void report(Resource resource, BuildMessage[] messages) {
			JustInTimeListener.this.report(resource.getSourceRelativePath().toString(), messages);
		}

		@Override
		public void clearMessages(Resource resource) {
			JustInTimeListener.this.clearMessages(resource.getSourceRelativePath().toString());
		}

	}

	private class JavaDiagnosticListener implements DiagnosticListener<JavaFileObject> {

		@Override
		public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
			JavaFileObject source = diagnostic.getSource();
			String name = source != null ? source.getName() : null;

			if (name == null || !name.startsWith(javaSourcesPath)) {
				logError(diagnostic.toString());
				return;
			}

			JustInTimeListener.this.report(name.substring(javaSourcesPath.length() + 1), new BuildMessage[] { toBuildMessage(diagnostic) });
		}

	}

	public JustInTimeListener(Workspace workspace) {
		this.javaSourcesPath = workspace.getJavaSources().getAbsolutePath();
	}

	public IBuildMessageConsumer getBuildMessageConsumer() {
		return new BlBuildMessageConsumer();
	}

	public DiagnosticListener<JavaFileObject> getDiagnosticListener() {
		return new JavaDiagnosticListener();
	}

	public int getErrorCount() {
		return errors;
	}

	protected void report(BuildMessage message) {
		logError(message.format());
	}

	protected void report(String path, BuildMessage[] messages) {
		List<BuildMessage> resourceMessages = this.messages.get(path);
		if (resourceMessages == null)
			this.messages.put(path, (resourceMessages = new LinkedList<BuildMessage>()));
		for (BuildMessage message : messages) {
			resourceMessages.add(message);
			if (message instanceof BuildError)
				errors++;
			else if (message instanceof BuildWarning)
				warnings++;
		}
	}

	protected void clearMessages(String path) {
		List<BuildMessage> resourceMessages = messages.get(path);
		if (resourceMessages != null)
			resourceMessages.clear();
	}

	public void writeMessages(ISource source) {
		source.writeMessages(messages);
	}

	public void clear() {
		messages.clear();
		errors = 0;
		warnings = 0;
	}

	private static void logError(String message) {
		IMonitor monitor = ApplicationServer.getMonitor();
		if (monitor != null)
			monitor.error(message);
		else
			Trace.logEvent("Just-in-time error: " + message);
	}

	private static BuildMessage toBuildMessage(Diagnostic<?> diagnostic) {
		Position position = new Position();
		position.setColumn((int) diagnostic.getColumnNumber());
		position.setLine((int) diagnostic.getLineNumber());
		position.setLength((int) (diagnostic.getEndPosition() - diagnostic.getStartPosition()));
		position.setOffset((int) (diagnostic.getPosition() - diagnostic.getStartPosition()));

		if (diagnostic.getKind() == Kind.ERROR)
			return new BuildError(null, position, diagnostic.getMessage(Locale.getDefault()), null);
		return new BuildWarning(null, position, diagnostic.getMessage(Locale.getDefault()));
	}

}
