package org.zenframework.z8.pde.refactoring.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchSite;

public abstract class SelectionDispatchAction extends Action implements ISelectionChangedListener {
    private IWorkbenchSite m_site;
    private ISelectionProvider m_specialSelectionProvider;

    protected SelectionDispatchAction(IWorkbenchSite site) {
        m_site = site;
    }

    public IWorkbenchSite getSite() {
        return m_site;
    }

    public ISelection getSelection() {
        ISelectionProvider selectionProvider = getSelectionProvider();

        if(selectionProvider != null) {
            return selectionProvider.getSelection();
        }

        return null;
    }

    public Shell getShell() {
        return m_site.getShell();
    }

    public ISelectionProvider getSelectionProvider() {
        if(m_specialSelectionProvider != null) {
            return m_specialSelectionProvider;
        }
        return m_site.getSelectionProvider();
    }

    public void setSpecialSelectionProvider(ISelectionProvider provider) {
        m_specialSelectionProvider = provider;
    }

    public void update(ISelection selection) {
        dispatchSelectionChanged(selection);
    }

    public void selectionChanged(IStructuredSelection selection) {
        selectionChanged((ISelection)selection);
    }

    public void run(IStructuredSelection selection) {
        run((ISelection)selection);
    }

    public void selectionChanged(ITextSelection selection) {
        selectionChanged((ISelection)selection);
    }

    public void run(ITextSelection selection) {
        run((ISelection)selection);
    }

    public void selectionChanged(ISelection selection) {
        setEnabled(false);
    }

    public void run(ISelection selection) {}

    @Override
    public void run() {
        dispatchRun(getSelection());
    }

    @Override
    public void selectionChanged(SelectionChangedEvent event) {
        dispatchSelectionChanged(event.getSelection());
    }

    private void dispatchSelectionChanged(ISelection selection) {
        if(selection instanceof IStructuredSelection) {
            selectionChanged((IStructuredSelection)selection);
        }
        else if(selection instanceof ITextSelection) {
            selectionChanged((ITextSelection)selection);
        }
        else {
            selectionChanged(selection);
        }
    }

    private void dispatchRun(ISelection selection) {
        if(selection instanceof IStructuredSelection) {
            run((IStructuredSelection)selection);
        }
        else if(selection instanceof ITextSelection) {
            run((ITextSelection)selection);
        }
        else {
            run(selection);
        }
    }
}
