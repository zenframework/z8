package org.zenframework.z8.pde.refactoring;

import java.util.Map;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringContribution;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;

public abstract class Z8RefactoringContribution extends RefactoringContribution {
    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public RefactoringDescriptor createDescriptor(String id, String project, String description, String comment,
            Map arguments, int flags) {
        return new Z8RefactoringDescriptor(this, id, project, description, comment, arguments, flags);
    }

    public abstract Refactoring createRefactoring(RefactoringDescriptor descriptor) throws CoreException;

    @Override
    @SuppressWarnings("rawtypes")
    public Map retrieveArgumentMap(RefactoringDescriptor descriptor) {
        if(descriptor instanceof Z8RefactoringDescriptor) {
            return ((Z8RefactoringDescriptor)descriptor).getArguments();
        }
        return super.retrieveArgumentMap(descriptor);
    }
}