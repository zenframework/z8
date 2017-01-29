package org.zenframework.z8.pde.debug.launch;

import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.ISourceLookupParticipant;

public class SourceLookupDirector extends AbstractSourceLookupDirector {
	@Override
	public void initializeParticipants() {
		addParticipants(new ISourceLookupParticipant[] { new SourceLookupParticipant() });
	}
}
