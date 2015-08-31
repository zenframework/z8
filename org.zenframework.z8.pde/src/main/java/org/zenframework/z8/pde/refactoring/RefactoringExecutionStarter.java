package org.zenframework.z8.pde.refactoring;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.swt.widgets.Shell;

import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.workspace.CompilationUnit;
import org.zenframework.z8.compiler.workspace.Folder;
import org.zenframework.z8.pde.refactoring.messages.RefactoringMessages;
import org.zenframework.z8.pde.refactoring.move.Z8MoveRefactoring;
import org.zenframework.z8.pde.refactoring.processors.move.Z8MoveProcessor;
import org.zenframework.z8.pde.refactoring.processors.rename.RenameResourceProcessor;
import org.zenframework.z8.pde.refactoring.rename.Z8RenameRefactoring;
import org.zenframework.z8.pde.refactoring.rename.RenameSupport;
import org.zenframework.z8.pde.refactoring.rename.RenameUserInterfaceManager;
import org.zenframework.z8.pde.refactoring.reorg.CreateTargetQueries;
import org.zenframework.z8.pde.refactoring.reorg.IReorgPolicy.IMovePolicy;
import org.zenframework.z8.pde.refactoring.reorg.ReorgMoveWizard;
import org.zenframework.z8.pde.refactoring.reorg.ReorgPolicyFactory;
import org.zenframework.z8.pde.refactoring.reorg.ReorgQueries;

public final class RefactoringExecutionStarter {
    private static RenameSupport createRenameSupport(ILanguageElement element, String newName, int flags)
            throws CoreException {
        if(element instanceof IType) {
            return RenameSupport.create((IType)element, newName, flags);
        }
        else if(element instanceof CompilationUnit) {
            return RenameSupport.create((CompilationUnit)element, newName, flags);
        }
        else if(element instanceof Folder) {
            return RenameSupport.create((Folder)element, newName, flags);
        }
        /*		else if(element instanceof Project)
        		{
        			return RenameSupport.create((Project)element, newName, flags);
        		}
        */

        /*		switch(element.getElementType())
        		{
        		case IJavaElement.JAVA_PROJECT:
        			return RenameSupport.create((IJavaProject)element, newName, flags);
        		case IJavaElement.PACKAGE_FRAGMENT_ROOT:
        			return RenameSupport.create((IPackageFragmentRoot)element, newName);
        		case IJavaElement.PACKAGE_FRAGMENT:
        			return RenameSupport.create((IPackageFragment)element, newName, flags);
        		case IJavaElement.COMPILATION_UNIT:
        			return RenameSupport.create((ICompilationUnit)element, newName, flags);
        		case IJavaElement.TYPE:
        			return RenameSupport.create((IType)element, newName, flags);
        		case IJavaElement.METHOD:
        			final IMethod method = (IMethod)element;
        			if(method.isConstructor())
        				return createRenameSupport(method.getDeclaringType(), newName, flags);
        			else
        				return RenameSupport.create((IMethod)element, newName, flags);
        		case IJavaElement.FIELD:
        			return RenameSupport.create((IField)element, newName, flags);
        		case IJavaElement.TYPE_PARAMETER:
        			return RenameSupport.create((ITypeParameter)element, newName, flags);
        		case IJavaElement.LOCAL_VARIABLE:
        			return RenameSupport.create((ILocalVariable)element, newName, flags);
        		}
        */
        return null;
    }

    public static void startRenameRefactoring(ILanguageElement element, Shell shell) throws CoreException {
        final RenameSupport support = createRenameSupport(element, null, RenameSupport.UPDATE_REFERENCES);

        if(support != null && support.preCheck().isOK())
            support.openDialog(shell);
    }

    public static void startRenameResourceRefactoring(IResource resource, Shell shell) throws CoreException {
        Z8RenameRefactoring refactoring = new Z8RenameRefactoring(new RenameResourceProcessor(resource));
        RenameUserInterfaceManager.getDefault().getStarter(refactoring).activate(refactoring, shell, true);
    }

    public static void startMoveRefactoring(final IResource[] resources, final ILanguageElement[] elements, final Shell shell)
            throws CoreException {
        IMovePolicy policy = ReorgPolicyFactory.createMovePolicy(resources, elements);

        if(policy.canEnable()) {
            Z8MoveProcessor processor = new Z8MoveProcessor(policy);
            Z8MoveRefactoring refactoring = new Z8MoveRefactoring(processor);
            final RefactoringWizard wizard = new ReorgMoveWizard(refactoring);
            processor.setCreateTargetQueries(new CreateTargetQueries(wizard));
            processor.setReorgQueries(new ReorgQueries(wizard));
            new RefactoringStarter().activate(refactoring, wizard, shell,
                    RefactoringMessages.OpenRefactoringWizardAction_refactoring, true);
        }
    }

    private RefactoringExecutionStarter() {}
}
