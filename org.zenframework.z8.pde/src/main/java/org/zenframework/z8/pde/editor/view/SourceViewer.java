package org.zenframework.z8.pde.editor.view;

import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.widgets.Composite;

import org.zenframework.z8.pde.editor.Z8Editor;

public class SourceViewer extends ProjectionViewer {
	private Z8Editor m_editor;

	public SourceViewer(Z8Editor editor, Composite parent, IVerticalRuler ruler, IOverviewRuler overviewRuler, boolean showsAnnotationOverview, int styles) {
		super(parent, ruler, overviewRuler, showsAnnotationOverview, styles);
		m_editor = editor;
	}

	public Z8Editor getEditor() {
		return m_editor;
	}

}
