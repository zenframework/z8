package org.zenframework.z8.pde.navigator;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.Image;

import org.zenframework.z8.compiler.core.IType;

public class ClassTypeImageProvider {
	private final static ClassTypeImageProvider INSTANCE = new ClassTypeImageProvider();

	private static Map<String, Image> m_images = new HashMap<String, Image>(4);

	private Image get(IType type) {
		while(type != null && !type.isNative()) {
			type = type.getBaseType();
		}

		if(type != null) {
			String typeName = type.getUserName();

			Image image = m_images.get(typeName);

			if(image == null) {
				try {
					image = Z8LabelProvider.loadImage("icons/classes/" + typeName + ".bmp");
					m_images.put(typeName, image);
				} catch(Exception e) {
					image = null;
				}
			}

			return image;
		}

		return null;
	}

	public static Image getImage(IType type) {
		return INSTANCE.get(type);
	}
}
