package org.zenframework.z8.pde.refactoring.action;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.IWorkbenchSite;

import org.zenframework.z8.pde.editor.Z8Editor;
import org.zenframework.z8.pde.refactoring.messages.RefactoringMessages;

public class RenameAction extends SelectionDispatchAction {
    private RenameLanguageElementAction m_renameLanguageElementAction;
    private RenameResourceAction m_renameResourceAction;

    public RenameAction(IWorkbenchSite site) {
        super(site);
        setText(RefactoringMessages.RenameAction_text);
        m_renameLanguageElementAction = new RenameLanguageElementAction(site);
        m_renameLanguageElementAction.setText(getText());
        m_renameResourceAction = new RenameResourceAction(site);
        m_renameResourceAction.setText(getText());
    }

    public RenameAction(Z8Editor editor) {
        this(editor.getEditorSite());
        m_renameLanguageElementAction = new RenameLanguageElementAction(editor);
    }

    @Override
    public void selectionChanged(SelectionChangedEvent event) {
        m_renameLanguageElementAction.selectionChanged(event);
        if(m_renameResourceAction != null)
            m_renameResourceAction.selectionChanged(event);
        setEnabled(computeEnabledState());
    }

    @Override
    public void update(ISelection selection) {
        m_renameLanguageElementAction.update(selection);
        if(m_renameResourceAction != null)
            m_renameResourceAction.update(selection);
        setEnabled(computeEnabledState());
    }

    private boolean computeEnabledState() {
        if(m_renameResourceAction != null) {
            return m_renameLanguageElementAction.isEnabled() || m_renameResourceAction.isEnabled();
        }
        else {
            return m_renameLanguageElementAction.isEnabled();
        }
    }

    @Override
    public void run(IStructuredSelection selection) {
        if(m_renameLanguageElementAction.isEnabled())
            m_renameLanguageElementAction.run(selection);
        if(m_renameResourceAction != null && m_renameResourceAction.isEnabled())
            m_renameResourceAction.run(selection);
    }

    @Override
    public void run(ITextSelection selection) {
        if(m_renameLanguageElementAction.canRun())
            m_renameLanguageElementAction.run(selection);
        else
            MessageDialog.openInformation(getShell(), RefactoringMessages.RenameAction_rename,
                    RefactoringMessages.RenameAction_unavailable);
    }
}
