package org.zenframework.z8.pde.refactoring.action;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchSite;

import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.workspace.Workspace;
import org.zenframework.z8.pde.Plugin;
import org.zenframework.z8.pde.editor.Z8Editor;
import org.zenframework.z8.pde.refactoring.ExceptionHandler;
import org.zenframework.z8.pde.refactoring.RefactoringExecutionStarter;
import org.zenframework.z8.pde.refactoring.messages.RefactoringMessages;

public class RenameLanguageElementAction extends SelectionDispatchAction {
    private Z8Editor m_editor;

    public RenameLanguageElementAction(IWorkbenchSite site) {
        super(site);
    }

    public RenameLanguageElementAction(Z8Editor editor) {
        this(editor.getEditorSite());
        m_editor = editor;
        setEnabled(SelectionConverter.canOperateOn(m_editor));
    }

    @Override
    public void selectionChanged(IStructuredSelection selection) {
        try {
            if(selection.size() == 1) {
                setEnabled(canEnable(selection));
                return;
            }
        }
        catch(CoreException e) {
            Plugin.log(e);
        }
        setEnabled(false);
    }

    private static boolean canEnable(IStructuredSelection selection) throws CoreException {
        ILanguageElement element = getLanguageElement(selection);

        if(element == null)
            return false;

        return isRenameAvailable(element);
    }

    private static ILanguageElement getLanguageElement(IStructuredSelection selection) {
        if(selection.size() != 1)
            return null;

        Object first = selection.getFirstElement();

        if(first instanceof IResource) {
            IResource r = (IResource)first;
            return Workspace.getInstance().getResource(r);
        }

        if(!(first instanceof ILanguageElement))
            return null;

        return (ILanguageElement)first;
    }

    @Override
    public void run(IStructuredSelection selection) {
        ILanguageElement element = getLanguageElement(selection);

        if(element == null)
            return;

        try {
            run(element);
        }
        catch(CoreException e) {
            ExceptionHandler.handle(e, RefactoringMessages.RenameElementAction_name,
                    RefactoringMessages.RenameElementAction_exception);
        }
    }

    @Override
    public void selectionChanged(ITextSelection selection) {
        /*		if(selection instanceof JavaTextSelection)
        		{
        			try
        			{
        				IJavaElement[] elements = ((JavaTextSelection)selection).resolveElementAtOffset();
        				if(elements.length == 1)
        				{
        					setEnabled(isRenameAvailable(elements[0]));
        				}
        				else
        				{
        					setEnabled(false);
        				}
        			}
        			catch(CoreException e)
        			{
        				setEnabled(false);
        			}
        		}
        		else
        		{*/
        setEnabled(true);
        /*}*/
    }

    @Override
    public void run(ITextSelection selection) {
        try {
            ILanguageElement element = getLanguageElement();

            if(element != null && isRenameAvailable(element)) {
                run(element);
                return;
            }
        }
        catch(CoreException e) {
            ExceptionHandler.handle(e, RefactoringMessages.RenameElementAction_name,
                    RefactoringMessages.RenameElementAction_exception);
        }

        MessageDialog.openInformation(getShell(), RefactoringMessages.RenameElementAction_name,
                RefactoringMessages.RenameElementAction_not_available);
    }

    public boolean canRun() {
        try {
            ILanguageElement element = getLanguageElement();

            if(element == null)
                return false;

            return isRenameAvailable(element);
        }
        catch(CoreException e) {
            Plugin.log(e);
        }
        return false;
    }

    private ILanguageElement getLanguageElement() {
        return null;
    }

    private void run(ILanguageElement element) throws CoreException {
        RefactoringExecutionStarter.startRenameRefactoring(element, getShell());
    }

    private static boolean isRenameAvailable(ILanguageElement element) throws CoreException {
        /*		switch(element.getElementType())
        		{
        		case IJavaElement.JAVA_PROJECT:
        			return RefactoringAvailabilityTester.isRenameAvailable((IJavaProject)element);
        		case IJavaElement.PACKAGE_FRAGMENT_ROOT:
        			return RefactoringAvailabilityTester.isRenameAvailable((IPackageFragmentRoot)element);
        		case IJavaElement.PACKAGE_FRAGMENT:
        			return RefactoringAvailabilityTester.isRenameAvailable((IPackageFragment)element);
        		case IJavaElement.COMPILATION_UNIT:
        			return RefactoringAvailabilityTester.isRenameAvailable((ICompilationUnit)element);
        		case IJavaElement.TYPE:
        			return RefactoringAvailabilityTester.isRenameAvailable((IType)element);
        		case IJavaElement.METHOD:
        			final IMethod method = (IMethod)element;
        			if(method.isConstructor())
        				return RefactoringAvailabilityTester.isRenameAvailable(method.getDeclaringType());
        			else
        				return RefactoringAvailabilityTester.isRenameAvailable(method);
        		case IJavaElement.FIELD:
        			final IField field = (IField)element;
        			if(Flags.isEnum(field.getFlags()))
        				return RefactoringAvailabilityTester.isRenameEnumConstAvailable(field);
        			else
        				return RefactoringAvailabilityTester.isRenameFieldAvailable(field);
        		case IJavaElement.TYPE_PARAMETER:
        			return RefactoringAvailabilityTester.isRenameAvailable((ITypeParameter)element);
        		case IJavaElement.LOCAL_VARIABLE:
        			return RefactoringAvailabilityTester.isRenameAvailable((ILocalVariable)element);
        		}
        		return false;
        */
        return true;
    }
}
