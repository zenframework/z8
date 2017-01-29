package org.zenframework.z8.pde.navigator.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.actions.ActionGroup;

import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.pde.navigator.HierarchyNavigator;

public class HierarchyNavigatorActionGroup extends ActionGroup {

	private IAction swSuper;

	private HierarchyNavigator m_navigator;

	public HierarchyNavigatorActionGroup(HierarchyNavigator provider) {
		this.m_navigator = provider;
	}

	@Override
	public void fillActionBars(IActionBars actionBars) {
		swSuper = new SwitchHierarchySuper(m_navigator);
		swSuper.setChecked(m_navigator.isShowSuper());
		actionBars.getToolBarManager().add(swSuper);
		actionBars.getMenuManager().add(swSuper);
	}

	@Override
	public void fillContextMenu(IMenuManager menu) {
		IStructuredSelection selection = (IStructuredSelection)getContext().getSelection();
		Object element = selection.getFirstElement();
		if(element != null) {
			IType type = (IType)element;
			IAction openAction = new OpenSourceAction(type);
			menu.add(openAction);
			IAction hAction = new OpenHierarchyAction(type);
			if(hAction != null)
				menu.add(hAction);
		}
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

}
