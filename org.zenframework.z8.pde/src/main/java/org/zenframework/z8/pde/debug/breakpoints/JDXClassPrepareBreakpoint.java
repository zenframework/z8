package org.zenframework.z8.pde.debug.breakpoints;

import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IBreakpoint;

import org.zenframework.z8.pde.debug.JDXMessages;
import org.zenframework.z8.pde.debug.model.JDXDebugModel;
import org.zenframework.z8.pde.debug.model.JDXDebugTarget;
import org.zenframework.z8.pde.debug.model.JDXThread;
import org.zenframework.z8.pde.debug.model.value.JDXObjectValue;

import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequest;

public class JDXClassPrepareBreakpoint extends JDXBreakpoint {
	public static final int TYPE_CLASS = 0;
	public static final int TYPE_INTERFACE = 1;

	private static final String JAVA_CLASS_PREPARE_BREAKPOINT = "org.zenframework.z8.pde.jdxClassPrepareBreakpointMarker";
	protected static final String MEMBER_TYPE = "org.zenframework.z8.pde.memberType";

	public JDXClassPrepareBreakpoint(final IResource resource, final String typeName, final int memberType, final int charStart, final int charEnd, final boolean add, final Map<String, Object> attributes) throws DebugException {
		IWorkspaceRunnable wr = new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				setMarker(resource.createMarker(JAVA_CLASS_PREPARE_BREAKPOINT));
				attributes.put(IBreakpoint.ID, getModelIdentifier());
				attributes.put(IMarker.CHAR_START, new Integer(charStart));
				attributes.put(IMarker.CHAR_END, new Integer(charEnd));
				attributes.put(TYPE_NAME, typeName);
				attributes.put(MEMBER_TYPE, new Integer(memberType));
				attributes.put(ENABLED, Boolean.TRUE);
				ensureMarker().setAttributes(attributes);
				register(add);
			}
		};
		run(getMarkerRule(resource), wr);
	}

	public JDXClassPrepareBreakpoint() {
	}

	@Override
	protected void createRequests(JDXDebugTarget target) throws CoreException {
		if(target.isTerminated() || shouldSkipBreakpoint()) {
			return;
		}
		String referenceTypeName = getTypeName();
		if(referenceTypeName == null) {
			return;
		}
		ClassPrepareRequest request = target.createClassPrepareRequest(referenceTypeName, null, false);
		configureRequestHitCount(request);
		updateEnabledState(request, target);
		registerRequest(request, target);
		incrementInstallCount();
	}

	@Override
	protected void deregisterRequest(EventRequest request, JDXDebugTarget target) throws CoreException {
		target.removeEventListener(this, request);

		if(getMarker().exists()) {
			decrementInstallCount();
		}
	}

	@Override
	protected void addInstanceFilter(EventRequest request, ObjectReference object) {
	}

	@Override
	protected EventRequest[] newRequests(JDXDebugTarget target, ReferenceType type) {
		return null;
	}

	@Override
	protected void setRequestThreadFilter(EventRequest request, ThreadReference thread) {
	}

	@Override
	public boolean handleClassPrepareEvent(ClassPrepareEvent event, JDXDebugTarget target) {
		try {
			if(isEnabled() && event.referenceType().name().equals(getTypeName())) {
				ThreadReference threadRef = event.thread();
				JDXThread thread = target.findThread(threadRef);
				if(thread == null) {
					return true;
				}
				return handleBreakpointEvent(event, target, thread);
			}
		} catch(CoreException e) {
		}
		return true;
	}

	public int getMemberType() throws CoreException {
		return ensureMarker().getAttribute(MEMBER_TYPE, TYPE_CLASS);
	}

	@Override
	public boolean supportsInstanceFilters() {
		return false;
	}

	@Override
	public void addInstanceFilter(JDXObjectValue object) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, JDXDebugModel.getModelIdentifier(), DebugException.REQUEST_FAILED, JDXMessages.JDXClassPrepareBreakpoint_2, null));
	}

	@Override
	public void setThreadFilter(JDXThread thread) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, JDXDebugModel.getModelIdentifier(), DebugException.REQUEST_FAILED, JDXMessages.JDXClassPrepareBreakpoint_3, null));
	}

	@Override
	public boolean supportsThreadFilters() {
		return false;
	}
}
