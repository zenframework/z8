package org.zenframework.z8.pde.debug.breakpoints;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IDebugTarget;

import org.zenframework.z8.pde.debug.JDXMessages;
import org.zenframework.z8.pde.debug.model.JDXDebugTarget;

import com.sun.jdi.Field;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.event.AccessWatchpointEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.ModificationWatchpointEvent;
import com.sun.jdi.request.AccessWatchpointRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.ModificationWatchpointRequest;
import com.sun.jdi.request.WatchpointRequest;

public class JDXWatchpoint extends JDXLineBreakpoint {
	private static final String JDX_WATCHPOINT = "org.zenframework.z8.pde.jdxWatchpointMarker";
	protected static final String ACCESS = "org.zenframework.z8.pde.access";
	protected static final String MODIFICATION = "org.zenframework.z8.pde.modification";
	protected static final String AUTO_DISABLED = "org.zenframework.z8.pde.auto_disabled";
	protected static final String FIELD_NAME = "org.zenframework.z8.pde.fieldName";

	protected static final Integer ACCESS_EVENT = new Integer(0);
	protected static final Integer MODIFICATION_EVENT = new Integer(1);

	private HashMap<JDXDebugTarget, Integer> m_lastEventTypes = new HashMap<JDXDebugTarget, Integer>(10);

	public JDXWatchpoint() {
	}

