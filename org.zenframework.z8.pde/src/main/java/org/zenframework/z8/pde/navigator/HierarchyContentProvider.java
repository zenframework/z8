package org.zenframework.z8.pde.navigator;

import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import org.zenframework.z8.compiler.core.IType;

public class HierarchyContentProvider implements ITreeContentProvider {

	private Map<IType, List<IType>> m_map;

	public HierarchyContentProvider(Map<IType, List<IType>> map) {
		m_map = map;
	}

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if(parentElement instanceof String)
			return new Object[0];
		List<IType> types = m_map.get((IType)parentElement);
		if(types == null)
			return new Object[0];
		return types.toArray(new IType[types.size()]);
	}

	@Override
	public Object getParent(Object element) {
		if(element instanceof IType) {
			IType type = (IType)element;
			if(type.getBaseType() != null)
				return type.getBaseType();
			else
				return new IType[] { type };
		}
		return null;

	}

	@Override
	public boolean hasChildren(Object element) {
		if(element instanceof String)
			return false;
		return m_map.get((IType)element) != null;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if(inputElement instanceof String) {
			String str = (String)inputElement;
			str = str + "...";
			return new Object[] { str };
		} else if(inputElement instanceof IType[]) {
			IType[] types = (IType[])inputElement;
			return types;
		}
		return null;
	}

}
