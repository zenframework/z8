package org.zenframework.z8.pde.refactoring;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.workspace.CompilationUnit;
import org.zenframework.z8.compiler.workspace.Resource;
import org.zenframework.z8.pde.refactoring.reorg.ReorgPolicyFactory;

public final class RefactoringAvailabilityTester {
    public static boolean isRenameAvailable(CompilationUnit compilationUnit) {
        if(compilationUnit == null)
            return false;

        if(!compilationUnit.getResource().exists())
            return false;

        IType type = compilationUnit.getReconciledType();

        if(type != null && !type.isPrimary())
            return false;

        return true;
    }

    public static boolean isRenameAvailable(Resource resource) {
        if(resource == null)
            return false;

        return isRenameAvailable(resource.getResource());
    }

    public static boolean isRenameAvailable(IResource resource) {
        if(resource == null)
            return false;

        if(!resource.exists())
            return false;

        if(!resource.isAccessible())
            return false;

        return true;
    }

    public static boolean isRenameAvailable(IType type) {
        if(type == null) {
            return false;
        }

        return isRenameAvailable(type.getCompilationUnit().getResource());
    }

    public static boolean isMoveAvailable(final IResource[] resources, final ILanguageElement[] elements)
            throws CoreException {
        if(elements != null) {
            for(int index = 0; index < elements.length; index++) {
                ILanguageElement element = elements[index];

                if(element == null)
                    return false;
            }
        }
        return ReorgPolicyFactory.createMovePolicy(resources, elements).canEnable();
    }

    private RefactoringAvailabilityTester() {}
}