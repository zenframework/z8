package org.zenframework.z8.pde.editor;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.texteditor.IAnnotationImageProvider;

import org.zenframework.z8.pde.PluginImages;

public class OverrideAnnotationImageProvider implements IAnnotationImageProvider {

	public OverrideAnnotationImageProvider() {
	}

	private final static ImageDescriptor OVER = PluginImages.DESC_OVR_OVERRIDES;
	private final static ImageDescriptor IMPL = PluginImages.DESC_OVR_IMPLEMENTS;

	private final static String ID_OVER = "override";
	private final static String ID_IMPL = "implement";

	@Override
	public ImageDescriptor getImageDescriptor(String imageDescritporId) {
		return imageDescritporId.equals(ID_OVER) ? OVER : IMPL;
	}

	@Override
	public String getImageDescriptorId(Annotation annotation) {
		return ((OverrideAnnotation)annotation).isOverride() ? ID_OVER : ID_IMPL;
	}

	@Override
	public Image getManagedImage(Annotation annotation) {
		return null;
	}

}
