package org.zenframework.z8.pde.refactoring.changes;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.ChangeDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringChangeDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;

public final class DynamicValidationRefactoringChange extends DynamicValidationStateChange {
    private final RefactoringDescriptor m_descriptor;

    public DynamicValidationRefactoringChange(RefactoringDescriptor descriptor, String name) {
        super(name);
        m_descriptor = descriptor;
    }

    public DynamicValidationRefactoringChange(RefactoringDescriptor descriptor, String name, Change[] changes) {
        super(name, changes);
        m_descriptor = descriptor;
    }

    @Override
    public ChangeDescriptor getDescriptor() {
        return new RefactoringChangeDescriptor(m_descriptor);
    }
}
