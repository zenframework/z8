package org.zenframework.z8.pde.editor.actions;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;

import org.zenframework.z8.compiler.content.Hyperlink;
import org.zenframework.z8.compiler.content.TypeHyperlink;
import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.workspace.CompilationUnit;
import org.zenframework.z8.compiler.workspace.Workspace;
import org.zenframework.z8.pde.Z8EditorMessages;
import org.zenframework.z8.pde.editor.view.FindEntryPointPathsHyperlink;

public class FindEntryPointPaths extends TextEditorAction {
	public FindEntryPointPaths(String prefix, ITextEditor editor) {
		super(Z8EditorMessages.getResourceBundle(), prefix, editor);
	}

	@Override
	public void run() {
		IResource resource = ((FileEditorInput)getTextEditor().getEditorInput()).getFile();
		CompilationUnit compilationUnit = Workspace.getInstance().getCompilationUnit(resource);

		IPosition position = compilationUnit.getHyperlinkPosition(((TextSelection)getTextEditor().getSelectionProvider().getSelection()).getOffset());
		Hyperlink hyperlink = compilationUnit.getHyperlink(position);

		if(hyperlink == null) {
			notifyResult(false);
			return;
		}

		if(!(hyperlink instanceof TypeHyperlink)) {
			notifyResult(false);
			return;
		}

		TypeHyperlink typeHyperlink = (TypeHyperlink)hyperlink;
		FindEntryPointPathsHyperlink dsh = new FindEntryPointPathsHyperlink(position, typeHyperlink);
		dsh.open();
	}
}
