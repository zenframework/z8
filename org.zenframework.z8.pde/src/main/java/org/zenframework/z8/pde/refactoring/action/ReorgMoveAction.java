package org.zenframework.z8.pde.refactoring.action;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.actions.MoveProjectAction;

import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.workspace.Project;
import org.zenframework.z8.compiler.workspace.Resource;
import org.zenframework.z8.compiler.workspace.Workspace;
import org.zenframework.z8.pde.Plugin;
import org.zenframework.z8.pde.refactoring.ExceptionHandler;
import org.zenframework.z8.pde.refactoring.RefactoringAvailabilityTester;
import org.zenframework.z8.pde.refactoring.RefactoringExecutionStarter;
import org.zenframework.z8.pde.refactoring.messages.RefactoringMessages;

public class ReorgMoveAction extends SelectionDispatchAction {
    public ReorgMoveAction(IWorkbenchSite site) {
        super(site);
        setText(RefactoringMessages.ReorgMoveAction_3);
        setDescription(RefactoringMessages.ReorgMoveAction_4);
    }

    public IResource[] getResources(List<Object> elements) {
        List<IResource> resources = new ArrayList<IResource>(elements.size());

        for(Object object : elements) {
            if(object instanceof IResource) {
                resources.add((IResource)object);
            }
            else if(object instanceof Resource) {
                resources.add(((Resource)object).getResource());
            }
            else if(object instanceof ILanguageElement) {
                resources.add(((ILanguageElement)object).getCompilationUnit().getResource());
            }
        }
        return resources.toArray(new IResource[resources.size()]);
    }

    public ILanguageElement[] getLanguageElements(List<Object> elements) {
        List<ILanguageElement> result = new ArrayList<ILanguageElement>(elements.size());

        for(Object object : elements) {
            if(object instanceof ILanguageElement) {
                result.add((ILanguageElement)object);
            }
            else if(object instanceof IResource) {
                IResource r = (IResource)object;
                result.add(Workspace.getInstance().getResource(r));
            }
        }
        return result.toArray(new ILanguageElement[result.size()]);
    }

    public boolean containsOnlyProjects(List<Object> elements) {
        if(elements.isEmpty())
            return false;

        for(Object object : elements) {
            if(!(object instanceof Project || object instanceof IProject)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void selectionChanged(IStructuredSelection selection) {
        if(!selection.isEmpty()) {
            @SuppressWarnings("unchecked")
            List<Object> elements = selection.toList();

            if(containsOnlyProjects(elements)) {
                setEnabled(createWorkbenchAction(selection).isEnabled());
                return;
            }

            try {
                IResource[] resources = getResources(elements);

                ILanguageElement[] languageElements = getLanguageElements(elements);

                if(elements.size() != resources.length + languageElements.length)
                    setEnabled(false);
                else
                    setEnabled(RefactoringAvailabilityTester.isMoveAvailable(resources, languageElements));
            }
            catch(CoreException e) {
                Plugin.log(e);
                setEnabled(false);
            }
        }

        setEnabled(false);
    }

    @Override
    public void selectionChanged(ITextSelection selection) {
        setEnabled(true);
    }

    private MoveProjectAction createWorkbenchAction(IStructuredSelection selection) {
        MoveProjectAction action = new MoveProjectAction(getSite());
        action.selectionChanged(selection);
        return action;
    }

    @Override
    public void run(IStructuredSelection selection) {
        @SuppressWarnings("unchecked")
        List<Object> elements = selection.toList();

        if(containsOnlyProjects(elements)) {
            createWorkbenchAction(selection).run();
            return;
        }

        try {
            IResource[] resources = getResources(elements);
            ILanguageElement[] languageElements = getLanguageElements(elements);
            if(RefactoringAvailabilityTester.isMoveAvailable(resources, languageElements)) {
                RefactoringExecutionStarter.startMoveRefactoring(resources, languageElements, getShell());
            }
        }
        catch(CoreException e) {
            ExceptionHandler.handle(e, RefactoringMessages.OpenRefactoringWizardAction_refactoring,
                    RefactoringMessages.OpenRefactoringWizardAction_exception);
        }
    }
}
