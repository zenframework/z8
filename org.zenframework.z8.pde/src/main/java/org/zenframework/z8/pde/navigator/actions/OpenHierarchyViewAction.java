package org.zenframework.z8.pde.navigator.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.PlatformUI;

import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.pde.Plugin;
import org.zenframework.z8.pde.navigator.HierarchyNavigator;

public class OpenHierarchyViewAction extends Action {
	private IWorkbenchSite site;

	public OpenHierarchyViewAction(IWorkbenchSite site) {
		super("Открыть иерархию типа");
		this.site = site;
	}

	@Override
	public boolean isEnabled() {
		ISelection s = site.getSelectionProvider().getSelection();
		IStructuredSelection ss = (IStructuredSelection)s;
		if(ss.isEmpty())
			return false;
		if(ss.size() > 1)
			return false;
		if(ss.getFirstElement() instanceof IType) {
			return true;
		}

		return false;
	}

	@Override
	public void run() {
		ISelection s = site.getSelectionProvider().getSelection();
		IStructuredSelection ss = (IStructuredSelection)s;
		if(ss.isEmpty())
			return;
		if(ss.size() > 1)
			return;
		if(ss.getFirstElement() instanceof IType) {
			IType t = (IType)ss.getFirstElement();
			try {
				HierarchyNavigator navigator = (HierarchyNavigator)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("org.zenframework.views.hierarchy");
				navigator.setInput(t);
			} catch(Exception e) {
				Plugin.log(e);
			}
		}
	}

}
