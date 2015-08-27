package org.zenframework.z8.pde;

import java.util.Map;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.zenframework.z8.pde.build.Z8ProjectBuilder;

public class BuildPathManager {
    final static String JAVA_OUTPUT_DEFAULT_FOLDER = "src";
    final static String JAVA_CLASS_DEFAULT_FOLDER = "classes";

    final static String JAVA_OUTPUT_PATH_KEY = "JavaSource";
    final static String CLASS_OUTPUT_PATH_KEY = "JavaClasses";

    static public IPath getJavaOutputPath(IProject project) {
        return getPath(project, JAVA_OUTPUT_PATH_KEY, JAVA_OUTPUT_DEFAULT_FOLDER);
    }

    static public IPath getClassOutputPath(IProject project) {
        return getPath(project, CLASS_OUTPUT_PATH_KEY, JAVA_CLASS_DEFAULT_FOLDER);
    }

    /*static public IPath getWebInfPath(IProject project)
    {
    	return getPath(project, WEBINF_PATH_KEY, null);
    }*/

    static protected IPath getPath(IProject project, String key, String defaultFolder) {
        IPath projectPath = project.getLocation();

        try {
            IPath outputPath = null;

            ICommand[] commands = project.getDescription().getBuildSpec();

            for(ICommand command : commands) {
                if(Z8ProjectBuilder.Id.equals(command.getBuilderName())) {
                    Map<String, String> arguments = command.getArguments();

                    if(arguments != null) {
                        String value = arguments.get(key);

                        if(value != null) {
                            outputPath = new Path(value);
                        }
                    }

                    break;
                }
            }

            if(outputPath != null && !outputPath.isAbsolute()) {
                if(outputPath.isAbsolute()) {
                    return outputPath;
                }

                return projectPath.append(outputPath);
            }
        }
        catch(CoreException e) {
            Plugin.log(e);
        }

        return defaultFolder != null ? projectPath.append(defaultFolder) : projectPath;
    }
}
