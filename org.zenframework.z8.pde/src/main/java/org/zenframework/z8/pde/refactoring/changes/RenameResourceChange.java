package org.zenframework.z8.pde.refactoring.changes;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.ChangeDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringChangeDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import org.zenframework.z8.pde.refactoring.messages.Messages;
import org.zenframework.z8.pde.refactoring.messages.RefactoringMessages;

public class RenameResourceChange extends Z8Change {
    private String m_comment;
    private RefactoringDescriptor m_descriptor;
    private String m_newName;
    private IPath m_resourcePath;
    private long m_stampToRestore;

    public static IPath renamedResourcePath(IPath path, String newName) {
        return path.removeLastSegments(1).append(newName);
    }

    private RenameResourceChange(RefactoringDescriptor descriptor, IPath resourcePath, String newName, String comment,
            long stampToRestore) {
        m_descriptor = descriptor;
        m_resourcePath = resourcePath;
        m_newName = newName;
        m_comment = comment;
        m_stampToRestore = stampToRestore;
    }

    public RenameResourceChange(RefactoringDescriptor descriptor, IResource resource, String newName, String comment) {
        this(descriptor, resource.getFullPath(), newName, comment, IResource.NULL_STAMP);
    }

    @Override
    public ChangeDescriptor getDescriptor() {
        if(m_descriptor != null)
            return new RefactoringChangeDescriptor(m_descriptor);
        return null;
    }

    @Override
    public Object getModifiedElement() {
        return getResource();
    }

    @Override
    public String getName() {
        return Messages.format(RefactoringMessages.RenameResourceChange_name, new String[] { m_resourcePath.toString(),
                m_newName });
    }

    public String getNewName() {
        return m_newName;
    }

    private IResource getResource() {
        return ResourcesPlugin.getWorkspace().getRoot().findMember(m_resourcePath);
    }

    @Override
    public RefactoringStatus isValid(IProgressMonitor pm) throws CoreException {
        IResource resource = getResource();

        if(resource == null || !resource.exists()) {
            return RefactoringStatus.createFatalErrorStatus(Messages.format(
                    RefactoringMessages.RenameResourceChange_does_not_exist, m_resourcePath.toString()));
        }
        else {
            return super.isValid(pm, DIRTY);
        }
    }

    @Override
    public Change perform(IProgressMonitor pm) throws CoreException {
        try {
            pm.beginTask(RefactoringMessages.RenameResourceChange_rename_resource, 1);

            IResource resource = getResource();
            long currentStamp = resource.getModificationStamp();

            IPath newPath = renamedResourcePath(m_resourcePath, m_newName);

            resource.move(newPath, IResource.SHALLOW, pm);

            if(m_stampToRestore != IResource.NULL_STAMP) {
                IResource newResource = ResourcesPlugin.getWorkspace().getRoot().findMember(newPath);
                newResource.revertModificationStamp(m_stampToRestore);
            }

            String oldName = m_resourcePath.lastSegment();

            return new RenameResourceChange(null, newPath, oldName, m_comment, currentStamp);
        }
        finally {
            pm.done();
        }
    }
}
