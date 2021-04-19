package org.zenframework.z8.compiler.workspace;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.zenframework.z8.compiler.cmd.CompilerException;
import org.zenframework.z8.compiler.file.File;
import org.zenframework.z8.compiler.file.FileException;

public class ProjectProperties {

	private static IPath CURRENT_DIRECTORY = new Path(new Path("").toFile().getAbsolutePath());
	static final private String Z8_PROJECT = "z8.project";

	private static final String PROJECT_NAME = "projectName";
	private static final String PROJECT_PATH = "projectPath";
	private static final String REQUIRED_PATHS = "requiredPaths";
	private static final String SOURCE_PATHS = "sourcePaths";
	private static final String OUTPUT_PATH = "outputPath";
	private static final String DOCS_PATH = "docsPath";
	private static final String DOC_TEMPLATE_PATH = "docTemplatePath";

	private static final IPath[] REQUIRED_PATHS_DEFAULT = new IPath[0];
	private static final IPath[] SOURCE_PATHS_DEFAULT = new IPath[] { new Path("") };

	protected IPath projectPath;
	protected String projectName;
	protected IPath[] requiredPaths;
	protected IPath[] sourcePaths;
	protected IPath outputPath;
	protected IPath docsPath;
	protected IPath docTemplatePath;

	public ProjectProperties() {}

	public ProjectProperties(java.io.File projectPath) {
		setProjectPath(projectPath);
	}

	public ProjectProperties(String projectPath) {
		setProjectPath(projectPath);
	}

	public ProjectProperties(IPath projectPath) {
		setProjectPath(projectPath);
	}

	public ProjectProperties(ProjectProperties properties) {
		apply(properties);
	}

