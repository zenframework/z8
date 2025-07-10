package org.zenframework.z8.justintime.runtime;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.runtime.CoreException;
import org.zenframework.z8.compiler.cmd.Main;
import org.zenframework.z8.compiler.workspace.Project;
import org.zenframework.z8.compiler.workspace.ProjectProperties;
import org.zenframework.z8.server.base.file.Folders;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.request.IMonitor;
import org.zenframework.z8.server.utils.IOUtils;

public class Workspace {

	private static final File DependenciesPath = new File(Folders.ApplicationPath, "just-in-time/dependencies");
	private static final File WorkspacesPath = new File(Folders.WorkingPath, "just-in-time/workspaces");

	private static final String BlSources = "bl";
	private static final String JavaSources = "java";
	private static final String JavaClasses = "classes";
	private static final String Backup = "backup";

	private static final Map<String, Workspace> Workspaces = new HashMap<String, Workspace>();

	public static Workspace workspace(String schema) {
		synchronized (Workspaces) {
			Workspace workspace = Workspaces.get(schema);
			if (workspace == null)
				Workspaces.put(schema, (workspace = new Workspace(schema)));
			return workspace;
		}
	}

	public static Workspace[] workspaces() {
		String[] schemas = WorkspacesPath.list();
		if (schemas == null)
			return new Workspace[0];
		Workspace[] workspaces = new Workspace[schemas.length];
		for (int i = 0; i < schemas.length; i++)
			workspaces[i] = workspace(schemas[i]);
		return workspaces;
	}

	public static String getSchema(URL resource) {
		try {
			Path path = Paths.get(resource.toURI());
			Path schemas = WorkspacesPath.toPath();
			return path.startsWith(schemas) ? path.getName(schemas.getNameCount()).toString() : null;
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	private final String schema;
	private final File workspace;
	private final File blSources;
	private final File javaSources;
	private final File javaClasses;
	private final File backup;

	private Workspace(String schema) {
		this.schema = schema;
		this.workspace = new File(WorkspacesPath, schema);
		this.blSources = new File(workspace, BlSources);
		this.javaSources = new File(workspace, JavaSources);
		this.javaClasses = new File(workspace, JavaClasses);
		this.backup = new File(workspace, Backup);
	}

	public void recompile(ISource source) {
		JustInTimeListener listener = new JustInTimeListener(this);

		try {
			backup();
			cleanWorkspace();
			exportSources(source);
			compileBl(listener);
			compileJava(listener);
			copyResources();
			logMessage("JUST-IN-TIME COMPILATION SUCCESSFUL");
		} catch (Throwable e) {
			logError("JUST-IN-TIME COMPILATION FAILED. See error messages", e);
			try {
				restore();
			} catch (Throwable e1) {
				logError("JUST-IN-TIME RESTORE FAILED", e1);
			}
		}

		listener.writeMessages(source);
	}

	public String getSchema() {
		return schema;
	}

	public File getWorkspace() {
		return workspace;
	}

	public File getBlSources() {
		return blSources;
	}

	public File getJavaSources() {
		return javaSources;
	}

	public File getJavaClasses() {
		return javaClasses;
	}

	private void backup() throws IOException {
		clean(backup);
		copy(javaClasses, backup);
	}

	private void restore() throws IOException {
		clean(javaClasses);
		copy(backup, javaClasses);
	}

	private void cleanWorkspace() throws IOException {
		clean(blSources);
		clean(javaSources);
		clean(javaClasses);
	}

	private void exportSources(ISource source) throws IOException {
		source.exportSources(this);
	}

	private void compileBl(JustInTimeListener listener) throws JustInTimeException {
		ProjectProperties properties = new ProjectProperties(this.workspace);
		properties.setProjectName(getProjectName());
		properties.setSourcePaths(BlSources);
		properties.setOutputPath(JavaSources);
		properties.setRequiredPaths(DependenciesPath.listFiles());

		try {
			Project project = Main.initializeProject(properties);
			project.build(listener.getBuildMessageConsumer());
		} catch (CoreException e) {
			throw new JustInTimeException(e);
		}
		if (listener.getErrorCount() != 0)
			throw new JustInTimeException("BL compilation error");
	}

	private void compileJava(JustInTimeListener listener) throws JustInTimeException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

		if (compiler == null)
			throw new RuntimeException("Java compiler not found");

		StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
		List<String> options = Arrays.asList("-d", javaClasses.getAbsolutePath());
		StringWriter out = new StringWriter();
		JavaCompiler.CompilationTask task = compiler.getTask(out, fileManager, listener.getDiagnosticListener(), options, null,
				fileManager.getJavaFileObjectsFromFiles(getJavaFiles(javaSources, new LinkedList<File>())));
		try {
			if (!task.call())
				throw new JustInTimeException("Java compilation error:\n" +  out.toString());
		} finally {
			try {
				fileManager.close();
			} catch (IOException e) {}
		}
	}

	private void copyResources() throws IOException {
		copy(javaSources, javaClasses, new FileFilter() {
			@Override
			public boolean accept(File file) {
				return !file.getName().endsWith('.' + JavaSources);
			}
		});
	}

	private static void clean(File folder) throws IOException {
		if (folder.exists())
			FileUtils.cleanDirectory(folder);
		if (!folder.exists() && !folder.mkdirs())
			throw new IOException("Couldn't create folder '" + folder.getAbsolutePath() + "'");
	}

	private static void copy(File from, File to) throws IOException {
		copy(from, to, null);
	}

	private static void copy(File from, File to, FileFilter filter) throws IOException {
		if (!from.exists())
			return;
		if (from.isDirectory()) {
			for (File file : from.listFiles())
				copy(file, new File(to, file.getName()), filter);
		} else if (filter == null || filter.accept(from)) {
			File folder = to.getParentFile();
			if (!folder.exists() && !folder.mkdirs())
				throw new IOException("Couldn't create folder '" + to.getParentFile().getAbsolutePath() + "'");
			IOUtils.copy(new FileInputStream(from), new FileOutputStream(to));
		}
	}

	private static String getProjectName() {
		return ApplicationServer.getSchema() + "-jst";
	}

	private static List<File> getJavaFiles(File folder, List<File> files) {
		for (File file : folder.listFiles()) {
			if (file.isDirectory())
				getJavaFiles(file, files);
			else if (FilenameUtils.isExtension(file.getName(), JavaSources))
				files.add(file);
		}
		return files;
	}

	private static void logMessage(String message) {
		IMonitor monitor = ApplicationServer.getMonitor();
		if (monitor != null)
			monitor.info(message);
		else
			Trace.logEvent(message);
	}

	private static void logError(String message, Throwable e) {
		IMonitor monitor = ApplicationServer.getMonitor();
		if (monitor != null) {
			monitor.error(message);
			monitor.error(e);
		} else {
			Trace.logError(message, e);
		}
	}
}
