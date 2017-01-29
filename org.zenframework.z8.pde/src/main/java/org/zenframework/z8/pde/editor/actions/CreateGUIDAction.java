package org.zenframework.z8.pde.editor.actions;

import java.util.UUID;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;

import org.zenframework.z8.pde.Z8EditorMessages;
import org.zenframework.z8.pde.Plugin;
import org.zenframework.z8.pde.editor.Z8Editor;

public class CreateGUIDAction extends TextEditorAction {
	public CreateGUIDAction(String prefix, ITextEditor editor) {
		super(Z8EditorMessages.getResourceBundle(), prefix, editor);
	}

	@Override
	public void run() {
		UUID guid = UUID.randomUUID();
		TextSelection sel = (TextSelection)getTextEditor().getSelectionProvider().getSelection();
		IDocument doc = ((Z8Editor)getTextEditor()).getDocument();
		try {
			doc.replace(sel.getOffset(), sel.getLength(), "'" + guid.toString().toUpperCase() + "'");
		} catch(Exception e) {
			Plugin.log(e);
		}
	}
}
