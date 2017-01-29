package org.zenframework.z8.pde.navigator.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import org.zenframework.z8.compiler.core.IMember;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.pde.PluginImages;
import org.zenframework.z8.pde.navigator.NavigatorMessages;
import org.zenframework.z8.pde.navigator.NavigatorPreferences;

public class SwitchFilterBase extends Action {

	private StructuredViewer m_viewer;

	private ViewerFilter m_filter = new NativeTypeMembersFilter();

	class NativeTypeMembersFilter extends ViewerFilter {
		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			if(element instanceof IMember) {
				IType type = null;

				if(parentElement instanceof IType) {
					type = (IType)parentElement;
				}

				if(parentElement instanceof IMember) {
					type = ((IMember)parentElement).getDeclaringType();
				}

				if(type != null) {
					IMember member = (IMember)element;
					return member.getDeclaringType() == type;
				}
			}

			return true;
		}
	};

	public SwitchFilterBase(StructuredViewer viewer) {
		m_viewer = viewer;

		if(hasFilter()) {
			m_viewer.addFilter(m_filter);
		}

		setChecked(hasFilter());
	}

	private boolean hasFilter() {
		return NavigatorPreferences.getFilterBaseTypeMembers();
	}

	@Override
	public void run() {
		if(hasFilter()) {
			m_viewer.removeFilter(m_filter);
		} else {
			m_viewer.addFilter(m_filter);
		}

		NavigatorPreferences.setFilterBaseTypeMembers(!hasFilter());
	}

	@Override
	public String getToolTipText() {
		return NavigatorMessages.ShowBaseTypeMembers_tooltip;
	}

	@Override
	public int getStyle() {
		return AS_CHECK_BOX;
	}

	@Override
	public String getText() {
		return NavigatorMessages.ShowBaseTypeMembers;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return PluginImages.DESC_OUTLINE_BASE;
	}

}