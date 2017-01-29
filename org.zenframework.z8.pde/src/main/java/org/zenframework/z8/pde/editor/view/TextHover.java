package org.zenframework.z8.pde.editor.view;

import java.util.Iterator;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;

public class TextHover implements ITextHover {
	@Override
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		IAnnotationModel model = ((ISourceViewer)textViewer).getAnnotationModel();

		Iterator<Annotation> iterator = model.getAnnotationIterator();

		while(iterator.hasNext()) {
			Annotation annotation = (Annotation)iterator.next();

			if(!annotation.getType().equals("org.zenframework.z8.pde.error")) {
				continue;
			}

			Position position = model.getPosition(annotation);

			if(position.overlapsWith(hoverRegion.getOffset(), hoverRegion.getLength())) {
				return annotation.getText();
			}
		}
		return null;
	}

	@Override
	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
		return findWord(textViewer.getDocument(), offset);
	}

	private IRegion findWord(IDocument document, int offset) {
		int start = -1;
		int end = -1;

		try {

			int pos = offset;
			char c;

			while(pos >= 0) {
				c = document.getChar(pos);
				if(!Character.isJavaIdentifierPart(c))
					break;
				--pos;
			}

			start = pos;

			pos = offset;
			int length = document.getLength();

			while(pos < length) {
				c = document.getChar(pos);
				if(!Character.isJavaIdentifierPart(c))
					break;
				++pos;
			}

			end = pos;

		} catch(BadLocationException x) {
		}

		if(start > -1 && end > -1) {
			if(start == offset && end == offset)
				return new Region(offset, 0);
			else if(start == offset)
				return new Region(start, end - start);
			else
				return new Region(start + 1, end - start - 1);
		}

		return null;
	}

}
