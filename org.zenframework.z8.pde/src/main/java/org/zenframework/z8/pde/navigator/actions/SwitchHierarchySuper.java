package org.zenframework.z8.pde.navigator.actions;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import org.zenframework.z8.pde.Plugin;
import org.zenframework.z8.pde.navigator.HierarchyNavigator;

public class SwitchHierarchySuper extends Action {

	private static ImageDescriptor imageDescriptor;

	private static Image image;

	static {
		try {
			image = new Image(null, FileLocator.openStream(Plugin.getDefault().getBundle(), new Path("icons/hierarchy.bmp"), true));
			imageDescriptor = ImageDescriptor.createFromImage(image);
		} catch(Exception e) {
		}
	}

	private HierarchyNavigator m_navigator;

	public SwitchHierarchySuper(HierarchyNavigator p) {
		this.m_navigator = p;
	}

	@Override
	public void run() {
		m_navigator.setShowSuper(!m_navigator.isShowSuper());
	}

	@Override
	public String getToolTipText() {
		return ("�������� ������ ��������");
	}

	@Override
	public int getStyle() {
		return AS_CHECK_BOX;
	}

	@Override
	public String getText() {
		return "������ ��������";
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return imageDescriptor;
	}

}
