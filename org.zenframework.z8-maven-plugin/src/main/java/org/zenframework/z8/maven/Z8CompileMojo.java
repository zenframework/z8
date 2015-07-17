package org.zenframework.z8.maven;

import java.io.File;
import java.lang.reflect.Field;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;
import org.codehaus.plexus.components.io.fileselectors.IncludeExcludeFileSelector;
import org.codehaus.plexus.util.ReflectionUtils;
import org.codehaus.plexus.util.StringUtils;

import org.zenframework.z8.compiler.cmd.Main;

@Mojo(name = "compile-bl", defaultPhase = LifecyclePhase.GENERATE_SOURCES, requiresDependencyResolution = ResolutionScope.COMPILE)
public class Z8CompileMojo extends AbstractMojo {

    @Parameter(defaultValue="${project}", readonly=true, required=true)
    private MavenProject project;
    
    @Parameter(defaultValue = "${basedir}")
    protected String srcPath;

    @Parameter(defaultValue = "${basedir}/.java")
    protected String outputPath;

    @Parameter
    protected String[] requiresPaths;

    @Parameter
    protected String[] excludes;

    @Parameter(property = "silent", defaultValue = "false")
    protected boolean silent;

    @Parameter(property = "dependency.useJvmChmod", defaultValue = "true")
    protected boolean useJvmChmod = true;

    @Parameter(property = "dependency.ignorePermissions", defaultValue = "false")
    protected boolean ignorePermissions;

    @Component
    private ArchiverManager archiverManager;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        this.
        getLog().info("Starting bl compilation ...");
        getLog().info("Sources path: " + srcPath);
        getLog().info("Output path: " + outputPath);
        if (requiresPaths != null) {
            for (String requirePath : requiresPaths) {
                getLog().info("Require path: " + requirePath);
            }
        }
        try {
            FileUtils.forceMkdir(new File(outputPath));
            Main.compile(project.getArtifactId(), srcPath, outputPath, requiresPaths);
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

    public void setSrcPath(String srcPath) {
        this.srcPath = srcPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public void setRequiresPaths(String[] requiresPaths) {
        this.requiresPaths = requiresPaths;
    }

    public void setSilent(boolean silent) {
        this.silent = silent;
    }

    public void setArchiverManager(ArchiverManager archiverManager) {
        this.archiverManager = archiverManager;
    }

    protected void unpack(Artifact artifact, File location, String includes, String excludes) throws MojoExecutionException {
        File file = artifact.getFile();
        try {
            logUnpack(file, location, includes, excludes);

            location.mkdirs();

            if (file.isDirectory()) {
                // usual case is a future jar packaging, but there are special cases: classifier and other packaging
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

            if (StringUtils.isNotEmpty(excludes) || StringUtils.isNotEmpty(includes)) {
                // Create the selectors that will filter
                // based on include/exclude parameters
                // MDEP-47
                IncludeExcludeFileSelector[] selectors = new IncludeExcludeFileSelector[] { new IncludeExcludeFileSelector() };

                if (StringUtils.isNotEmpty(excludes)) {
                    selectors[0].setExcludes(excludes.split(","));
                }

                if (StringUtils.isNotEmpty(includes)) {
                    selectors[0].setIncludes(includes.split(","));
                }

                unArchiver.setFileSelectors(selectors);
            }
            if (this.silent) {
                silenceUnarchiver(unArchiver);
            }

            unArchiver.extract();
        } catch (NoSuchArchiverException e) {
            throw new MojoExecutionException("Unknown archiver type", e);
        } catch (ArchiverException e) {
            throw new MojoExecutionException("Error unpacking file: " + file + " to: " + location + "\r\n" + e.toString(), e);
        }
    }

    private void logUnpack(File file, File location, String includes, String excludes) {
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
            msg.append(includes);
            msg.append("\" and excludes \"");
            msg.append(excludes);
            msg.append("\"");
        } else if (includes != null) {
            msg.append(" with includes \"");
            msg.append(includes);
            msg.append("\"");
        } else if (excludes != null) {
            msg.append(" with excludes \"");
            msg.append(excludes);
            msg.append("\"");
        }

        getLog().info(msg.toString());
    }

    private void silenceUnarchiver(UnArchiver unArchiver) {
        // dangerous but handle any errors. It's the only way to silence the unArchiver.
        try {
            Field field = ReflectionUtils.getFieldByNameIncludingSuperclasses("logger", unArchiver.getClass());

            field.setAccessible(true);

            field.set(unArchiver, this.getLog());
        } catch (Exception e) {
            // was a nice try. Don't bother logging because the log is silent.
        }
    }

}
