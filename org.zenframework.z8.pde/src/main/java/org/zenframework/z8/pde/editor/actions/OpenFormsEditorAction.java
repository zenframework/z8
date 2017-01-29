package org.zenframework.z8.pde.editor.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;

import org.zenframework.z8.compiler.workspace.CompilationUnit;
import org.zenframework.z8.compiler.workspace.Workspace;
import org.zenframework.z8.pde.Z8EditorMessages;
import org.zenframework.z8.pde.Plugin;

public class OpenFormsEditorAction extends TextEditorAction {
	public OpenFormsEditorAction(String prefix, ITextEditor editor) {
		super(Z8EditorMessages.getResourceBundle(), prefix, editor);
	}

	@Override
	public void run() {
		IResource resource = ((FileEditorInput)getTextEditor().getEditorInput()).getFile();
		CompilationUnit compilationUnit = Workspace.getInstance().getCompilationUnit(resource);
		// getTextEditor().close(true);
		open(compilationUnit);
	}

	public static void open(CompilationUnit unit) {
		FileEditorInput input = new FileEditorInput((IFile)unit.getResource());
		try {
			IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), input, "org.zenframework.z8.forms.editors.MultiEditor");
		} catch(Exception e) {
			Plugin.log(e);
		}
	}
}
