package org.zenframework.z8.pde.refactoring.changes;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.workspace.CompilationUnit;
import org.zenframework.z8.pde.refactoring.messages.Messages;
import org.zenframework.z8.pde.refactoring.messages.RefactoringMessages;
import org.zenframework.z8.pde.refactoring.operations.MultiOperation;
import org.zenframework.z8.pde.refactoring.operations.RenameResourceElementsOperation;

public class RenameCompilationUnitChange extends AbstractLanguageElementRenameChange {
    public RenameCompilationUnitChange(RefactoringDescriptor descriptor, CompilationUnit unit, String newName, String comment) {
        this(descriptor, unit.getResource().getFullPath(), unit.getName(), newName, comment, IResource.NULL_STAMP);
    }

    private RenameCompilationUnitChange(RefactoringDescriptor descriptor, IPath resourcePath, String oldName,
            String newName, String comment, long stampToRestore) {
        super(descriptor, resourcePath, oldName, newName, comment, stampToRestore);
    }

    @Override
    protected IPath createNewPath() {
        if(getResourcePath().getFileExtension() != null) {
            return getResourcePath().removeFileExtension().removeLastSegments(1).append(getNewName());
        }
        else {
            return getResourcePath().removeLastSegments(1).append(getNewName());
        }
    }

    @Override
    protected Change createUndoChange(long stampToRestore) throws CoreException {
        return new RenameCompilationUnitChange(null, createNewPath(), getNewName(), getOldName(), getComment(),
                stampToRestore);
    }

    @Override
    protected void doRename(IProgressMonitor monitor) throws CoreException {
        CompilationUnit compilationUnit = (CompilationUnit)getModifiedElement();

        if(compilationUnit != null) {
            ILanguageElement[] elements = new ILanguageElement[] { compilationUnit };
            ILanguageElement[] destinations = new ILanguageElement[] { compilationUnit.getFolder() };
            String[] renamings = new String[] { getNewName() };

            MultiOperation op = new RenameResourceElementsOperation(elements, destinations, renamings, false);
            op.runOperation(monitor);
        }
    }

    @Override
    public String getName() {
        return Messages.format(RefactoringMessages.RenameCompilationUnitChange_name, new String[] { getOldName(),
                getNewName() });
    }

    @Override
    public RefactoringStatus isValid(IProgressMonitor pm) throws CoreException {
        return super.isValid(pm, READ_ONLY | SAVE_IF_DIRTY);
    }
}
