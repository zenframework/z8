package org.zenframework.z8.maven;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.FileUtils;

@Mojo(name = "clean", defaultPhase = LifecyclePhase.CLEAN)
public class Z8CleanMojo extends AbstractMojo {

    @Parameter(defaultValue = "${basedir}/.java")
    protected File outputPath;

    @Parameter(defaultValue = "${basedir}", readonly = true)
    private File basedir;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            if (!outputPath.getCanonicalPath().startsWith(basedir.getCanonicalPath())) {
                getLog().warn("Skipping cleaning. Output '" + outputPath + "' is not in basedir '" + basedir + "'");
            } else {
                getLog().info("Cleaning output directory '" + outputPath + "'");
                if (outputPath.exists()) {
                    FileUtils.cleanDirectory(outputPath);
                }
            }
        } catch (IOException e) {
            throw new MojoFailureException("Can't clean output directory '" + outputPath + "'", e);
        }
    }

    public void setOutputPath(File outputPath) {
        this.outputPath = outputPath;
    }

    public void setBasedir(File basedir) {
        this.basedir = basedir;
    }

}
