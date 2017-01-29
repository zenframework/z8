package org.zenframework.z8.pde.refactoring;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;

import org.zenframework.z8.pde.Plugin;
import org.zenframework.z8.pde.PluginImages;

public class LanguageElementImageDescriptor extends CompositeImageDescriptor {
	public final static int FINAL = 0x002;

	public final static int STATIC = 0x008;

	public final static int WARNING = 0x020;
	public final static int ERROR = 0x040;
	public final static int IMPLEMENTS = 0x100;
	public final static int OVERRIDES = 0x200;

	public final static int DEPRECATED = 0x400;

	private ImageDescriptor fBaseImage;
	private int fFlags;
	private Point fSize;

	public LanguageElementImageDescriptor(ImageDescriptor baseImage, int flags, Point size) {
		fBaseImage = baseImage;
		fFlags = flags;
		fSize = size;
	}

	public void setAdornments(int adornments) {
		fFlags = adornments;
	}

	public int getAdronments() {
		return fFlags;
	}

	public void setImageSize(Point size) {
		fSize = size;
	}

	public Point getImageSize() {
		return new Point(fSize.x, fSize.y);
	}

	@Override
	protected Point getSize() {
		return fSize;
	}

	@Override
	public boolean equals(Object object) {
		if(object == null || !LanguageElementImageDescriptor.class.equals(object.getClass()))
			return false;

		LanguageElementImageDescriptor other = (LanguageElementImageDescriptor)object;
		return (fBaseImage.equals(other.fBaseImage) && fFlags == other.fFlags && fSize.equals(other.fSize));
	}

	@Override
	public int hashCode() {
		return fBaseImage.hashCode() | fFlags | fSize.hashCode();
	}

	@Override
	protected void drawCompositeImage(int width, int height) {
		ImageData bg = getImageData(fBaseImage);
		drawImage(bg, 0, 0);
		drawTopRight();
		drawBottomRight();
		drawBottomLeft();
	}

	private ImageData getImageData(ImageDescriptor descriptor) {
		ImageData data = descriptor.getImageData();

		if(data == null) {
			data = DEFAULT_IMAGE_DATA;
			Plugin.log(IStatus.ERROR, "Image data not available: " + descriptor.toString(), 0, null);
		}
		return data;
	}

	private void drawTopRight() {
		int x = getSize().x;

		if((fFlags & FINAL) != 0) {
			ImageData data = getImageData(PluginImages.DESC_OVR_FINAL);
			x -= data.width;
			drawImage(data, x, 0);
		}

		if((fFlags & STATIC) != 0) {
			ImageData data = getImageData(PluginImages.DESC_OVR_STATIC);
			x -= data.width;
			drawImage(data, x, 0);
		}
	}

	private void drawBottomRight() {
		Point size = getSize();

		int x = size.x;
		int flags = fFlags;

		if((flags & IMPLEMENTS) != 0) {
			ImageData data = getImageData(PluginImages.DESC_OVR_IMPLEMENTS);
			x -= data.width;
			drawImage(data, x, size.y - data.height);
		}
		if((flags & OVERRIDES) != 0) {
			ImageData data = getImageData(PluginImages.DESC_OVR_OVERRIDES);
			x -= data.width;
			drawImage(data, x, size.y - data.height);
		}
	}

	private void drawBottomLeft() {
		Point size = getSize();
		int x = 0;
		if((fFlags & ERROR) != 0) {
			ImageData data = getImageData(PluginImages.DESC_OVR_ERROR);
			drawImage(data, x, size.y - data.height);
			x += data.width;
		}
		if((fFlags & WARNING) != 0) {
			ImageData data = getImageData(PluginImages.DESC_OVR_WARNING);
			drawImage(data, x, size.y - data.height);
			x += data.width;
		}
	}
}
