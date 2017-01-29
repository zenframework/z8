package org.zenframework.z8.pde.navigator.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.ide.IDE;

import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.core.ISource;
import org.zenframework.z8.pde.MyMultiEditor;
import org.zenframework.z8.pde.Plugin;
import org.zenframework.z8.pde.editor.Z8Editor;

public class OpenSourceAction extends Action {
	private ISource m_source;

	public OpenSourceAction(ISource source) {
		this.m_source = source;
	}

	@Override
	public String getText() {
		return "������� � ������";
	}

	@Override
	public void run() {
		IPosition pos = m_source.getPosition();
		IFile file = (IFile)m_source.getCompilationUnit().getResource();

		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

		try {
			page.getWorkbenchWindow().getWorkbench().showPerspective("Z8Perspective", page.getWorkbenchWindow());

			IEditorDescriptor ed = PlatformUI.getWorkbench().getEditorRegistry().findEditor("org.zenframework.z8.forms.editors.MultiEditor");
			if(ed != null) {
				IEditorPart epart = IDE.openEditor(page, file, "org.zenframework.z8.forms.editors.MultiEditor");
				MyMultiEditor multi = (MyMultiEditor)epart;
				multi.setActivePage(0);
				multi.getEditorSite().getSelectionProvider().setSelection(new TextSelection(pos.getOffset(), pos.getLength()));
			} else {
				IEditorPart ep = IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), (IFile)file, "org.zenframework.z8.pde.Z8Editor");
				Z8Editor editor = (Z8Editor)ep;
				editor.getSelectionProvider().setSelection(new TextSelection(pos.getOffset(), pos.getLength()));
			}
		} catch(WorkbenchException e) {
			Plugin.log(e);
		}
	}
}
