package org.zenframework.z8.pde.editor.view;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.ide.IDE;

import org.zenframework.z8.compiler.content.LabelEntry;
import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.pde.Plugin;

public class ResourceHyperlink implements IHyperlink {
	private IPosition m_position;
	private String m_key;
	private LabelEntry m_entry;

	public ResourceHyperlink(IPosition position, String key, LabelEntry entry) {
		m_position = position;
		m_key = key;
		m_entry = entry;
	}

	@Override
	public IRegion getHyperlinkRegion() {
		return new Region(m_position.getOffset(), m_position.getLength());
	}

	@Override
	public String getHyperlinkText() {
		return m_key;
	}

	@Override
	public String getTypeLabel() {
		return null;
	}

	@Override
	public void open() {
		try {
			TextEditor editor = (TextEditor)IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), (IFile)m_entry.getNLSUnit().getResource());
			IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
			IRegion region = new FindReplaceDocumentAdapter(document).find(0, "\"" + m_key + "\"", true, true, false, false);
			if(region != null)
				editor.getSelectionProvider().setSelection(new TextSelection(region.getOffset(), region.getLength()));
		} catch(PartInitException e) {
			Plugin.log(e);
		} catch(BadLocationException e) {
			Plugin.log(e);
		}
	}
}
