package org.zenframework.z8.pde.editor.actions;

import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;

import org.zenframework.z8.pde.Z8EditorMessages;

/**
 * A toolbar action which toggles the presentation model of the connected text
 * editor. The editor shows either the highlight range only or always the whole
 * document.
 */

public class PresentationAction extends TextEditorAction {
	/**
	 * Constructs and updates the action.
	 */
	public PresentationAction() {
		super(Z8EditorMessages.getResourceBundle(), "TogglePresentation.", null);
		update();
	}

	@Override
	public void run() {
		ITextEditor editor = getTextEditor();
		editor.resetHighlightRange();
		boolean show = editor.showsHighlightRangeOnly();
		setChecked(!show);
		editor.showHighlightRangeOnly(!show);
	}

	@Override
	public void update() {
		setChecked(getTextEditor() != null && getTextEditor().showsHighlightRangeOnly());
		setEnabled(true);
	}
}
