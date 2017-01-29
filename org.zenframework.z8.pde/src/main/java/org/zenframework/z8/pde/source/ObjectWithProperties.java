package org.zenframework.z8.pde.source;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class ObjectWithProperties implements IObjectWithProperties {
	@Override
	public void addPropertyChangeListener(PropertyChangeListener l) {
		if(l == null) {
			throw new IllegalArgumentException();
		}
		pcs.addPropertyChangeListener(l);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(listener);
	}

	protected void firePropertyChange(String property, Object oldValue, Object newValue) {
		if(oldValue != newValue || newValue == null)
			if(pcs.hasListeners(property)) {
				pcs.firePropertyChange(property, oldValue, newValue);
			}
	}

	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

}
