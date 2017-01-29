package org.zenframework.z8.pde.navigator.actions;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.graphics.Image;

import org.zenframework.z8.pde.Plugin;

public class SwitchFilterSuper extends Action {

	private static ImageDescriptor imageDescriptor;

	private static Image image;

	private boolean added = Plugin.getDefault().getPreferenceStore().getBoolean(PREFERENCE_STRING);

	private final static ViewerFilter FILTER = new ViewerFilter() {

		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			/*
			 * if (element instanceof TreeMember) { TreeMember tm = (TreeMember)
			 * element; if (tm.getInit()==null && !tm.isChildInited()){ IType
			 * parentType=null; if (tm.getTreeParent() instanceof IType) {
			 * parentType = (IType) tm.getTreeParent(); } else if
			 * (((TreeMember)tm.getTreeParent()).getInit() instanceof
			 * MemberNestedType){ parentType =
			 * (IType)((TreeMember)tm.getTreeParent()).getInit(); } if
			 * (parentType!=null) return
			 * tm.getMember().getCompilationUnit().equals(parentType.
			 * getCompilationUnit()); } }
			 */
			return true;
		}
	};

	private static final String PREFERENCE_STRING = "Navigator.FilterSuper";

	static {
		try {
			image = new Image(null, FileLocator.openStream(Plugin.getDefault().getBundle(), new Path("icons/filter_super.bmp"), true));
			imageDescriptor = ImageDescriptor.createFromImage(image);
		} catch(Exception e) {
		}
	}

	private StructuredViewer viewer;

	public SwitchFilterSuper(StructuredViewer v) {
		viewer = v;
		if(added)
			v.addFilter(FILTER);
		setChecked(!added);
	}

	@Override
	public void run() {
		if(added)
			viewer.removeFilter(FILTER);
		else
			viewer.addFilter(FILTER);
		added = !added;
		Plugin.getDefault().getPreferenceStore().setValue(PREFERENCE_STRING, added);
	}

	@Override
	public String getToolTipText() {
		return ("���������� �������� �� ������������ �������");
	}

	@Override
	public int getStyle() {
		return AS_CHECK_BOX;
	}

	@Override
	public String getText() {
		return "���������� �� �������";
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return imageDescriptor;
	}

}
