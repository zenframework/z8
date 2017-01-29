package org.zenframework.z8.pde.source;

import java.beans.PropertyChangeListener;

public interface IObjectWithProperties {
	public void addPropertyChangeListener(PropertyChangeListener l);

	public void removePropertyChangeListener(PropertyChangeListener listener);
}
