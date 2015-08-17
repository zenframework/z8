package org.zenframework.z8.maven;

import java.util.Arrays;
import java.util.List;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "add-generated", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class Z8AddSourceMojo extends Z8AbstractMojo {

    private static final List<String> RESOURCES_INCLUDES = Arrays.asList("META-INF/z8.runtime", "META-INF/z8_bl.runtime");

    @Parameter(defaultValue = "${basedir}/.java")
    protected String outputPath;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        // Add generated java sources
        project.addCompileSourceRoot(outputPath);
        // Add generated resources
        Resource resource = new Resource();
        resource.setDirectory(outputPath);
        resource.setIncludes(RESOURCES_INCLUDES);
        project.addResource(resource);
    }

}
