package org.zenframework.z8.pde.editor.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;

import org.zenframework.z8.compiler.workspace.CompilationUnit;
import org.zenframework.z8.compiler.workspace.Workspace;
import org.zenframework.z8.pde.Z8EditorMessages;
import org.zenframework.z8.pde.Plugin;

public class OpenDataSchemaAction extends TextEditorAction {
	public OpenDataSchemaAction(String prefix, ITextEditor editor) {
		super(Z8EditorMessages.getResourceBundle(), prefix, editor);
	}

	@Override
	public void run() {
		IResource resource = ((FileEditorInput)getTextEditor().getEditorInput()).getFile();
		CompilationUnit compilationUnit = Workspace.getInstance().getCompilationUnit(resource);
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		try {
			IDE.openEditor(page, (IFile)compilationUnit.getResource(), "org.zenframework.z8.dataschema.editor");
			page.getWorkbenchWindow().getWorkbench().showPerspective("DataSchemaPerspective", page.getWorkbenchWindow());
		} catch(Exception e) {
			Plugin.log(e);
		}
	}
}
