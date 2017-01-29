package org.zenframework.z8.pde.editor;

import java.util.Iterator;
import java.util.ResourceBundle;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationAccessExtension;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel;
import org.eclipse.ui.texteditor.SelectMarkerRulerAction;

public class RulerClickAction extends SelectMarkerRulerAction {

	private Annotation fAnnotation;

	public RulerClickAction(ResourceBundle bundle, String prefix, Z8Editor editor, IVerticalRulerInfo ruler) {
		super(bundle, prefix, editor, ruler);
	}

	@Override
	public void run() {
		runWithEvent(null);
	}

	@Override
	public void runWithEvent(Event event) {
		if(fAnnotation instanceof OverrideAnnotation) {
			OverrideAnnotation over = (OverrideAnnotation)fAnnotation;
			over.open();
			return;
		}
		super.run();
	}

	@Override
	public void update() {
		findAnnotation();
		setEnabled(true); // super.update() might change this later
		if(fAnnotation instanceof OverrideAnnotation) {
			return;
		}
		super.update();
	}

	private void findAnnotation() {
		fAnnotation = null;

		AbstractMarkerAnnotationModel model = getAnnotationModel();
		IAnnotationAccessExtension annotationAccess = getAnnotationAccessExtension();

		IDocument document = getDocument();
		if(model == null)
			return;

		Iterator<Annotation> iter = model.getAnnotationIterator();
		int layer = Integer.MIN_VALUE;

		while(iter.hasNext()) {
			Annotation annotation = iter.next();
			if(annotation.isMarkedDeleted())
				continue;

			int annotationLayer = layer;
			if(annotationAccess != null) {
				annotationLayer = annotationAccess.getLayer(annotation);
				if(annotationLayer < layer)
					continue;
			}

			Position position = model.getPosition(annotation);
			if(!includesRulerLine(position, document))
				continue;

			if(annotation instanceof OverrideAnnotation) {
				fAnnotation = annotation;
				break;
			}
		}
	}

}
