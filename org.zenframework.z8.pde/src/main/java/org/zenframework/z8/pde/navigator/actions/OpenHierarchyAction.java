package org.zenframework.z8.pde.navigator.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.PlatformUI;

import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.pde.Plugin;
import org.zenframework.z8.pde.navigator.HierarchyNavigator;

public class OpenHierarchyAction extends Action {

	private IType m_type;

	public OpenHierarchyAction(IType type) {
		m_type = type;
	}

	@Override
	public String getText() {
		return "Открыть иерархию";
	}

	@Override
	public String getToolTipText() {
		return "Открыть в иерархии типов";
	}

	@Override
	public void run() {
		try {
			HierarchyNavigator navigator = (HierarchyNavigator)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("org.zenframework.views.hierarchy");
			navigator.setInput(m_type);
		} catch(Exception e) {
			Plugin.log(e);
		}
	}

}
