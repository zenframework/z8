package org.zenframework.z8.pde.editor.view;

import java.util.Iterator;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;

public class AnnotationHover implements IAnnotationHover {
	@Override
	public String getHoverInfo(ISourceViewer sourceViewer, int line) {
		IDocument document = sourceViewer.getDocument();
		IAnnotationModel model = sourceViewer.getAnnotationModel();

		Iterator<Annotation> iterator = model.getAnnotationIterator();

		while(iterator.hasNext()) {
			Annotation annotation = iterator.next();

			Position position = model.getPosition(annotation);
			if(position == null)
				continue;

			if(!isRulerLine(position, document, line))
				continue;

			return annotation.getText();
		}

		return null;
	}

	private boolean isRulerLine(Position position, IDocument document, int line) {
		if(position.getOffset() > -1 && position.getLength() > -1) {
			try {
				return line == document.getLineOfOffset(position.getOffset());
			} catch(BadLocationException x) {
			}
		}
		return false;
	}
}
