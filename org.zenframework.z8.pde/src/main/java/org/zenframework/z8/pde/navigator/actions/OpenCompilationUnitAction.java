package org.zenframework.z8.pde.navigator.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import org.zenframework.z8.compiler.workspace.CompilationUnit;
import org.zenframework.z8.pde.Plugin;

public class OpenCompilationUnitAction extends Action {
	private CompilationUnit m_cunit;

	public OpenCompilationUnitAction(CompilationUnit file) {
		m_cunit = file;
	}

	@Override
	public void run() {
		try {
			IEditorDescriptor ed = PlatformUI.getWorkbench().getEditorRegistry().findEditor("org.zenframework.z8.forms.editors.MultiEditor");
			if(ed != null) {
				IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), (IFile)m_cunit.getResource(), "org.zenframework.z8.forms.editors.MultiEditor");
			} else {
				IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), (IFile)m_cunit.getResource(), "org.zenframework.z8.pde.Z8Editor");
			}
		} catch(Exception e) {
			Plugin.log(e);
		}
	}

	@Override
	public String getText() {
		return "�������";
	}
}