	public String getProjectName() {
		return projectName != null ? projectName : getProjectPath().lastSegment();
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public IPath getProjectPath() {
		return absolutePath(projectPath, CURRENT_DIRECTORY);
	}

	public void setProjectPath(IPath projectPath) {
		this.projectPath = projectPath;
	}

	public void setProjectPath(String projectPath) {
		setProjectPath(getValidPath(projectPath));
	}

	public void setProjectPath(java.io.File projectPath) {
		setProjectPath(new Path(projectPath.getAbsolutePath()));
	}

	public IPath[] getRequiredPaths() {
		return absolutePath(requiredPaths != null ? requiredPaths : REQUIRED_PATHS_DEFAULT, getProjectPath());
	}

	public void setRequiredPaths(IPath[] requiredPaths) {
		this.requiredPaths = requiredPaths;
	}

	public void setRequiredPaths(String requiredPaths) {
		setRequiredPaths(getValidPath(parsePathList(requiredPaths)));
	}

	public void setRequiredPaths(List<String> requiredPaths) {
		if (requiredPaths == null)
			return;
		this.requiredPaths = new IPath[requiredPaths.size()];
		for (int i = 0; i < this.requiredPaths.length; i++)
			this.requiredPaths[i] = getValidPath(requiredPaths.get(i));
	}

	public void setRequiredPaths(String[] requiredPaths) {
		setRequiredPaths(Arrays.asList(requiredPaths));
	}

	public void setRequiredPaths(java.io.File[] requiredPaths) {
		setRequiredPaths(toPaths(requiredPaths));
	}

	public IPath[] getSourcePaths() {
		return relativePath(sourcePaths != null ? sourcePaths : SOURCE_PATHS_DEFAULT, getProjectPath());
	}

	public void setSourcePaths(IPath[] sourcePaths) {
		this.sourcePaths = sourcePaths;
	}

	public void setSourcePaths(String sourcePaths) {
		setSourcePaths(getValidPath(parsePathList(sourcePaths)));
	}

	public void setSourcePaths(String[] sourcePaths) {
		if (sourcePaths == null)
			return;
		this.sourcePaths = new IPath[sourcePaths.length];
		for (int i = 0; i < this.sourcePaths.length; i++)
			this.sourcePaths[i] = getValidPath(sourcePaths[i]);
	}

	public void setSourcePaths(List<String> sourcePaths) {
		if (sourcePaths == null)
			return;
		this.sourcePaths = new IPath[sourcePaths.size()];
		for (int i = 0; i < this.sourcePaths.length; i++)
			this.sourcePaths[i] = getValidPath(sourcePaths.get(i));
	}

	public void setSourcePaths(java.io.File[] sourcePaths) {
		setSourcePaths(toPaths(sourcePaths));
	}

	public IPath getOutputPath() {
		return absolutePath(outputPath, getProjectPath());
	}

	public void setOutputPath(IPath outputPath) {
		this.outputPath = outputPath;
	}

	public void setOutputPath(String outputPath) {
		setOutputPath(getValidPath(outputPath));
	}

	public void setOutputPath(java.io.File outputPath) {
		setOutputPath(new Path(outputPath.getAbsolutePath()));
	}

	public IPath getDocsPath() {
		return absolutePath(docsPath, getProjectPath());
	}

	public void setDocsPath(IPath docsPath) {
		this.docsPath = docsPath;
	}

	public void setDocsPath(String docsPath) {
		setDocsPath(getValidPath(docsPath));
	}

	public IPath getDocTemplatePath() {
		return absolutePath(docTemplatePath, getProjectPath());
	}

	public void setDocTemplatePath(IPath docTemplatePath) {
		this.docTemplatePath = docTemplatePath;
	}

	public void setDocTemplatePath(String docTemplatePath) {
		setDocTemplatePath(getValidPath(docTemplatePath));
	}

	public void apply(ProjectProperties properties) {
		if (properties.projectName != null)
			this.projectName = properties.projectName;
		if (properties.projectPath != null)
			this.projectPath = properties.projectPath;
		if (properties.requiredPaths != null)
			this.requiredPaths = properties.requiredPaths;
		if (properties.sourcePaths != null)
			this.sourcePaths = properties.sourcePaths;
		if (properties.outputPath != null)
			this.outputPath = properties.outputPath;
		if (properties.docsPath != null)
			this.docsPath = properties.docsPath;
		if (properties.docTemplatePath != null)
			this.docTemplatePath = properties.docTemplatePath;
	}

	public void load() throws FileException {
		if (projectPath == null)
			return;

		IPath z8ProjectPath = projectPath.append(Z8_PROJECT);
		File z8ProjectFile = File.fromPath(z8ProjectPath);

		if (!z8ProjectFile.exists())
			return;

		Properties properties = new Properties();
		InputStream in = z8ProjectFile.inputStream();
		try {
			try {
				properties.load(in);
			} finally {
				in.close();
			}
		} catch (IOException e) {
			throw new FileException(z8ProjectPath, e);
		}

		if (projectName == null && properties.containsKey(PROJECT_NAME))
			setProjectName(properties.getProperty(PROJECT_NAME));
		if (projectPath == null && properties.containsKey(PROJECT_PATH))
			setProjectPath(properties.getProperty(PROJECT_PATH));
		if (requiredPaths == null && properties.containsKey(REQUIRED_PATHS))
			setRequiredPaths(properties.getProperty(REQUIRED_PATHS));
		if (sourcePaths == null && properties.containsKey(SOURCE_PATHS))
			setSourcePaths(properties.getProperty(SOURCE_PATHS));
		if (outputPath == null && properties.containsKey(OUTPUT_PATH))
			setOutputPath(properties.getProperty(OUTPUT_PATH));
		if (docsPath == null && properties.containsKey(DOCS_PATH))
			setDocsPath(properties.getProperty(DOCS_PATH));
		if (docTemplatePath == null && properties.containsKey(DOC_TEMPLATE_PATH))
			setDocTemplatePath(properties.getProperty(DOC_TEMPLATE_PATH));
	}

	public void print(PrintStream out) {
		out.println("Project name:   " + getProjectName());
		out.println("Project path:   " + getProjectPath());
		IPath[] sourcePaths = getSourcePaths();
		if (sourcePaths.length > 1 || !sourcePaths[0].isEmpty())
			out.println("Source paths:   " + Arrays.toString(sourcePaths));
		out.println("Required paths: " + Arrays.toString(getRequiredPaths()));
		out.println("Output path:    " + getOutputPath());
		IPath docsPath = getDocsPath();
		IPath docTemplatePath = getDocTemplatePath();
		if (docsPath != null)
			out.println("Docs path:      " + docsPath);
		if (docTemplatePath != null)
			out.println("Docs template:  " + docTemplatePath);
	}

	static private boolean isValidPath(String path) {
		IPath test = new Path(path);
		return test.isValidPath(path);
	}

	static private String[] parsePathList(String pathList) {
		pathList = stripQuotes(pathList);

		StringTokenizer tokenizer = new StringTokenizer(pathList, ",;");

		List<String> result = new ArrayList<String>();

		while(tokenizer.hasMoreTokens())
			result.add(tokenizer.nextToken());

		return result.toArray(new String[result.size()]);
	}

	static private IPath relativePath(IPath path, IPath absolutePrefix) {
		return path == null || !path.isAbsolute() ? path : absolutePrefix.isPrefixOf(path)
				? path.removeFirstSegments(absolutePrefix.segmentCount()).setDevice(null) : null;
	}

	static private IPath[] relativePath(IPath[] paths, IPath absolutePrefix) {
		for (int i = 0; i < paths.length; i++)
			paths[i] = relativePath(paths[i], absolutePrefix);
		return paths;
	}

	static private IPath absolutePath(IPath path, IPath absolutePrefix) {
		return path == null || path.isAbsolute() ? path : absolutePrefix.append(path);
	}

	static private IPath[] absolutePath(IPath[] paths, IPath absolutePrefix) {
		for (int i = 0; i < paths.length; i++)
			paths[i] = absolutePath(paths[i], absolutePrefix);
		return paths;
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
		if (pathString == null)
			return null;

		pathString = stripQuotes(pathString);

		if(!isValidPath(pathString))
			throw new CompilerException("Invalid path " + '"' + pathString + '"');

		return new Path(pathString);
	}

	static private String stripQuotes(String path) {
		String result = path;

		if(result.startsWith("\""))
			result = result.substring(1);

		if(result.endsWith("\""))
			result = result.substring(0, result.length() - 1);

		return result;
	}

	static private IPath[] toPaths(java.io.File[] files) {
		if (files == null)
			return null;
		IPath[] paths = new IPath[files.length];
		for (int i = 0; i < paths.length; i++)
			paths[i] = new Path(files[i].getAbsolutePath());
		return paths;
	}

}
