package org.zenframework.z8.compiler.cmd;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.zenframework.z8.compiler.error.DefaultBuildMessageConsumer;
import org.zenframework.z8.compiler.file.File;
import org.zenframework.z8.compiler.file.FileException;
import org.zenframework.z8.compiler.workspace.CompilationUnit;
import org.zenframework.z8.compiler.workspace.Project;
import org.zenframework.z8.compiler.workspace.ProjectProperties;
import org.zenframework.z8.compiler.workspace.StartupCodeGenerator;
import org.zenframework.z8.compiler.workspace.Workspace;

public class Main {

	public static final String SOURCES_KEY = "-sources:";
	public static final String OUTPUT_KEY = "-output:";
	public static final String DOCS_KEY = "-docs:";
	public static final String DOC_TEMPLATE_KEY = "-doctemplate:";
	public static final String REQUIRED_KEY = "-requires:";
	public static final String PROJECT_NAME_KEY = "-projectname:";

	static private void outputHello() {
		System.out.println("ZENFRAMEWORK 2014, Z8 Compiler 1.3.0.210119");
	}

	static private void outputUsage() {
		outputHello();
		System.out.println("java -classpath <classpath> <project path> " +
				"[-projectName:<Project name>] " +
				"[-sources:<comma or semicolon separated relative source paths>] " +
				"[-requires:<comma or semicolon separated required projects>] " +
				"[-output:<output path relative to project path>] " + 
				"[-docs:<docs output path (relative to project path)>] " + 
				"[-docTemplate:<doc template file path (relative to project path)>]");
	}

	static private void outputUsageAndExit() {
		outputUsage();
		outputExitCode(-2);
	}

	static private void outputExitCode(int exitCode) {
		System.out.println("Tool execution exited with code " + exitCode);
		System.exit(exitCode);
	}

	static private void outputErrorAndExit(String error) {
		System.out.println(error);
		outputExitCode(-3);
	}

	static public Project initializeProject(ProjectProperties properties) throws CoreException {
		Workspace workspace = Workspace.initialize(new DummyContainer());

		Project project = workspace.createProject(new DummyProject(properties.getProjectName(), (DummyContainer) workspace.getResource(), properties.getProjectPath()), properties);
		project.initialize();
		for(IPath requiredPath : properties.getRequiredPaths())
			workspace.createProject(new DummyContainer((DummyContainer) workspace.getResource(), requiredPath, true)).initialize();

		Project[] projects = workspace.getProjects();
		List<IResource> resources = new ArrayList<IResource>();

		for(Project p : projects) {
			if(p != project)
				resources.add(p.getResource());
		}

		for(Project p : projects)
			p.setReferencedProjects(resources.toArray(new IResource[0]));

		return project;
	}

	static protected void generateSourceList(Project project, IPath outputPath) throws FileException {
		final StringBuffer sources = new StringBuffer();

		CompilationUnit[] compilationUnits = project.getCompilationUnits();

		for(CompilationUnit compilationUnit : compilationUnits) {
			if(!compilationUnit.containsNativeType())
				sources.append("\"" + compilationUnit.getOutputPath().toString() + "\"" + "\n");
		}

		sources.append("\"" + project.getOutputPath().append(StartupCodeGenerator.getRuntimeJavaPath(project)) + "\"\n");
		sources.append("\"" + project.getOutputPath().append(StartupCodeGenerator.Z8BlRuntimePath) + "\"");

		outputPath = outputPath.append("javafiles.lst");
		File.fromPath(outputPath).write(sources.toString());
	}

	static protected void generateDocs(Project[] projects, IPath docsPath, IPath docTemplatePath) throws FileException {
		if(docsPath != null)
			new DocsGenerator(projects, docsPath, docTemplatePath).run();
	}

	static private boolean validateDirectory(IPath path) throws FileException {
		File file = File.fromPath(path);
		if(!file.exists() || !file.isContainer())
			return false;
		return true;
	}

	static public void main(String[] arguments) {
		try {
			ProjectProperties properties = new ProjectProperties();

			for(String argument : arguments) {
				if (argument.toLowerCase().startsWith(PROJECT_NAME_KEY))
					properties.setProjectName(argument.substring(PROJECT_NAME_KEY.length()));
				else if (argument.toLowerCase().startsWith(SOURCES_KEY))
					properties.setSourcePaths(argument.substring(SOURCES_KEY.length()));
				else if (argument.toLowerCase().startsWith(OUTPUT_KEY))
					properties.setOutputPath(argument.substring(OUTPUT_KEY.length()));
				else if (argument.toLowerCase().startsWith(DOCS_KEY))
					properties.setDocsPath(argument.substring(DOCS_KEY.length()));
				else if (argument.toLowerCase().startsWith(DOC_TEMPLATE_KEY))
					properties.setDocTemplatePath(argument.substring(DOC_TEMPLATE_KEY.length()));
				else if (argument.toLowerCase().startsWith(REQUIRED_KEY))
					properties.setRequiredPaths(argument.substring(REQUIRED_KEY.length()));
				else if (properties.getProjectPath() == null)
					properties.setProjectPath(argument);
				else
					outputUsageAndExit();
			}

			if(properties.getProjectPath() == null) {
				outputUsage();
				outputErrorAndExit("Missing <project path> argument");
			}

			if(properties.getOutputPath() == null) {
				outputUsage();
				outputErrorAndExit("Missing <output path> argument");
			}

			if(properties.getDocsPath() != null && properties.getDocTemplatePath() == null) {
				outputUsage();
				outputErrorAndExit("Missing <<doc template file path> argument");
			}

			if(properties.getDocsPath() == null && properties.getDocTemplatePath() != null) {
				outputUsage();
				outputErrorAndExit("Missing <<docs output path> argument");
			}

			if (!validateDirectory(properties.getProjectPath()))
				throw new CompilerException("Project directory " + '"' + properties.getProjectPath() + '"' + " does not exist");

			compile(properties);
		} catch (Throwable e) {
			outputHello();
			outputErrorAndExit("Error: " + e.getMessage());
		}
	}

	static public void compile(ProjectProperties properties) throws FileException, CoreException {
		outputHello();

		properties.load();
		properties.print(System.out);

		for (int i = 0; i < properties.getRequiredPaths().length; i++) {
			IPath requiredPath = properties.getRequiredPaths()[i];
			if (!validateDirectory(requiredPath))
				System.out.println("Warning: required path " + requiredPath + " does not exist");
		}

		Project project = initializeProject(properties);
		Project[] projects = project.getWorkspace().getProjects();

		DefaultBuildMessageConsumer consumer = new DefaultBuildMessageConsumer();

		project.build(consumer);

		generateSourceList(project, properties.getOutputPath());
		generateDocs(projects, properties.getDocsPath(), properties.getDocTemplatePath());

		if (consumer.getErrorCount() != 0)
			throw new CompilerException("COMPILATION FAILED");
	}

}
