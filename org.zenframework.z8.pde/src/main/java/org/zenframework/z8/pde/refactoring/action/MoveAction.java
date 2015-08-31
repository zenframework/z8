package org.zenframework.z8.pde.refactoring.action;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.IWorkbenchSite;

import org.zenframework.z8.pde.editor.Z8Editor;
import org.zenframework.z8.pde.refactoring.messages.RefactoringMessages;

public class MoveAction extends SelectionDispatchAction {
    private ReorgMoveAction m_reorgMoveAction;

    public MoveAction(IWorkbenchSite site) {
        super(site);
        setText(RefactoringMessages.MoveAction_text);
        m_reorgMoveAction = new ReorgMoveAction(site);
    }

    public MoveAction(Z8Editor editor) {
        super(editor.getEditorSite());
        setText(RefactoringMessages.MoveAction_text);
        m_reorgMoveAction = new ReorgMoveAction(editor.getEditorSite());
    }

    @Override
    public void selectionChanged(SelectionChangedEvent event) {
        m_reorgMoveAction.selectionChanged(event);
        setEnabled(computeEnableState());
    }

    @Override
    public void run(IStructuredSelection selection) {
        if(m_reorgMoveAction.isEnabled())
            m_reorgMoveAction.run();
    }

    @Override
    public void update(ISelection selection) {
        m_reorgMoveAction.update(selection);
        setEnabled(computeEnableState());
    }

    private boolean computeEnableState() {
        return m_reorgMoveAction.isEnabled();
    }
}
