package org.zenframework.z8.pde.refactoring.changes;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.ChangeDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringChangeDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;

import org.zenframework.z8.compiler.workspace.Workspace;
import org.zenframework.z8.pde.refactoring.messages.RefactoringMessages;

@SuppressWarnings("deprecation")
public abstract class AbstractLanguageElementRenameChange extends Z8Change {
    private final String m_newName;
    private final String m_oldName;
    private final IPath m_resourcePath;
    private final long m_stampToRestore;
    private final String m_comment;
    private final RefactoringDescriptor m_descriptor;

    protected AbstractLanguageElementRenameChange(RefactoringDescriptor descriptor, IPath resourcePath, String oldName,
            String newName, String comment) {
        this(descriptor, resourcePath, oldName, newName, comment, IResource.NULL_STAMP);
    }

    protected AbstractLanguageElementRenameChange(RefactoringDescriptor descriptor, IPath resourcePath, String oldName,
            String newName, String comment, long stampToRestore) {
        m_descriptor = descriptor;
        m_resourcePath = resourcePath;
        m_oldName = oldName;
        m_newName = newName;
        m_comment = comment;
        m_stampToRestore = stampToRestore;
    }

    protected final IResource getResource() {
        return ResourcesPlugin.getWorkspace().getRoot().findMember(m_resourcePath);
    }

    @Override
    public Object getModifiedElement() {
        return Workspace.getInstance().getResource(getResource());
    }

    protected abstract Change createUndoChange(long stampToRestore) throws CoreException;

    protected abstract void doRename(IProgressMonitor pm) throws CoreException;

    protected abstract IPath createNewPath();

    @Override
    public final Change perform(IProgressMonitor pm) throws CoreException {
        try {
            pm.beginTask(RefactoringMessages.AbstractRenameChange_Renaming, 1);
            IResource resource = getResource();
            IPath newPath = createNewPath();
            Change result = createUndoChange(resource.getModificationStamp());
            doRename(new SubProgressMonitor(pm, 1));
            if(m_stampToRestore != IResource.NULL_STAMP) {
                IResource newResource = ResourcesPlugin.getWorkspace().getRoot().findMember(newPath);
                newResource.revertModificationStamp(m_stampToRestore);
            }
            return result;
        }
        finally {
            pm.done();
        }
    }

    public String getNewName() {
        return m_newName;
    }

    public String getComment() {
        return m_comment;
    }

    protected IPath getResourcePath() {
        return m_resourcePath;
    }

    public String getOldName() {
        return m_oldName;
    }

    @Override
    public final ChangeDescriptor getDescriptor() {
        if(m_descriptor != null)
            return new RefactoringChangeDescriptor(m_descriptor);
        return null;
    }
}
