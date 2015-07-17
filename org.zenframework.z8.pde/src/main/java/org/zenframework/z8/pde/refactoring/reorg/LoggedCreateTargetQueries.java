package org.zenframework.z8.pde.refactoring.reorg;

import java.net.URI;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

import org.zenframework.z8.compiler.workspace.Folder;
import org.zenframework.z8.pde.Z8ProjectNature;
import org.zenframework.z8.pde.Plugin;
import org.zenframework.z8.pde.refactoring.messages.RefactoringMessages;

public final class LoggedCreateTargetQueries implements ICreateTargetQueries {
    private final class CreateTargetQuery implements ICreateTargetQuery {
        private void createZ8Project(IProject project) throws CoreException {
            if(!project.exists()) {
                createProject(project, null, new NullProgressMonitor());
                addZ8Nature(project, new NullProgressMonitor());
            }
        }

        public void createProject(IProject project, URI locationURI, IProgressMonitor monitor) throws CoreException {
            if(monitor == null) {
                monitor = new NullProgressMonitor();
            }

            monitor.beginTask(RefactoringMessages.BuildPathsBlock_operationdesc_project, 10);

            try {
                if(!project.exists()) {
                    IProjectDescription desc = project.getWorkspace().newProjectDescription(project.getName());

                    if(locationURI != null && ResourcesPlugin.getWorkspace().getRoot().getLocationURI().equals(locationURI)) {
                        locationURI = null;
                    }
                    desc.setLocationURI(locationURI);
                    project.create(desc, monitor);
                    monitor = null;
                }
                if(!project.isOpen()) {
                    project.open(monitor);
                    monitor = null;
                }
            }
            finally {
                if(monitor != null) {
                    monitor.done();
                }
            }
        }

        public void addZ8Nature(IProject project, IProgressMonitor monitor) throws CoreException {
            if(monitor != null && monitor.isCanceled()) {
                throw new OperationCanceledException();
            }

            if(!project.hasNature(Z8ProjectNature.Id)) {
                IProjectDescription description = project.getDescription();
                String[] prevNatures = description.getNatureIds();
                String[] newNatures = new String[prevNatures.length + 1];
                System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
                newNatures[prevNatures.length] = Z8ProjectNature.Id;
                description.setNatureIds(newNatures);
                project.setDescription(description, monitor);
            }
            else {
                if(monitor != null) {
                    monitor.worked(1);
                }
            }
        }

        @Override
        public Object getCreatedTarget(final Object selection) {
            Object target = fLog.getCreatedElement(selection);

            if(target instanceof Folder) {}
            else if(target instanceof IFolder) {
                try {
                    final IFolder folder = (IFolder)target;
                    final IProject project = folder.getProject();

                    if(!project.exists()) {
                        createZ8Project(project);
                    }

                    if(!folder.exists()) {
                        createFolder(folder, true, true, new NullProgressMonitor());
                    }
                }
                catch(CoreException exception) {
                    Plugin.log(exception);
                    return null;
                }
            }
            return target;
        }

        public void createFolder(IFolder folder, boolean force, boolean local, IProgressMonitor monitor)
                throws CoreException {
            if(!folder.exists()) {
                IContainer parent = folder.getParent();

                if(parent instanceof IFolder) {
                    createFolder((IFolder)parent, force, local, null);
                }

                folder.create(force, local, monitor);
            }
        }

        @Override
        public String getNewButtonLabel() {
            return "unused";
        }
    }

    private final CreateTargetExecutionLog fLog;

    public LoggedCreateTargetQueries(CreateTargetExecutionLog log) {
        fLog = log;
    }

    @Override
    public ICreateTargetQuery createNewFolderQuery() {
        return new CreateTargetQuery();
    }

    public CreateTargetExecutionLog getCreateTargetExecutionLog() {
        return fLog;
    }
}