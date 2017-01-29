package org.zenframework.z8.pde.navigator;

import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.IPropertySourceProvider;

import org.zenframework.z8.compiler.core.IAttributed;

public class AttributedPropertySourceProvider implements IPropertySourceProvider {

	@Override
	public IPropertySource getPropertySource(Object object) {
		if(object instanceof IAttributed) {
			IAttributed attributed = (IAttributed)object;
			return new AttributedPropertySource(attributed);
		}
		if(object instanceof IAttributedProvider) {
			IAttributedProvider prov = (IAttributedProvider)object;
			return new AttributedPropertySource(prov.getAttributed());
		}
		return null;

	}

}
