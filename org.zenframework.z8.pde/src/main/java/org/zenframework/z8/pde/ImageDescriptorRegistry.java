package org.zenframework.z8.pde;

import java.util.HashMap;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public class ImageDescriptorRegistry {
	private HashMap<ImageDescriptor, Image> fRegistry = new HashMap<ImageDescriptor, Image>(10);
	private Display fDisplay;

	public static Display getStandardDisplay() {
		Display display = Display.getCurrent();

		if(display == null)
			display = Display.getDefault();

		return display;
	}

	public ImageDescriptorRegistry() {
		this(getStandardDisplay());
	}

	public ImageDescriptorRegistry(Display display) {
		fDisplay = display;
		hookDisplay();
	}

	public Image get(ImageDescriptor descriptor) {
		if(descriptor == null)
			descriptor = ImageDescriptor.getMissingImageDescriptor();

		Image result = (Image)fRegistry.get(descriptor);

		if(result != null)
			return result;

		result = descriptor.createImage();

		if(result != null)
			fRegistry.put(descriptor, result);

		return result;
	}

	public void dispose() {
		for(Image image : fRegistry.values()) {
			image.dispose();
		}
		fRegistry.clear();
	}

	private void hookDisplay() {
		fDisplay.disposeExec(new Runnable() {
			@Override
			public void run() {
				dispose();
			}
		});
	}
}
