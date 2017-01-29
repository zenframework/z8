package org.zenframework.z8.pde.editor;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.ui.texteditor.AbstractRulerActionDelegate;
import org.eclipse.ui.texteditor.ITextEditor;

import org.zenframework.z8.pde.Z8ResourceBundle;

public class RulerClickActionDelegate extends AbstractRulerActionDelegate {

	@Override
	protected IAction createAction(ITextEditor editor, IVerticalRulerInfo rulerInfo) {
		return new RulerClickAction(new Z8ResourceBundle(), "SelectAnnotationRulerAction.", (Z8Editor)editor, rulerInfo);
	}

}
