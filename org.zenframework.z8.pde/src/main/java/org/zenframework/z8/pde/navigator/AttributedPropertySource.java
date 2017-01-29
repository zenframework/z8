package org.zenframework.z8.pde.navigator;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;

import org.zenframework.z8.compiler.core.IAttribute;
import org.zenframework.z8.compiler.core.IAttributed;

public class AttributedPropertySource implements IPropertySource {

	private IAttributed m_attributed;

	public AttributedPropertySource(IAttributed attributed) {
		m_attributed = attributed;
	}

	@Override
	public Object getEditableValue() {
		return m_attributed;
	}

	@Override
	public IPropertyDescriptor[] getPropertyDescriptors() {
		IAttribute[] atts = m_attributed.getAttributes();
		int size = atts.length;
		IPropertyDescriptor[] descs = new IPropertyDescriptor[size];
		for(int i = 0; i < size; i++)
			descs[i] = new AttributePropertyDescriptor(atts[i]);
		return descs;
	}

	@Override
	public Object getPropertyValue(Object id) {
		return m_attributed.getAttribute((String)id).getValueString();
	}

	@Override
	public boolean isPropertySet(Object id) {
		return false;
	}

	@Override
	public void resetPropertyValue(Object id) {
	}

	@Override
	public void setPropertyValue(Object id, Object value) {
	}

}
