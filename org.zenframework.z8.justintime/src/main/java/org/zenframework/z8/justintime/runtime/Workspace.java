package org.zenframework.z8.justintime.runtime;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
import javax.tools.JavaFileManager;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.zenframework.z8.compiler.cmd.Main;
import org.zenframework.z8.compiler.workspace.Project;
import org.zenframework.z8.compiler.workspace.ProjectProperties;
import org.zenframework.z8.justintime.compiler.CustomClassloaderJavaFileManager;
import org.zenframework.z8.server.base.file.Folders;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.utils.IOUtils;

public class Workspace {

	private static final File JUST_IN_TIME = new File(Folders.Base, "just-in-time");
	private static final File SCHEMAS = new File(JUST_IN_TIME, "schemas");
	private static final File[] DEPENDENCIES = new File(JUST_IN_TIME, "dependencies").listFiles();

	private static final String BL_SOURCES = "bl";
	private static final String JAVA_SOURCES = "java";
	private static final String JAVA_CLASSES = "classes";

	private static final Map<String, Workspace> WORKSPACES = new HashMap<String, Workspace>();

	public static Workspace workspace(String schema) {
		synchronized (WORKSPACES) {
			Workspace workspace = WORKSPACES.get(schema);
			if (workspace == null)
				WORKSPACES.put(schema, (workspace = new Workspace(schema)));
			return workspace;
		}
	}

	public static Workspace[] workspaces() {
		String[] schemas = SCHEMAS.list();
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
			Path schemas = SCHEMAS.toPath();
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

	private Workspace(String schema) {
		this.schema = schema;
		this.workspace = new File(SCHEMAS, schema);
		this.blSources = new File(workspace, BL_SOURCES);
		this.javaSources = new File(workspace, JAVA_SOURCES);
		this.javaClasses = new File(workspace, JAVA_CLASSES);
	}

	public boolean recompile(ISource source, JustInTimeListener listener) {
		cleanWorkspace();

		source.exportSources(this);

		return compileBl(listener) && compileJava(listener) && copyResources();
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

	private void cleanWorkspace() {
		try {
			if (workspace.exists())
				FileUtils.cleanDirectory(workspace);
			else
				workspace.mkdirs();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		javaSources.mkdirs();
		javaClasses.mkdirs();
	}

	private boolean compileBl(JustInTimeListener listener) {
		ProjectProperties properties = new ProjectProperties(this.workspace);
		properties.setProjectName(getProjectName());
		properties.setSourcePaths(BL_SOURCES);
		properties.setOutputPath(JAVA_SOURCES);
		properties.setRequiredPaths(DEPENDENCIES);

		try {
			Project project = Main.initializeProject(properties);
			project.build(listener);
			return listener.getErrorCount() == 0;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private boolean compileJava(JustInTimeListener listener) {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

		if (compiler == null)
			throw new RuntimeException("Java compiler not found");

		StandardJavaFileManager standardFileManager = compiler.getStandardFileManager(null, null, null);
		JavaFileManager customFileManager = new CustomClassloaderJavaFileManager(getClass().getClassLoader(), standardFileManager);
		List<String> options = Arrays.asList("-d", javaClasses.getAbsolutePath());
		JavaCompiler.CompilationTask task = compiler.getTask(null, customFileManager, listener, options, null,
				standardFileManager.getJavaFileObjectsFromFiles(getJavaFiles(javaSources, new LinkedList<File>())));
		try {
			return task.call();
		} finally {
			try {
				standardFileManager.close();
			} catch (IOException e) {}
		}
	}

	private boolean copyResources() {
		copyResources(javaSources, javaClasses);
		return true;
	}

	private static void copyResources(File from, File to) {
		if (from.isDirectory()) {
			for (File file : from.listFiles())
				copyResources(file, new File(to, file.getName()));
		} else if (!from.getName().endsWith('.' + JAVA_SOURCES)) {
			to.getParentFile().mkdirs();
			try {
				IOUtils.copy(new FileInputStream(from), new FileOutputStream(to));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private static String getProjectName() {
		return ServerConfig.database().schema() + "-jst";
	}

	private static List<File> getJavaFiles(File folder, List<File> files) {
		for (File file : folder.listFiles()) {
			if (file.isDirectory())
				getJavaFiles(file, files);
			else if (FilenameUtils.isExtension(file.getName(), JAVA_SOURCES))
				files.add(file);
		}
		return files;
	}

}
