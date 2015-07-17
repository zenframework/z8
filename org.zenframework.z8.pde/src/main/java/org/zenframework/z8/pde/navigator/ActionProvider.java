package org.zenframework.z8.pde.navigator;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.MoveResourceAction;
import org.eclipse.ui.actions.RenameResourceAction;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionConstants;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonMenuConstants;
import org.eclipse.ui.navigator.ICommonViewerSite;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;

import org.zenframework.z8.pde.navigator.actions.OpenHierarchyViewAction;
import org.zenframework.z8.pde.navigator.actions.OpenLanguageElementAcion;
import org.zenframework.z8.pde.navigator.actions.OrganizeImportsAction;
import org.zenframework.z8.pde.refactoring.action.MoveAction;
import org.zenframework.z8.pde.refactoring.action.RenameAction;

public class ActionProvider extends CommonActionProvider {

    private MoveAction moveAction;
    private RenameAction renameAction;
    private OpenLanguageElementAcion openAction;
    private OrganizeImportsAction orgImpAction;
    private OpenHierarchyViewAction hierAction;

    private IMenuListener listener = new IMenuListener() {
        @Override
        public void menuAboutToShow(IMenuManager manager) {
            manager.remove(MoveResourceAction.ID);
            manager.remove(RenameResourceAction.ID);
        }
    };
    private boolean firstTime = true;

    @Override
    public void init(ICommonActionExtensionSite aSite) {
        super.init(aSite);

        ICommonViewerSite vSite = aSite.getViewSite();
        if(vSite instanceof ICommonViewerWorkbenchSite) {
            ICommonViewerWorkbenchSite wSite = (ICommonViewerWorkbenchSite)vSite;

            moveAction = new MoveAction(wSite.getSite());
            renameAction = new RenameAction(wSite.getSite());
            openAction = new OpenLanguageElementAcion(wSite.getSite());
            orgImpAction = new OrganizeImportsAction(wSite.getSite());
            hierAction = new OpenHierarchyViewAction(wSite.getSite());
        }
    }

    @Override
    public void fillActionBars(IActionBars ab) {
        if(moveAction != null)
            if(moveAction.isEnabled())
                ab.setGlobalActionHandler(/*ActionFactory.MOVE.getId()*/MoveResourceAction.ID, moveAction);

        if(renameAction != null)
            if(renameAction.isEnabled())
                ab.setGlobalActionHandler(/*ActionFactory.RENAME.getId()*/RenameResourceAction.ID, renameAction);
        if(openAction != null)
            if(openAction.isEnabled())
                ab.setGlobalActionHandler(ICommonActionConstants.OPEN, openAction);
        //if (orgImpAction!=null) if (orgImpAction.isEnabled())

    }

    @Override
    public void fillContextMenu(IMenuManager mgr) {
        mgr.addMenuListener(listener);
        if(firstTime) {
            mgr.setVisible(true);
            firstTime = false;
        }
        if(moveAction != null)
            if(moveAction.isEnabled()) {
                mgr.appendToGroup(ICommonMenuConstants.GROUP_REORGANIZE, moveAction);
            }
        if(renameAction != null)
            if(renameAction.isEnabled()) {
                mgr.appendToGroup(ICommonMenuConstants.GROUP_REORGANIZE, renameAction);
            }
        if(openAction != null)
            if(openAction.isEnabled()) {
                mgr.appendToGroup(ICommonMenuConstants.GROUP_OPEN, openAction);
            }
        if(orgImpAction != null)
            if(orgImpAction.isEnabled())
                mgr.appendToGroup(ICommonMenuConstants.GROUP_SOURCE, orgImpAction);
        if(hierAction != null)
            if(hierAction.isEnabled())
                mgr.appendToGroup(ICommonMenuConstants.GROUP_SOURCE, hierAction);
    }
}
