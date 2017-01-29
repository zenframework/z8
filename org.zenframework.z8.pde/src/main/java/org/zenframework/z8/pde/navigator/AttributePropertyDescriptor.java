package org.zenframework.z8.pde.navigator;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.IPropertyDescriptor;

import org.zenframework.z8.compiler.core.IAttribute;

public class AttributePropertyDescriptor implements IPropertyDescriptor {

	private IAttribute m_attribute;

	public AttributePropertyDescriptor(IAttribute attribute) {
		m_attribute = attribute;
	}

	@Override
	public CellEditor createPropertyEditor(Composite parent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCategory() {
		return null;
	}

	@Override
	public String getDescription() {
		return m_attribute.getName();
	}

	@Override
	public String getDisplayName() {
		return m_attribute.getName();
	}

	@Override
	public String[] getFilterFlags() {
		return null;
	}

	@Override
	public Object getHelpContextIds() {
		return null;
	}

	@Override
	public Object getId() {
		return m_attribute.getName();
	}

	@Override
	public ILabelProvider getLabelProvider() {
		return null;
	}

	@Override
	public boolean isCompatibleWith(IPropertyDescriptor anotherProperty) {
		return false;
	}

}
