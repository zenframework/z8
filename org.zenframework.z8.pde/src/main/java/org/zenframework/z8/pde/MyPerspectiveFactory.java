package org.zenframework.z8.pde;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.console.IConsoleConstants;

public class MyPerspectiveFactory implements IPerspectiveFactory {
	@Override
	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();

		IFolderLayout left = layout.createFolder("left", IPageLayout.LEFT, (float)0.18, editorArea);
		left.addView("org.eclipse.ui.navigator.ProjectExplorer");

		IFolderLayout right = layout.createFolder("right", IPageLayout.RIGHT, (float)0.85, editorArea);
		right.addView(IPageLayout.ID_OUTLINE);

		IFolderLayout bottom = layout.createFolder("bottom", IPageLayout.BOTTOM, (float)0.75, editorArea); //$NON-NLS-1$
		bottom.addView(IPageLayout.ID_PROBLEM_VIEW);
		bottom.addView(IConsoleConstants.ID_CONSOLE_VIEW);
		bottom.addView(IPageLayout.ID_PROP_SHEET);

		defineActions(layout);

	}

	// registering new items in the NewWizard and ShowView menus
	@SuppressWarnings("deprecation")
	public void defineActions(IPageLayout layout) {
		// Add "new wizards".
		layout.addNewWizardShortcut("Z8ProjectWizard");
		layout.addNewWizardShortcut("Z8ClassWizard");
		layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.folder");
		layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.file");
		// Add "show views".
		layout.addShowViewShortcut(IPageLayout.ID_RES_NAV);
		layout.addShowViewShortcut(IPageLayout.ID_BOOKMARKS);
		layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
		layout.addShowViewShortcut(IPageLayout.ID_PROP_SHEET);
		layout.addShowViewShortcut(IPageLayout.ID_TASK_LIST);
		layout.addShowViewShortcut("org.zenframework.views.hierarchy");
		layout.addShowViewShortcut("org.eclipse.ui.navigator.ProjectExplorer");

		layout.addActionSet(IPageLayout.ID_NAVIGATE_ACTION_SET);
		layout.addActionSet("org.eclipse.debug.ui.launchActionSet");
	}
}
