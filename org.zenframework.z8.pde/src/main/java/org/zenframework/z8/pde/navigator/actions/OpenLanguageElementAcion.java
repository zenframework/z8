package org.zenframework.z8.pde.navigator.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchSite;

import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.core.ISource;

public class OpenLanguageElementAcion extends Action {
	private IWorkbenchSite site;

	public OpenLanguageElementAcion(IWorkbenchSite site) {
		super("Open");
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
		if(ss.getFirstElement() instanceof ILanguageElement) {
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
		if(ss.getFirstElement() instanceof ILanguageElement) {
			new OpenSourceAction((ISource)ss.getFirstElement()).run();
		}
	}
}
