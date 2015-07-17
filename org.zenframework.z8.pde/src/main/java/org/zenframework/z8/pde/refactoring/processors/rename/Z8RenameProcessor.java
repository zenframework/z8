package org.zenframework.z8.pde.refactoring.processors.rename;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.mapping.IResourceChangeDescriptionFactory;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.RenameProcessor;
import org.eclipse.ltk.core.refactoring.participants.ResourceChangeChecker;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;
import org.eclipse.ltk.core.refactoring.participants.ValidateEditChecker;

import org.zenframework.z8.pde.refactoring.IScriptableRefactoring;
import org.zenframework.z8.pde.refactoring.modifications.RenameModifications;
import org.zenframework.z8.pde.refactoring.rename.ICommentProvider;
import org.zenframework.z8.pde.refactoring.rename.INameUpdating;

public abstract class Z8RenameProcessor extends RenameProcessor implements IScriptableRefactoring, INameUpdating,
        ICommentProvider {
    private String m_newElementName;
    private String m_comment;
    private RenameModifications m_renameModifications;

    @Override
    public final RefactoringParticipant[] loadParticipants(RefactoringStatus status, SharableParticipants shared)
            throws CoreException {
        return getRenameModifications().loadParticipants(status, this, getAffectedProjectNatures(), shared);
    }

    @Override
    public final RefactoringStatus checkFinalConditions(IProgressMonitor pm, CheckConditionsContext context)
            throws CoreException, OperationCanceledException {
        ResourceChangeChecker checker = (ResourceChangeChecker)context.getChecker(ResourceChangeChecker.class);

        IResourceChangeDescriptionFactory deltaFactory = checker.getDeltaFactory();

        RefactoringStatus result = doCheckFinalConditions(pm, context);

        if(result.hasFatalError())
            return result;

        IFile[] changed = getChangedFiles();

        for(int i = 0; i < changed.length; i++) {
            deltaFactory.change(changed[i]);
        }

        RenameModifications renameModifications = getRenameModifications();
        renameModifications.buildDelta(deltaFactory);
        renameModifications.buildValidateEdits((ValidateEditChecker)context.getChecker(ValidateEditChecker.class));

        return result;
    }

    private RenameModifications getRenameModifications() throws CoreException {
        if(m_renameModifications == null) {
            m_renameModifications = computeRenameModifications();
        }
        return m_renameModifications;
    }

    protected abstract RenameModifications computeRenameModifications() throws CoreException;

    protected abstract RefactoringStatus doCheckFinalConditions(IProgressMonitor pm, CheckConditionsContext context)
            throws CoreException, OperationCanceledException;

    protected abstract IFile[] getChangedFiles() throws CoreException;

    protected abstract String[] getAffectedProjectNatures() throws CoreException;

    @Override
    public void setNewElementName(String newName) {
        Assert.isNotNull(newName);
        m_newElementName = newName;
    }

    @Override
    public String getNewElementName() {
        return m_newElementName;
    }

    public boolean needsSavedEditors() {
        return true;
    }

    @Override
    public final boolean canEnableComment() {
        return true;
    }

    @Override
    public final String getComment() {
        return m_comment;
    }

    @Override
    public final void setComment(String comment) {
        m_comment = comment;
    }
}
