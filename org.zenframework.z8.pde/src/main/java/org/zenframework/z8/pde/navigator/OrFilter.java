package org.zenframework.z8.pde.navigator;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

public class OrFilter extends ViewerFilter {

	private ViewerFilter m_filter1, m_filter2;

	public OrFilter(ViewerFilter f1, ViewerFilter f2) {
		m_filter1 = f1;
		m_filter2 = f2;
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		return m_filter1.select(viewer, parentElement, element) || m_filter2.select(viewer, parentElement, element);
	}
}
