package org.zenframework.z8.pde;

import org.eclipse.ui.views.properties.PropertySheetEntry;

import org.zenframework.z8.compiler.workspace.Resource;
import org.zenframework.z8.compiler.workspace.ResourceListener;

public class MyPropertySheetEnrty extends PropertySheetEntry implements ResourceListener {

	@Override
	public void event(int type, Resource resource, Object object) {
		refreshFromRoot();
	}

}
