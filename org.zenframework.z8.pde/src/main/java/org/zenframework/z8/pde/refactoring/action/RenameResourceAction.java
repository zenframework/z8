package org.zenframework.z8.pde.refactoring.action;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchSite;

import org.zenframework.z8.pde.refactoring.ExceptionHandler;
import org.zenframework.z8.pde.refactoring.RefactoringAvailabilityTester;
import org.zenframework.z8.pde.refactoring.RefactoringExecutionStarter;
import org.zenframework.z8.pde.refactoring.messages.RefactoringMessages;

public class RenameResourceAction extends SelectionDispatchAction {
    public RenameResourceAction(IWorkbenchSite site) {
        super(site);
    }

    @Override
    public void selectionChanged(IStructuredSelection selection) {
        IResource element = getResource(selection);

        if(element == null) {
            setEnabled(false);
        }
        else {
            setEnabled(RefactoringAvailabilityTester.isRenameAvailable(element));
        }
    }

    @Override
    public void run(IStructuredSelection selection) {
        IResource resource = getResource(selection);

        if(!RefactoringAvailabilityTester.isRenameAvailable(resource))
            return;

        try {
            RefactoringExecutionStarter.startRenameResourceRefactoring(resource, getShell());
        }
        catch(CoreException e) {
            ExceptionHandler.handle(e, RefactoringMessages.RenameElementAction_name,
                    RefactoringMessages.RenameElementAction_exception);
        }
    }

    private static IResource getResource(IStructuredSelection selection) {
        if(selection.size() != 1) {
            return null;
        }

        Object first = selection.getFirstElement();

        if(first instanceof IResource) {
            return (IResource)first;
        }
        return null;
    }
}
