package org.zenframework.z8.pde.build;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.error.BuildError;
import org.zenframework.z8.compiler.error.BuildMessage;
import org.zenframework.z8.compiler.error.DefaultBuildMessageConsumer;
import org.zenframework.z8.compiler.workspace.Resource;
import org.zenframework.z8.pde.Plugin;

public class BuildMessageConsumer extends DefaultBuildMessageConsumer {
	public BuildMessageConsumer() {
	}

	@Override
	public void clearMessages(Resource resource) {
		try {
			resource.getResource().deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_ZERO);
		} catch(CoreException e) {
			Plugin.info(e);
		}
	}

	@Override
	public void consume(BuildMessage message) {
		super.consume(message);

		try {
			IResource resource = message.getResource();

			IMarker marker = resource.createMarker(IMarker.PROBLEM);

			if(marker.exists()) {
				marker.setAttribute(IMarker.MESSAGE, message.getDescription());
				marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
				marker.setAttribute(IMarker.SEVERITY, message instanceof BuildError ? IMarker.SEVERITY_ERROR : IMarker.SEVERITY_WARNING);

				IPosition position = message.getPosition();

				if(position != null) {
					marker.setAttribute(IMarker.LINE_NUMBER, position.getLine() + 1);
					marker.setAttribute(IMarker.CHAR_START, position.getOffset());
					marker.setAttribute(IMarker.CHAR_END, position.getOffset() + position.getLength());
				}
			}

			Throwable throwable = message.getException();

			if(throwable != null) {
				Plugin.info(throwable);
			}
		} catch(CoreException e) {
			Plugin.log(e);
		}
	}
}
