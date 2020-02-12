package org.zenframework.z8.pde;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.zenframework.z8.pde.build.Z8ProjectBuilder;

public class BuildPathManager {
	public final static String JAVA_OUTPUT_DEFAULT_FOLDER = "./.java";
	//public final static String JAVA_CLASS_DEFAULT_FOLDER = "classes";
	//public final static String DOCS_DEFAULT_FOLDER = "docs";

	public final static String JAVA_OUTPUT_PATH_KEY = "JavaSource";
	//public final static String CLASS_OUTPUT_PATH_KEY = "JavaClasses";
	//public final static String DOCS_OUTPUT_PATH_KEY = "Docs";

	static public IPath getJavaOutputPath(IProject project) {
		return getPath(project, JAVA_OUTPUT_PATH_KEY, JAVA_OUTPUT_DEFAULT_FOLDER);
	}

	/*static public IPath getClassOutputPath(IProject project) {
		return getPath(project, CLASS_OUTPUT_PATH_KEY, JAVA_CLASS_DEFAULT_FOLDER);
	}*/

	/*static public IPath getDocsOutputPath(IProject project) {
		return getPath(project, DOCS_OUTPUT_PATH_KEY, DOCS_DEFAULT_FOLDER);
	}*/

	/*
	 * static public IPath getWebInfPath(IProject project) { return
	 * getPath(project, WEBINF_PATH_KEY, null); }
	 */

	static protected IPath getPath(IProject project, String key, String defaultValue) {
		IPath projectPath = project.getLocation();

		try {
			IProjectDescription desc = project.getDescription();
			List<ICommand> commands = Arrays.asList(desc.getBuildSpec());
			String value = null;

			for (ICommand command : commands) {
				if (Z8ProjectBuilder.Id.equals(command.getBuilderName())) {
					Map<String, String> arguments = command.getArguments();
					value = arguments != null ? arguments.get(key) : null;
					break;
				}
			}

			if (value == null)
				value = defaultValue;

			if (value != null) {
				IPath outputPath = new Path(value);
				return outputPath.isAbsolute() ? outputPath : projectPath.append(outputPath);
			}
		} catch(CoreException e) {
			Plugin.log(e);
		}

		return defaultValue != null ? projectPath.append(defaultValue) : projectPath;
	}
}
