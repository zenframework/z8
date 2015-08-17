package org.zenframework.z8.maven;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.zenframework.z8.compiler.cmd.Main;

@Mojo(name = "compile-bl", defaultPhase = LifecyclePhase.GENERATE_SOURCES, requiresDependencyResolution = ResolutionScope.COMPILE)
public class Z8CompileMojo extends Z8AbstractMojo {

    @Parameter(defaultValue = "${basedir}/.java")
    protected String outputPath;

    @Parameter
    protected String[] requiresPaths;

    @SuppressWarnings("unchecked")
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        List<String> requiresPaths = new LinkedList<String>();

        // Prepare dependencies
        Set<Artifact> artifacts = (Set<Artifact>) project.getArtifacts();
        for (Artifact artifact : artifacts) {
            if (classifier.equals(artifact.getClassifier())) {
                File targetPath = new File(System.getProperty("java.io.tmpdir") + '/' + artifact.getGroupId() + '.'
                        + artifact.getArtifactId());
                unpack(artifact, targetPath);
                try {
                    requiresPaths.add(targetPath.getCanonicalPath());
                } catch (IOException e) {
                    throw new MojoFailureException("Error while preparing dependency " + artifact.getGroupId() + ':'
                            + artifact.getArtifactId() + ':' + artifact.getVersion() + ':' + artifact.getClassifier(), e);
                }
            }
        }
        if (this.requiresPaths != null) {
            requiresPaths.addAll(Arrays.asList(this.requiresPaths));
        }

        // Compile
        getLog().info("Starting bl compilation ...");
        getLog().info("Sources path: " + sourcePath);
        getLog().info("Output path: " + outputPath);
        for (String requirePath : requiresPaths) {
            getLog().info("Require path: " + requirePath);
        }
        try {
            FileUtils.forceMkdir(new File(outputPath));
            Main.compile(project.getArtifactId(), sourcePath, outputPath,
                    requiresPaths.toArray(new String[requiresPaths.size()]));
        } catch (Throwable e) {
            throw new MojoFailureException("Compilation error: " + e.getMessage(), e);
        }
        if (excludes != null) {
            for (String exclude : excludes) {
                try {
                    File file = new File(outputPath + "/" + exclude.replace('.', '/') + ".java");
                    getLog().info("Excluding class [" + exclude + "] (file '" + file + "')");
                    if (file.exists()) {
                        file.delete();
                    }
                } catch (Throwable e) {
                    getLog().warn("Can't exclude class [" + exclude + "]", e);
                }
            }
        }

    }

    public void setRequiresPaths(String[] requiresPaths) {
        this.requiresPaths = requiresPaths;
    }

}
