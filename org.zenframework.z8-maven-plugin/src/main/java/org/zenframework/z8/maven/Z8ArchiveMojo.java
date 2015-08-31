package org.zenframework.z8.maven;

import java.io.File;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.artifact.AttachedArtifact;

@Mojo(name = "archive-bl", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.NONE)
public class Z8ArchiveMojo extends Z8AbstractMojo {

    @Parameter(defaultValue = "${project.build.directory}")
    protected String outputPath;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Artifact blArchive = new AttachedArtifact(project.getArtifact(), type, classifier, project.getArtifact()
                .getArtifactHandler());
        blArchive.setFile(getArtifactFile(blArchive));
        pack(new File(sourcePath), blArchive);
        project.addAttachedArtifact(blArchive);
    }

    protected File getArtifactFile(Artifact artifact) {
        return new File(new StringBuilder(100).append(outputPath).append(File.separator).append(artifact.getArtifactId())
                .append('-').append(artifact.getVersion()).append('-').append(artifact.getClassifier()).append('.')
                .append(artifact.getType()).toString());
    }

}
