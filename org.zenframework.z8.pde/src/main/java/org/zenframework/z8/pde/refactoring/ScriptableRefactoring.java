package org.zenframework.z8.pde.refactoring;

import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import org.zenframework.z8.pde.refactoring.messages.LanguageElementLabels;
import org.zenframework.z8.pde.refactoring.messages.Messages;
import org.zenframework.z8.pde.refactoring.messages.RefactoringMessages;
import org.zenframework.z8.pde.refactoring.rename.ICommentProvider;

public abstract class ScriptableRefactoring extends Refactoring implements IScriptableRefactoring, ICommentProvider {
    private String m_comment;

    public static RefactoringStatus createInputFatalStatus(final Object element, final String name, final String id) {
        if(element != null) {
            return RefactoringStatus.createFatalErrorStatus(Messages.format(
                    RefactoringMessages.InitializableRefactoring_input_not_exists,
                    new String[] { LanguageElementLabels.getTextLabel(element, LanguageElementLabels.ALL_FULLY_QUALIFIED),
                            name, id }));
        }
        else {
            return RefactoringStatus.createFatalErrorStatus(Messages.format(
                    RefactoringMessages.InitializableRefactoring_inputs_do_not_exist, new String[] { name, id }));
        }
    }

    public static RefactoringStatus createInputWarningStatus(final Object element, final String name, final String id) {
        if(element != null) {
            return RefactoringStatus.createWarningStatus(Messages.format(
                    RefactoringMessages.InitializableRefactoring_input_not_exists,
                    new String[] { LanguageElementLabels.getTextLabel(element, LanguageElementLabels.ALL_FULLY_QUALIFIED),
                            name, id }));
        }
        else {
            return RefactoringStatus.createWarningStatus(Messages.format(
                    RefactoringMessages.InitializableRefactoring_inputs_do_not_exist, new String[] { name, id }));
        }
    }

    @Override
    public boolean canEnableComment() {
        return true;
    }

    public final RefactoringStatus createInputFatalStatus(final Object element, final String id) {
        return createInputFatalStatus(element, getName(), id);
    }

    public final RefactoringStatus createInputWarningStatus(final Object element, final String id) {
        return createInputWarningStatus(element, getName(), id);
    }

    @Override
    public String getComment() {
        return m_comment;
    }

    @Override
    public void setComment(String comment) {
        m_comment = comment;
    }
}