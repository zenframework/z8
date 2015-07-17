package org.zenframework.z8.pde.refactoring.changes;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.NullChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.workspace.CompilationUnit;
import org.zenframework.z8.compiler.workspace.Folder;
import org.zenframework.z8.compiler.workspace.Workspace;
import org.zenframework.z8.pde.refactoring.messages.Messages;
import org.zenframework.z8.pde.refactoring.messages.RefactoringMessages;
import org.zenframework.z8.pde.refactoring.operations.MultiOperation;
import org.zenframework.z8.pde.refactoring.operations.RenameResourceElementsOperation;

public final class RenameFolderChange extends AbstractLanguageElementRenameChange {
    private static IPath createPath(String folderName) {
        return new Path(folderName.replace('.', IPath.SEPARATOR));
    }

    Map<IResource, Long> m_stamps;

    public RenameFolderChange(Folder folder, String newName) {
        super(null, folder.getResource().getFullPath(), folder.getName(), newName, "", IResource.NULL_STAMP);
    }

    public RenameFolderChange(IPath path, String oldName, String newName, Long stampToRestore, Map<IResource, Long> stamps) {
        super(null, path, oldName, newName, "", stampToRestore);
        m_stamps = stamps;
    }

    @Override
    protected IPath createNewPath() {
        Folder oldFolder = getFolder();
        IPath oldFolderName = createPath(oldFolder.getName());
        IPath newFolderName = createPath(getNewName());
        return getResourcePath().removeLastSegments(oldFolderName.segmentCount()).append(newFolderName);
    }

    protected IPath createNewPath(Folder oldFolder) {
        IPath oldFolderPath = createPath(oldFolder.getName());
        IPath newFolderPath = createPath(getNewName(oldFolder));
        return oldFolder.getPath().removeLastSegments(oldFolderPath.segmentCount()).append(newFolderPath);
    }

    private void addStamps(Map<IResource, Long> stamps, CompilationUnit[] units) {
        for(int i = 0; i < units.length; i++) {
            IResource resource = units[i].getResource();
            long stamp = IResource.NULL_STAMP;
            if(resource != null && (stamp = resource.getModificationStamp()) != IResource.NULL_STAMP) {
                stamps.put(resource, new Long(stamp));
            }
        }
    }

    @Override
    protected Change createUndoChange(long stampToRestore) throws CoreException {
        Folder folder = getFolder();

        if(folder == null)
            return new NullChange();

        Map<IResource, Long> stamps = new HashMap<IResource, Long>();

        addStamps(stamps, folder.getCompilationUnits());

        return new RenameFolderChange(createNewPath(), getNewName(), getOldName(), stampToRestore, stamps);
    }

    @Override
    protected void doRename(IProgressMonitor pm) throws CoreException {
        Folder folder = getFolder();

        if(folder != null) {
            renameFolder(folder, pm, createNewPath(), getNewName());
        }
    }

    @Override
    public String getName() {
        String msg = RefactoringMessages.RenameFolderChange_name;
        return Messages.format(msg, new String[] { getOldName(), getNewName() });
    }

    private String getNewName(Folder subfolder) {
        return getNewName() + subfolder.getName().substring(getOldName().length());
    }

    private Folder getFolder() {
        return (Folder)getModifiedElement();
    }

    @Override
    public RefactoringStatus isValid(IProgressMonitor pm) throws CoreException {
        pm.beginTask("", 2);

        RefactoringStatus result;

        try {
            result = new RefactoringStatus();

            ILanguageElement element = (ILanguageElement)getModifiedElement();

            result.merge(isValid(new SubProgressMonitor(pm, 1), DIRTY));

            if(result.hasFatalError())
                return result;

            if(element != null && element instanceof Folder) {
                Folder folder = (Folder)element;

                if(folder.getResource().exists())
                    isValid(result, folder, new SubProgressMonitor(pm, 1));
            }
        }
        finally {
            pm.done();
        }
        return result;
    }

    private void isValid(RefactoringStatus result, Folder pack, IProgressMonitor pm) throws CoreException {
        CompilationUnit[] units = pack.getCompilationUnits();

        pm.beginTask("", units.length);

        for(int i = 0; i < units.length; i++) {
            pm.subTask(Messages.format(RefactoringMessages.RenameFolderChange_checking_change, pack.getName()));
            checkIfModifiable(result, units[i], READ_ONLY | DIRTY);
            pm.worked(1);
        }
        pm.done();
    }

    private void renameFolder(Folder folder, IProgressMonitor pm, IPath newPath, String newName) throws CoreException,
            CoreException {
        ILanguageElement[] elements = new ILanguageElement[] { folder };
        ILanguageElement[] destinations = new ILanguageElement[] { folder.getFolder() };
        String[] renamings = new String[] { newName };

        MultiOperation op = new RenameResourceElementsOperation(elements, destinations, renamings, false);

        op.runOperation(pm);

        if(m_stamps != null) {
            folder = Workspace.getInstance().getFolder(ResourcesPlugin.getWorkspace().getRoot().getFolder(newPath));

            if(folder != null && folder.getResource().exists()) {
                CompilationUnit[] units = folder.getCompilationUnits();

                for(int i = 0; i < units.length; i++) {
                    IResource resource = units[i].getResource();

                    if(resource != null) {
                        Long stamp = m_stamps.get(resource);

                        if(stamp != null) {
                            resource.revertModificationStamp(stamp.longValue());
                        }
                    }
                }
            }
        }
    }
}
