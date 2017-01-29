package org.zenframework.z8.pde;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.MultiPageEditorPart;

public abstract class MyMultiEditor extends MultiPageEditorPart {

	@Override
	public void setActivePage(int pageIndex) {
		super.setActivePage(pageIndex);
	}

	@Override
	public IEditorPart getEditor(int pageIndex) {
		return super.getEditor(pageIndex);
	}

	@Override
	public int getActivePage() {
		return super.getActivePage();
	}
}
