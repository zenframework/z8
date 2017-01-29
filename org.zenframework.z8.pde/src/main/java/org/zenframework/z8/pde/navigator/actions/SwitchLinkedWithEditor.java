package org.zenframework.z8.pde.navigator.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

import org.zenframework.z8.pde.PluginImages;
import org.zenframework.z8.pde.navigator.ClassesNavigator;

public class SwitchLinkedWithEditor extends Action {
	private ClassesNavigator navigator;

	public SwitchLinkedWithEditor(ClassesNavigator p) {
		this.navigator = p;
	}

	@Override
	public void run() {
		navigator.setLinkedWithEditor(!navigator.isLinkedWithEditor());
	}

	@Override
	public String getToolTipText() {
		return ("������� � ����������");
	}

	@Override
	public int getStyle() {
		return AS_CHECK_BOX;
	}

	@Override
	public String getText() {
		return "����� � ����������";
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return PluginImages.DESC_OUTLINE_SYNCHRONIZED;
	}
}
