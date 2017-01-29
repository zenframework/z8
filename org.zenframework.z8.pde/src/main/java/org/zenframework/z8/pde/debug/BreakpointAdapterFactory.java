package org.zenframework.z8.pde.debug;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.ui.actions.IRunToLineTarget;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;

public class BreakpointAdapterFactory implements IAdapterFactory {
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if(adapterType == IToggleBreakpointsTarget.class) {
			return new ToggleBreakpointAdapter();
		}
		return null;
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Class[] getAdapterList() {
		return new Class[] { IRunToLineTarget.class, IToggleBreakpointsTarget.class };
	}
}
