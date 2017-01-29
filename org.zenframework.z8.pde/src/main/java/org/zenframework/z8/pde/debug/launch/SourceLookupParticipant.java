package org.zenframework.z8.pde.debug.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupParticipant;

import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.pde.debug.model.JDXStackFrame;

public class SourceLookupParticipant extends AbstractSourceLookupParticipant {
	@Override
	public String getSourceName(Object object) throws CoreException {
		if(object instanceof JDXStackFrame) {
			IType type = ((JDXStackFrame)object).getType();

			if(type != null) {
				return type.getCompilationUnit().getName();
			}
		}

		return null;
	}
}
