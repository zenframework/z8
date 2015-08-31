package org.zenframework.z8.maven;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;
import org.codehaus.plexus.archiver.util.DefaultFileSet;
import org.codehaus.plexus.util.ReflectionUtils;

import edu.emory.mathcs.backport.java.util.Arrays;

public abstract class Z8AbstractMojo extends AbstractMojo {

    private static final String[] DEFAULT_EXCLUDES = new String[] {};

    private static final String[] DEFAULT_INCLUDES = new String[] { "**/*.bl" };

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;

    @Parameter(defaultValue = "${basedir}")
    protected String sourcePath;

    @Parameter
    protected String[] includes;

    @Parameter
    protected String[] excludes;

    @Parameter(defaultValue = "bl")
    protected String classifier;

    @Parameter(defaultValue = "jar")
    protected String type;

    @Component
    protected ArchiverManager archiverManager;

    @Parameter(property = "silent", defaultValue = "false")
    protected boolean silent;

    @Parameter(property = "dependency.useJvmChmod", defaultValue = "true")
    protected boolean useJvmChmod = true;

    @Parameter(property = "dependency.ignorePermissions", defaultValue = "false")
    protected boolean ignorePermissions;

    protected String[] getIncludes() {
        if (includes != null && includes.length > 0) {
            return includes;
        }
        return DEFAULT_INCLUDES;
    }

    protected String[] getExcludes() {
        if (excludes != null && excludes.length > 0) {
            return excludes;
        }
        return DEFAULT_EXCLUDES;
    }

    protected void pack(File sourcePath, Artifact artifact) throws MojoExecutionException {

        String[] includes = getIncludes();
        String[] excludes = getExcludes();
        File file = artifact.getFile();

        try {

            file.getParentFile().mkdirs();

            Archiver archiver;
            try {
                archiver = archiverManager.getArchiver(artifact.getType());
                getLog().debug("Found archiver by type: " + archiver);
            } catch (NoSuchArchiverException e) {
                archiver = archiverManager.getArchiver(file);
                getLog().debug("Found archiver by extension: " + archiver);
            }

            archiver.setUseJvmChmod(useJvmChmod);
            archiver.setIgnorePermissions(ignorePermissions);
            archiver.setDestFile(file);
            archiver.addFileSet(DefaultFileSet.fileSet(sourcePath).includeExclude(includes, excludes));

            if (silent) {
                silenceArchiver(archiver);
            }

            archiver.createArchive();

        } catch (NoSuchArchiverException e) {
            throw new MojoExecutionException("Unknown archiver type", e);
        } catch (ArchiverException e) {
            throw new MojoExecutionException("Error packing file: " + file + " from: " + sourcePath + "\r\n" + e.toString(),
                    e);
        } catch (IOException e) {
            throw new MojoExecutionException("Error packing file: " + file + " from: " + sourcePath + "\r\n" + e.toString(),
                    e);
        }

    }

    protected void unpack(Artifact artifact, File location) throws MojoExecutionException {

        String[] includes = getIncludes();
        String[] excludes = getExcludes();
        File file = artifact.getFile();

        try {
            logUnpack(file, location, includes, excludes);

            location.mkdirs();

            if (file.isDirectory()) {
                // usual case is a future jar packaging, but there are special
                // cases: classifier and other packaging
                throw new MojoExecutionException("Artifact has not been packaged yet. When used on reactor artifact, "
                        + "unpack should be executed after packaging: see MDEP-98.");
            }

            UnArchiver unArchiver;
            try {
                unArchiver = archiverManager.getUnArchiver(artifact.getType());
                getLog().debug("Found unArchiver by type: " + unArchiver);
            } catch (NoSuchArchiverException e) {
                unArchiver = archiverManager.getUnArchiver(file);
                getLog().debug("Found unArchiver by extension: " + unArchiver);
            }

            unArchiver.setUseJvmChmod(useJvmChmod);
            unArchiver.setIgnorePermissions(ignorePermissions);
            unArchiver.setSourceFile(file);
            unArchiver.setDestDirectory(location);

            // This produces NoClassDefFoundException org.apache.maven.shared.utils.io.MatchPattern
            // if (excludes.length > 0 || includes.length > 0) {
            //     IncludeExcludeFileSelector[] selectors = new IncludeExcludeFileSelector[] { new IncludeExcludeFileSelector() };
            //     selectors[0].setExcludes(excludes);
            //     selectors[0].setIncludes(includes);
            //     unArchiver.setFileSelectors(selectors);
            // }

            if (silent) {
                silenceUnarchiver(unArchiver);
            }

            unArchiver.extract();
        } catch (NoSuchArchiverException e) {
            throw new MojoExecutionException("Unknown archiver type", e);
        } catch (ArchiverException e) {
            throw new MojoExecutionException("Error unpacking file: " + file + " to: " + location + "\r\n" + e.toString(), e);
        }
    }

    private void logUnpack(File file, File location, String[] includes, String[] excludes) {
        if (!getLog().isInfoEnabled()) {
            return;
        }

        StringBuilder msg = new StringBuilder();
        msg.append("Unpacking ");
        msg.append(file);
        msg.append(" to ");
        msg.append(location);

        if (includes != null && excludes != null) {
            msg.append(" with includes \"");
            msg.append(Arrays.toString(includes));
            msg.append("\" and excludes \"");
            msg.append(Arrays.toString(excludes));
            msg.append("\"");
        } else if (includes != null) {
            msg.append(" with includes \"");
            msg.append(Arrays.toString(includes));
            msg.append("\"");
        } else if (excludes != null) {
            msg.append(" with excludes \"");
            msg.append(Arrays.toString(excludes));
            msg.append("\"");
        }

        getLog().info(msg.toString());
    }

    private void silenceArchiver(Archiver archiver) {
        // dangerous but handle any errors. It's the only way to silence the
        // unArchiver.
        try {
            Field field = ReflectionUtils.getFieldByNameIncludingSuperclasses("logger", archiver.getClass());

            field.setAccessible(true);

            field.set(archiver, this.getLog());
        } catch (Exception e) {
            // was a nice try. Don't bother logging because the log is silent.
        }
    }

    private void silenceUnarchiver(UnArchiver unArchiver) {
        // dangerous but handle any errors. It's the only way to silence the
        // unArchiver.
        try {
            Field field = ReflectionUtils.getFieldByNameIncludingSuperclasses("logger", unArchiver.getClass());

            field.setAccessible(true);

            field.set(unArchiver, this.getLog());
        } catch (Exception e) {
            // was a nice try. Don't bother logging because the log is silent.
        }
    }

}
