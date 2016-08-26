package org.zenframework.z8.compiler.cmd;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.zenframework.z8.compiler.error.DefaultBuildMessageConsumer;
import org.zenframework.z8.compiler.file.File;
import org.zenframework.z8.compiler.file.FileException;
import org.zenframework.z8.compiler.workspace.CompilationUnit;
import org.zenframework.z8.compiler.workspace.Folder;
import org.zenframework.z8.compiler.workspace.Project;
import org.zenframework.z8.compiler.workspace.StartupCodeGenerator;
import org.zenframework.z8.compiler.workspace.Workspace;

public class Main {

	public static final String OUTPUT_KEY = "-output:";
	public static final String REQUIRED_KEY = "-requires:";
	public static final String PROJECT_NAME_KEY = "-projectName:";
	private static IPath CURRENT_DIRECTORY = new Path(new Path("/").toFile().getAbsolutePath());

	static private void outputHello() {
		System.out.println("ZENFRAMEWORK 2014, Z8 Compiler 1.0.0");
	}

	static private void outputUsage() {
		System.out.println("java -classpath <classpath> <project path> " +
				"-projectName:<Project name> " +
				"[-requires:<comma or semicolon separated required projects>] " +
				"-output:<output path relative to project path>");
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

	static private String stripQuotes(String path) {
		String result = path;

		if(result.startsWith("\""))
			result = result.substring(1);

		if(result.endsWith("\""))
			result = result.substring(0, result.length() - 1);

		return result;
	}

	static boolean isValidPath(String path) {
		IPath test = new Path(path);
		return test.isValidPath(path);
	}

	static private IPath makeAbsolutePath(String pathValue) {
		IPath path = new Path(pathValue);

		if(!path.isAbsolute())
			return CURRENT_DIRECTORY.append(path);

		return path;
	}

	static private IPath[] getValidPath(String[] pathString) {
		if(pathString == null)
			return new IPath[0];

		List<IPath> result = new ArrayList<IPath>();

		for(String path : pathString)
			result.add(getValidPath(path));

		return result.toArray(new IPath[result.size()]);
	}

	static private IPath getValidPath(String pathString) {
		pathString = stripQuotes(pathString);

		if(!isValidPath(pathString))
			outputErrorAndExit("Invalid path " + '"' + pathString + '"');

		return makeAbsolutePath(pathString);
	}

	static private String[] parsePathList(String pathList) {
		pathList = stripQuotes(pathList);

		StringTokenizer tokenizer = new StringTokenizer(pathList, ",;");

		List<String> result = new ArrayList<String>();

		while(tokenizer.hasMoreTokens())
			result.add(tokenizer.nextToken());

		return result.toArray(new String[result.size()]);
	}

	static private boolean initializeWorkspaceAndBuild(String projectName, IPath projectPath, IPath[] requiredPaths, IPath outputPath) {
		Workspace workspace = Workspace.initialize(new DummyResource());

		Project project = workspace.createProject(new DummyProject(projectName, workspace.getResource(), projectPath));
		project.setOutputPath(outputPath);

		addResources(project);

		for(IPath requiredPath : requiredPaths) {
			Project p = workspace.createProject(new DummyResource(workspace.getResource(), requiredPath, true));
			addResources(p);
		}

		Project[] projects = workspace.getProjects();
		List<IResource> resources = new ArrayList<IResource>();

		for(Project p : projects) {
			if(p != project)
				resources.add(p.getResource());
		}

		for(Project p : projects)
			p.setReferencedProjects(resources.toArray(new IResource[0]));

		DefaultBuildMessageConsumer consumer = new DefaultBuildMessageConsumer();

		project.build(consumer);

		generateSourceList(project, outputPath);

		return consumer.getErrorCount() == 0;
	}

	static protected void generateSourceList(Project project, IPath outputPath) {
		final StringBuffer sources = new StringBuffer();

		CompilationUnit[] compilationUnits = project.getCompilationUnits();

		System.out.println("compilationUnits: " + compilationUnits.length);
		for(CompilationUnit compilationUnit : compilationUnits) {
			if(!compilationUnit.containsNativeType())
				sources.append("\"" + compilationUnit.getOutputPath().toString() + "\"" + "\n");
		}

		sources.append("\"" + project.getOutputPath().append(StartupCodeGenerator.getRuntimeJavaPath(project)) + "\"\n");
		sources.append("\"" + project.getOutputPath().append(StartupCodeGenerator.Z8BlRuntimePath) + "\"");

		try {
			outputPath = outputPath.append("javafiles.lst");
			File.fromPath(outputPath).write(sources.toString());
		} catch(FileException e) {
			outputErrorAndExit(e.getMessage());
		}
	}

	static protected boolean isCompilationUnit(IPath path) {
		return "bl".equals(path.getFileExtension());
	}

	static protected boolean isNLSUnit(IPath path) {
		return "nls".equals(path.getFileExtension());
	}

	static protected void addResources(Folder folder) {
		try {
			IPath folderPath = folder.getAbsolutePath();
			IPath[] members = File.fromPath(folderPath).getFiles();

			for(IPath member : members) {
				File file = File.fromPath(member);

				if(file.isDirectory()) {
					Folder newFolder = folder.createFolder(new DummyResource(folder.getResource(), member));
					addResources(newFolder);
				} else {
					if(isCompilationUnit(member))
						folder.createCompilationUnit(new DummyResource(folder.getResource(), member));
					if(isNLSUnit(member))
						folder.createNLSUnit(new DummyResource(folder.getResource(), member));
				}
			}
		} catch(FileException e) {
			outputErrorAndExit(e.getMessage());
		}

	}

	static private void validateDirectory(IPath path, String pathValue) throws CompilerException, FileException {
		File file = File.fromPath(path);
		if(!file.exists() || !file.isDirectory())
			throw new CompilerException("Directory " + '"' + pathValue + '"' + " does not exist");
	}

	static public void main(String[] arguments) {
		outputHello();

		String projectName = null;
		String projectPathValue = null;
		String outputPathValue = null;

		String[] requiredPathValues = null;

		for(String argument : arguments) {
			if(argument.toLowerCase().startsWith(PROJECT_NAME_KEY)) {
				if(projectName != null)
					outputUsageAndExit();

				projectName = argument.substring(PROJECT_NAME_KEY.length());
			} else if(argument.toLowerCase().startsWith(OUTPUT_KEY)) {
				if(outputPathValue != null)
					outputUsageAndExit();

				outputPathValue = argument.substring(OUTPUT_KEY.length());
			} else if(argument.toLowerCase().startsWith(REQUIRED_KEY)) {
				if(requiredPathValues != null)
					outputUsageAndExit();

				requiredPathValues = parsePathList(argument.substring(REQUIRED_KEY.length()));
			} else if(projectPathValue == null)
				projectPathValue = argument;
			else
				outputUsageAndExit();
		}

		if(projectName == null) {
			outputUsage();
			outputErrorAndExit("Missing <project name> argument");
		}

		if(projectPathValue == null) {
			outputUsage();
			outputErrorAndExit("Missing <project path> argument");
		}

		if(outputPathValue == null) {
			outputUsage();
			outputErrorAndExit("Missing <output path> argument");
		}

		try {
			compile(projectName, projectPathValue, outputPathValue, requiredPathValues);
		} catch(CompilerException e) {
			outputErrorAndExit(e.getMessage());
		}
	}

	public static void compile(String projectName, String projectPathValue, String outputPathValue, String[] requiredPathValues) throws CompilerException {

		IPath projectPath = getValidPath(projectPathValue);
		CURRENT_DIRECTORY = projectPath;
		IPath outputPath = getValidPath(outputPathValue);
		IPath[] requiredPaths = getValidPath(requiredPathValues);

		if(requiredPathValues == null)
			requiredPathValues = new String[0];

		try {
			validateDirectory(projectPath, projectPathValue);
			for(int i = 0; i < requiredPaths.length; i++)
				validateDirectory(requiredPaths[i], requiredPathValues[i]);
		} catch(FileException e) {
			throw new CompilerException(e.getMessage());
		}

		if(!initializeWorkspaceAndBuild(projectName, projectPath, requiredPaths, outputPath)) {
			throw new CompilerException("COMPILATION FAILED");
		}
	}
}
