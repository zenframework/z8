package org.zenframework.z8.pde.refactoring.changes;

import java.net.URI;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.participants.ReorgExecutionLog;

import org.zenframework.z8.pde.refactoring.reorg.INewNameQuery;

@SuppressWarnings("deprecation")
abstract class ResourceReorgChange extends Z8Change {
    private final IPath fResourcePath;
    private final boolean fIsFile;
    private final IPath fDestinationPath;
    private final boolean fIsDestinationProject;
    private final INewNameQuery fNewNameQuery;

    ResourceReorgChange(IResource res, IContainer dest, INewNameQuery nameQuery) {
        fIsFile = (res instanceof IFile);
        fResourcePath = getResourcePath(res);
        Assert.isTrue(dest instanceof IProject || dest instanceof IFolder);
        fIsDestinationProject = (dest instanceof IProject);
        fDestinationPath = getResourcePath(dest);
        fNewNameQuery = nameQuery;
    }

    protected abstract Change doPerformReorg(IPath path, IProgressMonitor pm) throws CoreException;

    @Override
    public final Change perform(IProgressMonitor pm) throws CoreException, OperationCanceledException {
        try {
            pm.beginTask(getName(), 2);
            String newName = getNewResourceName();
            IResource resource = getResource();
            boolean performReorg = deleteIfAlreadyExists(new SubProgressMonitor(pm, 1), newName);
            if(!performReorg)
                return null;
            final Change result = doPerformReorg(getDestinationPath(newName), new SubProgressMonitor(pm, 1));
            markAsExecuted(resource);
            return result;
        }
        finally {
            pm.done();
        }
    }

    protected IPath getDestinationPath(String newName) {
        return getDestination().getFullPath().append(newName);
    }

    public static boolean areEqualInWorkspaceOrOnDisk(IResource r1, IResource r2) {
        if(r1 == null || r2 == null)
            return false;
        if(r1.equals(r2))
            return true;
        URI r1Location = r1.getLocationURI();
        URI r2Location = r2.getLocationURI();
        if(r1Location == null || r2Location == null)
            return false;
        return r1Location.equals(r2Location);
    }

    private boolean deleteIfAlreadyExists(IProgressMonitor pm, String newName) throws CoreException {
        pm.beginTask("", 1);
        IResource current = getDestination().findMember(newName);
        if(current == null)
            return true;
        if(!current.exists())
            return true;
        IResource resource = getResource();

        if(areEqualInWorkspaceOrOnDisk(resource, current))
            return false;

        if(current instanceof IFile)
            ((IFile)current).delete(false, true, new SubProgressMonitor(pm, 1));
        else if(current instanceof IFolder)
            ((IFolder)current).delete(false, true, new SubProgressMonitor(pm, 1));

        return true;
    }

    private String getNewResourceName() throws OperationCanceledException {
        if(fNewNameQuery == null)
            return getResource().getName();

        String name = fNewNameQuery.getNewName();

        if(name == null)
            return getResource().getName();

        return name;
    }

    @Override
    public Object getModifiedElement() {
        return getResource();
    }

    private IFile getFile() {
        return getFile(fResourcePath);
    }

    private IFolder getFolder() {
        return getFolder(fResourcePath);
    }

    protected IResource getResource() {
        if(fIsFile)
            return getFile();
        else
            return getFolder();
    }

    static IPath getResourcePath(IResource resource) {
        return resource.getFullPath().removeFirstSegments(
                ResourcesPlugin.getWorkspace().getRoot().getFullPath().segmentCount());
    }

    static IFile getFile(IPath path) {
        return ResourcesPlugin.getWorkspace().getRoot().getFile(path);
    }

    static IFolder getFolder(IPath path) {
        return ResourcesPlugin.getWorkspace().getRoot().getFolder(path);
    }

    static IProject getProject(IPath path) {
        return (IProject)ResourcesPlugin.getWorkspace().getRoot().findMember(path);
    }

    IContainer getDestination() {
        if(fIsDestinationProject)
            return getProject(fDestinationPath);
        else
            return getFolder(fDestinationPath);
    }

    protected int getReorgFlags() {
        return IResource.KEEP_HISTORY | IResource.SHALLOW;
    }

    private void markAsExecuted(IResource resource) {
        ReorgExecutionLog log = (ReorgExecutionLog)getAdapter(ReorgExecutionLog.class);
        if(log != null) {
            log.markAsProcessed(resource);
        }
    }
}
