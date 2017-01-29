package org.zenframework.z8.pde.editor.view;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import org.zenframework.z8.compiler.content.Hyperlink;
import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.pde.MyMultiEditor;
import org.zenframework.z8.pde.Plugin;
import org.zenframework.z8.pde.editor.Z8Editor;

public class EditorHyperlink implements IHyperlink {

	private IPosition m_position;
	private Hyperlink m_hyperlink;

	public EditorHyperlink(IPosition position, Hyperlink hyperlink) {
		m_hyperlink = hyperlink;
		m_position = position;
	}

	@Override
	public IRegion getHyperlinkRegion() {
		return new Region(m_position.getOffset(), m_position.getLength());
	}

	@Override
	public String getTypeLabel() {
		return null;
	}

	@Override
	public String getHyperlinkText() {
		return "Open Declaration Hyperlink";
	}

	@Override
	public void open() {
		IFile file = (IFile)m_hyperlink.getCompilationUnit().getResource();

		assert (file != null);

		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

		IPosition pos = m_hyperlink.getPosition();

		try {
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
		} catch(Exception e) {
			Plugin.log(e);
		}
	}
}
