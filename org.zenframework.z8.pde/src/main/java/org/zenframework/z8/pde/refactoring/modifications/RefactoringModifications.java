package org.zenframework.z8.pde.refactoring.modifications;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;
import org.eclipse.ltk.core.refactoring.participants.ValidateEditChecker;

import org.zenframework.z8.compiler.workspace.CompilationUnit;
import org.zenframework.z8.compiler.workspace.Folder;

public abstract class RefactoringModifications {
    private ResourceModifications fResourceModifications;

    public RefactoringModifications() {
        fResourceModifications = new ResourceModifications();
    }

    public ResourceModifications getResourceModifications() {
        return fResourceModifications;
    }

    public abstract RefactoringParticipant[] loadParticipants(RefactoringStatus status, RefactoringProcessor owner,
            String[] natures, SharableParticipants shared);

    public void buildValidateEdits(ValidateEditChecker checker) {}

    protected void createIncludingParents(IContainer container) {
        while(container != null && !(container.exists() || getResourceModifications().willExist(container))) {
            getResourceModifications().addCreate(container);
            container = container.getParent();
        }
    }

    protected IResource[] collectResourcesOfInterest(Folder source) throws CoreException {
        CompilationUnit[] children = source.getCompilationUnits();

        List<IResource> result = new ArrayList<IResource>(children.length);

        for(CompilationUnit compilationUnit : children) {
            result.add(compilationUnit.getResource());
        }

        return result.toArray(new IResource[result.size()]);
    }
}