	public JDXWatchpoint(final IResource resource, final String typeName, final String fieldName, final int lineNumber, final int javaLineNumber, final int charStart, final int charEnd, final int hitCount, final boolean add, final Map<String, Object> attributes)
			throws DebugException {
		IWorkspaceRunnable wr = new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				setMarker(resource.createMarker(JDX_WATCHPOINT));
				addLineBreakpointAttributes(attributes, getModelIdentifier(), true, lineNumber, javaLineNumber, charStart, charEnd);
				addTypeNameAndHitCount(attributes, typeName, hitCount);
				addFieldName(attributes, fieldName);
				addDefaultAccessAndModification(attributes);
				ensureMarker().setAttributes(attributes);
				register(add);
			}
		};
		run(getMarkerRule(resource), wr);
	}

	@Override
	protected boolean createRequest(JDXDebugTarget target, ReferenceType type) throws CoreException {
		if(shouldSkipBreakpoint()) {
			return false;
		}
		Field field = null;
		field = type.fieldByName(getFieldName());
		if(field == null) {
			return false;
		}
		AccessWatchpointRequest accessRequest = null;
		ModificationWatchpointRequest modificationRequest = null;
		if(target.supportsAccessWatchpoints()) {
			accessRequest = createAccessWatchpoint(target, field);
			registerRequest(accessRequest, target);
		} else {
			notSupported(JDXMessages.JDXWatchpoint_no_access_watchpoints);
		}
		if(target.supportsModificationWatchpoints()) {
			modificationRequest = createModificationWatchpoint(target, field);
			if(modificationRequest == null) {
				return false;
			}
			registerRequest(modificationRequest, target);
			return true;
		}
		notSupported(JDXMessages.JDXWatchpoint_no_modification_watchpoints);
		return false;
	}

	@Override
	protected void setRequestThreadFilter(EventRequest request, ThreadReference thread) {
		((WatchpointRequest)request).addThreadFilter(thread);
	}

	protected void notSupported(String message) throws DebugException {
		throw new DebugException(new Status(IStatus.ERROR, DebugPlugin.getUniqueIdentifier(), DebugException.NOT_SUPPORTED, message, null));
	}

	protected AccessWatchpointRequest createAccessWatchpoint(JDXDebugTarget target, Field field) throws CoreException {
		return (AccessWatchpointRequest)createWatchpoint(target, field, true);
	}

	protected ModificationWatchpointRequest createModificationWatchpoint(JDXDebugTarget target, Field field) throws CoreException {
		return (ModificationWatchpointRequest)createWatchpoint(target, field, false);
	}

	protected WatchpointRequest createWatchpoint(JDXDebugTarget target, Field field, boolean access) throws CoreException {
		WatchpointRequest request = null;
		EventRequestManager manager = target.getEventRequestManager();
		if(manager == null) {
			target.requestFailed(JDXMessages.JDXWatchpoint_Unable_to_create_breakpoint_request___VM_disconnected__1, null);
		}
		try {
			if(access) {
				request = manager.createAccessWatchpointRequest(field);
			} else {
				request = manager.createModificationWatchpointRequest(field);
			}
			configureRequest(request, target);
		} catch(VMDisconnectedException e) {
			if(!target.isAvailable()) {
				return null;
			}
			target.internalError(e);
			return null;
		} catch(RuntimeException e) {
			target.internalError(e);
			return null;
		}
		return request;
	}

	protected EventRequest recreateRequest(EventRequest request, JDXDebugTarget target) throws CoreException {
		try {
			Field field = ((WatchpointRequest)request).field();
			if(request instanceof AccessWatchpointRequest) {
				request = createAccessWatchpoint(target, field);
			} else if(request instanceof ModificationWatchpointRequest) {
				request = createModificationWatchpoint(target, field);
			}
		} catch(VMDisconnectedException e) {
			if(!target.isAvailable()) {
				return request;
			}
			target.internalError(e);
			return request;
		} catch(RuntimeException e) {
			target.internalError(e);
		}
		return request;
	}

	@Override
	public void setEnabled(boolean enabled) throws CoreException {
		if(enabled) {
			if(!(isAccess() || isModification())) {
				setDefaultAccessAndModification();
			}
		}
		super.setEnabled(enabled);
	}

	public boolean isAccess() throws CoreException {
		return ensureMarker().getAttribute(ACCESS, false);
	}

	public void setAccess(boolean access) throws CoreException {
		if(access == isAccess()) {
			return;
		}
		setAttribute(ACCESS, access);
		if(access && !isEnabled()) {
			setEnabled(true);
		} else if(!(access || isModification())) {
			setEnabled(false);
		}
		recreate();
	}

	public boolean isModification() throws CoreException {
		return ensureMarker().getAttribute(MODIFICATION, false);
	}

	public void setModification(boolean modification) throws CoreException {
		if(modification == isModification()) {
			return;
		}
		setAttribute(MODIFICATION, modification);
		if(modification && !isEnabled()) {
			setEnabled(true);
		} else if(!(modification || isAccess())) {
			setEnabled(false);
		}
		recreate();
	}

	protected void setDefaultAccessAndModification() throws CoreException {
		Object[] values = new Object[] { Boolean.TRUE, Boolean.TRUE };
		String[] attributes = new String[] { ACCESS, MODIFICATION };
		setAttributes(attributes, values);
	}

	protected void addDefaultAccessAndModification(Map<String, Object> attributes) {
		attributes.put(ACCESS, Boolean.TRUE);
		attributes.put(MODIFICATION, Boolean.TRUE);
		attributes.put(AUTO_DISABLED, Boolean.FALSE);
	}

	protected void addFieldName(Map<String, Object> attributes, String fieldName) {
		attributes.put(FIELD_NAME, fieldName);
	}

	public String getFieldName() throws CoreException {
		return ensureMarker().getAttribute(FIELD_NAME, null);
	}

	@Override
	public boolean handleEvent(Event event, JDXDebugTarget target) {
		if(event instanceof AccessWatchpointEvent) {
			m_lastEventTypes.put(target, ACCESS_EVENT);
		} else if(event instanceof ModificationWatchpointEvent) {
			m_lastEventTypes.put(target, MODIFICATION_EVENT);
		}
		return super.handleEvent(event, target);
	}

	@Override
	protected void updateEnabledState(EventRequest request, JDXDebugTarget target) throws CoreException {
		boolean enabled = isEnabled();
		if(request instanceof AccessWatchpointRequest) {
			if(isAccess()) {
				if(enabled != request.isEnabled()) {
					internalUpdateEnabledState(request, enabled, target);
				}
			} else {
				if(request.isEnabled()) {
					internalUpdateEnabledState(request, false, target);
				}
			}
		}
		if(request instanceof ModificationWatchpointRequest) {
			if(isModification()) {
				if(enabled != request.isEnabled()) {
					internalUpdateEnabledState(request, enabled, target);
				}
			} else {
				if(request.isEnabled()) {
					internalUpdateEnabledState(request, false, target);
				}
			}
		}
	}

	public boolean isAccessSuspend(IDebugTarget target) {
		Integer lastEventType = m_lastEventTypes.get(target);
		if(lastEventType == null) {
			return false;
		}
		return lastEventType.equals(ACCESS_EVENT);
	}

	@Override
	public void removeFromTarget(JDXDebugTarget target) throws CoreException {
		m_lastEventTypes.remove(target);
		super.removeFromTarget(target);
	}

	@Override
	protected void addInstanceFilter(EventRequest request, ObjectReference object) {
		if(request instanceof WatchpointRequest) {
			((WatchpointRequest)request).addInstanceFilter(object);
		}
	}

	public boolean supportsAccess() {
		return true;
	}

	public boolean supportsModification() {
		return true;
	}
}
