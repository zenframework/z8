package org.zenframework.z8.pde.refactoring.reorg;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.workspace.CompilationUnit;
import org.zenframework.z8.compiler.workspace.Folder;
import org.zenframework.z8.compiler.workspace.Project;
import org.zenframework.z8.compiler.workspace.Resource;
import org.zenframework.z8.compiler.workspace.Workspace;

public class StandardLanguageElementContentProvider implements ITreeContentProvider {
    protected static final Object[] NO_CHILDREN = new Object[0];
    protected boolean fProvideMembers;

    public StandardLanguageElementContentProvider() {
        this(false);
    }

    public StandardLanguageElementContentProvider(boolean provideMembers) {
        fProvideMembers = provideMembers;
    }

    public boolean getProvideMembers() {
        return fProvideMembers;
    }

    public void setProvideMembers(boolean b) {
        fProvideMembers = b;
    }

    @Override
    public Object[] getElements(Object parent) {
        return getChildren(parent);
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}

    @Override
    public void dispose() {}

    @Override
    public Object[] getChildren(Object element) {
        if(!exists(element))
            return NO_CHILDREN;
        try {
            if(element instanceof Workspace)
                return Workspace.getInstance().getProjects();
            if(element instanceof Project)
                return getFolderContent((Project)element);
            if(element instanceof Folder)
                return getFolderContent((Folder)element);
            if(element instanceof IFolder)
                return getFolderContent((IFolder)element);
        }
        catch(CoreException e) {
            return NO_CHILDREN;
        }
        return NO_CHILDREN;
    }

    @Override
    public boolean hasChildren(Object element) {
        if(getProvideMembers()) {
            if(element instanceof CompilationUnit) {
                return true;
            }
        }
        else {
            if(element instanceof CompilationUnit || element instanceof IFile)
                return false;
        }

        if(element instanceof Project) {
            Project project = (Project)element;

            if(!project.getResource().getProject().isOpen()) {
                return false;
            }
        }

        Object[] children = getChildren(element);
        return (children != null) && children.length > 0;
    }

    @Override
    public Object getParent(Object element) {
        if(!exists(element))
            return null;
        return internalGetParent(element);
    }

    protected Object[] getFolderContent(Folder folder) throws CoreException {
        return folder.getFolders();
    }

    protected Object[] getFolderContent(IFolder folder) throws CoreException {
        IResource[] members = folder.members();

        Project project = Workspace.getInstance().getProject(folder.getProject());

        if(project == null || !project.getResource().exists())
            return members;

        List<IResource> resources = new ArrayList<IResource>();

        for(int i = 0; i < members.length; i++) {
            IResource member = members[i];
            resources.add(member);
        }

        return resources.toArray();
    }

    protected boolean isFolderEmpty(ILanguageElement element) throws CoreException {
        if(element instanceof Folder) {
            Folder folder = (Folder)element;

            if(folder.getResource().exists() && !folder.hasCompilationUnits() && folder.hasSubfolders())
                return true;
        }
        return false;
    }

    protected boolean exists(Object element) {
        if(element == null) {
            return false;
        }
        if(element instanceof IResource) {
            return ((IResource)element).exists();
        }
        if(element instanceof Resource) {
            return ((Resource)element).getResource().exists();
        }
        if(element instanceof ILanguageElement) {
            return ((ILanguageElement)element).getCompilationUnit().getResource().exists();
        }
        return true;
    }

    protected Object internalGetParent(Object element) {
        if(element instanceof IResource) {
            IResource parent = ((IResource)element).getParent();

            Resource resource = Workspace.getInstance().getResource(parent);

            if(resource != null)
                return resource;

            return parent;
        }
        else if(element instanceof ILanguageElement) {
            if(element instanceof Resource) {
                return ((Resource)element).getFolder();
            }

            return ((ILanguageElement)element).getCompilationUnit().getFolder();
        }
        return null;
    }
}
