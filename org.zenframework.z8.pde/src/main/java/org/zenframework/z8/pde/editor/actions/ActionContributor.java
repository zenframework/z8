package org.zenframework.z8.pde.editor.actions;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.editors.text.TextEditorActionContributor;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.RetargetTextEditorAction;

import org.zenframework.z8.pde.Z8EditorMessages;
import org.zenframework.z8.pde.editor.IEditorActionDefinitionIds;

public class ActionContributor extends TextEditorActionContributor {
	protected RetargetTextEditorAction fContentAssistProposal;
	protected RetargetTextEditorAction fContentAssistTip;
	protected RetargetTextEditorAction fOpenDeclaration;
	// protected RetargetTextEditorAction fOpenTablesEditor;
	protected RetargetTextEditorAction fFindEntryPointPaths;
	protected RetargetTextEditorAction fOpenTextHierarchy;
	protected RetargetTextEditorAction fOrganizeImports;

	// protected RetargetTextEditorAction fOpenFormsEditor;
	// protected TextEditorAction fTogglePresentation;

	public ActionContributor() {
		super();
		fContentAssistProposal = new RetargetTextEditorAction(Z8EditorMessages.getResourceBundle(), "ContentAssistProposal."); //$NON-NLS-1$
		fContentAssistProposal.setActionDefinitionId(IEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
		fContentAssistTip = new RetargetTextEditorAction(Z8EditorMessages.getResourceBundle(), "ContentAssistTip."); //$NON-NLS-1$
		fContentAssistTip.setActionDefinitionId(IEditorActionDefinitionIds.CONTENT_ASSIST_CONTEXT_INFORMATION);
		fOpenDeclaration = new RetargetTextEditorAction(Z8EditorMessages.getResourceBundle(), "OpenDeclaration."); //$NON-NLS-1$
		fOpenDeclaration.setActionDefinitionId(IEditorActionDefinitionIds.OPEN_DECLARATION);
		fFindEntryPointPaths = new RetargetTextEditorAction(Z8EditorMessages.getResourceBundle(), "FindEntryPointPaths."); //$NON-NLS-1$
		fFindEntryPointPaths.setActionDefinitionId(IEditorActionDefinitionIds.FIND_ENTRYPOINT_PATHS);
		fOpenTextHierarchy = new RetargetTextEditorAction(Z8EditorMessages.getResourceBundle(), "OpenTextHierarchy.");
		fOpenTextHierarchy.setActionDefinitionId(IEditorActionDefinitionIds.OPEN_TEXT_HIERARCHY);
		fOrganizeImports = new RetargetTextEditorAction(Z8EditorMessages.getResourceBundle(), "OrganizeImports.");
		fOrganizeImports.setActionDefinitionId(IEditorActionDefinitionIds.ORGANIZE_IMPORTS);
	}

	@Override
	public void init(IActionBars bars) {
		super.init(bars);

		IMenuManager menuManager = bars.getMenuManager();
		IMenuManager editMenu = menuManager.findMenuUsingPath(IWorkbenchActionConstants.M_EDIT);
		if(editMenu != null) {
			editMenu.add(new Separator());
			editMenu.add(fContentAssistProposal);
			editMenu.add(fContentAssistTip);
			editMenu.add(fOrganizeImports);
		}
		IMenuManager navMenu = menuManager.findMenuUsingPath(IWorkbenchActionConstants.M_NAVIGATE);
		if(navMenu != null) {
			navMenu.add(new Separator());
			navMenu.add(fOpenDeclaration);
			navMenu.add(fFindEntryPointPaths);
			navMenu.add(fOpenTextHierarchy);
		}
	}

	private void doSetActiveEditor(IEditorPart part) {
		super.setActiveEditor(part);
		ITextEditor editor = null;
		if(part instanceof ITextEditor)
			editor = (ITextEditor)part;
		fContentAssistProposal.setAction(getAction(editor, "ContentAssistProposal")); //$NON-NLS-1$
		fContentAssistTip.setAction(getAction(editor, "ContentAssistTip")); //$NON-NLS-1$
		fOpenDeclaration.setAction(getAction(editor, "OpenDeclaration")); //$NON-NLS-1$
		fFindEntryPointPaths.setAction(getAction(editor, "FindEntryPointPaths"));
		fOpenTextHierarchy.setAction(getAction(editor, "OpenTextHierarchy"));
		fOrganizeImports.setAction(getAction(editor, "OrganizeImports"));
	}

	@Override
	public void setActiveEditor(IEditorPart part) {
		super.setActiveEditor(part);
		doSetActiveEditor(part);
	}

	@Override
	public void dispose() {
		doSetActiveEditor(null);
		super.dispose();
	}
}
