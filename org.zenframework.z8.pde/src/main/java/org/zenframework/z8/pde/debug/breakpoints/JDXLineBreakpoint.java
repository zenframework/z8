package org.zenframework.z8.pde.debug.breakpoints;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.ILineBreakpoint;

import org.zenframework.z8.compiler.workspace.CompilationUnit;
import org.zenframework.z8.compiler.workspace.Workspace;
import org.zenframework.z8.pde.Plugin;
import org.zenframework.z8.pde.debug.JDXMessages;
import org.zenframework.z8.pde.debug.model.JDXDebugTarget;
import org.zenframework.z8.pde.debug.model.JDXThread;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassNotPreparedException;
import com.sun.jdi.Location;
import com.sun.jdi.NativeMethodException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.event.Event;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;

public class JDXLineBreakpoint extends JDXBreakpoint implements ILineBreakpoint {
	protected static final String SOURCE_NAME = "org.zenframework.z8.pde.sourceName";
	private static final String JDX_LINE_BREAKPOINT = "org.zenframework.z8.pde.jdxLineBreakpointMarker";

	public static final int NO_LINE_NUMBERS = 162;

	public JDXLineBreakpoint() {
	}

	public JDXLineBreakpoint(IResource resource, String typeName, int lineNumber, int javaLineNumber, int charStart, int charEnd, int hitCount, boolean add, Map<String, Object> attributes) throws DebugException {
		this(resource, typeName, lineNumber, javaLineNumber, charStart, charEnd, hitCount, add, attributes, JDX_LINE_BREAKPOINT);
	}

	protected JDXLineBreakpoint(final IResource resource, final String typeName, final int lineNumber, final int javaLineNumber, final int charStart, final int charEnd, final int hitCount, final boolean add, final Map<String, Object> attributes, final String markerType)
			throws DebugException {
		IWorkspaceRunnable wr = new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				setMarker(resource.createMarker(markerType));
				addLineBreakpointAttributes(attributes, getModelIdentifier(), true, lineNumber, javaLineNumber, charStart, charEnd);
				addTypeNameAndHitCount(attributes, typeName, hitCount);
				ensureMarker().setAttributes(attributes);
				register(add);
			}
		};
		run(getMarkerRule(resource), wr);
	}

	@Override
	public void addToTarget(JDXDebugTarget target) throws CoreException {
		super.addToTarget(target);
	}

	@Override
	public void removeFromTarget(JDXDebugTarget target) throws CoreException {
		super.removeFromTarget(target);
	}

	@Override
	public int getLineNumber() throws CoreException {
		return ensureMarker().getAttribute(IMarker.LINE_NUMBER, -1);
	}

	public int getJavaLineNumber() throws CoreException {
		CompilationUnit compilationUnit = Workspace.getInstance().getCompilationUnit(ensureMarker().getResource());

		return compilationUnit != null ? compilationUnit.getTargetLineNumber(getLineNumber() - 1) + 1 : -1;
		// return ensureMarker().getResource().getAttribute(JAVA_LINE_NUMBER,
		// -1);
	}

	@Override
	public int getCharStart() throws CoreException {
		return ensureMarker().getAttribute(IMarker.CHAR_START, -1);
	}

	@Override
	public int getCharEnd() throws CoreException {
		return ensureMarker().getAttribute(IMarker.CHAR_END, -1);
	}

	public static String getMarkerType() {
		return JDX_LINE_BREAKPOINT;
	}

	@Override
	protected EventRequest[] newRequests(JDXDebugTarget target, ReferenceType type) throws CoreException {
		int lineNumber = getJavaLineNumber();

		List<Location> locations = determineLocations(lineNumber, type, target);

		if(locations == null || locations.isEmpty()) {
			return null;
		}

		EventRequest[] requests = new EventRequest[locations.size()];

		int i = 0;

		Iterator<Location> iterator = locations.iterator();

		while(iterator.hasNext()) {
			Location location = iterator.next();
			requests[i] = createLineBreakpointRequest(location, target);
			i++;
		}
		return requests;
	}

	protected BreakpointRequest createLineBreakpointRequest(Location location, JDXDebugTarget target) throws CoreException {
		BreakpointRequest request = null;
		EventRequestManager manager = target.getEventRequestManager();
		if(manager == null) {
			target.requestFailed(JDXMessages.JDXLineBreakpoint_Unable_to_create_breakpoint_request___VM_disconnected__1, null);
		}
		try {
			request = manager.createBreakpointRequest(location);
			configureRequest(request, target);
		} catch(VMDisconnectedException e) {
			if(!target.isAvailable()) {
				return null;
			}
			Plugin.log(e);
		} catch(RuntimeException e) {
			target.internalError(e);
			return null;
		}
		return request;
	}

	@Override
	protected void setRequestThreadFilter(EventRequest request, ThreadReference thread) {
		((BreakpointRequest)request).addThreadFilter(thread);
	}

	protected List<Location> determineLocations(int lineNumber, ReferenceType type, JDXDebugTarget target) {
		List<Location> locations = null;

		try {
			locations = type.locationsOfLine(lineNumber);
		} catch(AbsentInformationException aie) {
			IStatus status = new Status(IStatus.ERROR, Plugin.getUniqueIdentifier(), NO_LINE_NUMBERS, JDXMessages.JDXLineBreakpoint_Absent_Line_Number_Information_1, null);
			IStatusHandler handler = DebugPlugin.getDefault().getStatusHandler(status);
			if(handler != null) {
				try {
					handler.handleStatus(status, type);
				} catch(CoreException e) {
				}
			}
			return null;
		} catch(NativeMethodException e) {
			return null;
		} catch(VMDisconnectedException e) {
			return null;
		} catch(ClassNotPreparedException e) {
			return null;
		} catch(RuntimeException e) {
			target.internalError(e);
			return null;
		}
		return locations;
	}

	public void addLineBreakpointAttributes(Map<String, Object> attributes, String modelIdentifier, boolean enabled, int lineNumber, int javaLineNumber, int charStart, int charEnd) {
		attributes.put(IBreakpoint.ID, modelIdentifier);
		attributes.put(IBreakpoint.ENABLED, new Boolean(enabled));
		attributes.put(IMarker.LINE_NUMBER, new Integer(lineNumber));
		attributes.put(IMarker.CHAR_START, new Integer(charStart));
		attributes.put(IMarker.CHAR_END, new Integer(charEnd));

		attributes.put(JAVA_LINE_NUMBER, new Integer(javaLineNumber));
	}

	public void addTypeNameAndHitCount(Map<String, Object> attributes, String typeName, int hitCount) {
		attributes.put(TYPE_NAME, typeName);
		if(hitCount > 0) {
			attributes.put(HIT_COUNT, new Integer(hitCount));
			attributes.put(EXPIRED, Boolean.FALSE);
		}
	}

	@Override
	public boolean handleBreakpointEvent(Event event, JDXDebugTarget target, JDXThread thread) {
		return !suspendForEvent(event, thread);
	}

	protected boolean suspendForEvent(Event event, JDXThread thread) {
		expireHitCount(event);
		return suspend(thread);
	}

	protected String getMarkerMessage(boolean conditionEnabled, String condition, int hitCount, int suspendPolicy, int lineNumber) {
		StringBuffer message = new StringBuffer(super.getMarkerMessage(hitCount, suspendPolicy));
		if(lineNumber != -1) {
			message.append(MessageFormat.format(JDXMessages.JDXLineBreakpoint___line___0___1, new Object[] { Integer.toString(lineNumber) }));
		}
		if(conditionEnabled && condition != null) {
			message.append(MessageFormat.format(JDXMessages.JDXLineBreakpoint___Condition___0___2, new Object[] { condition }));
		}
		return message.toString();
	}

	@Override
	protected void cleanupForThreadTermination(JDXThread thread) {
		super.cleanupForThreadTermination(thread);
	}

	@Override
	protected void addInstanceFilter(EventRequest request, ObjectReference object) {
		if(request instanceof BreakpointRequest) {
			((BreakpointRequest)request).addInstanceFilter(object);
		}
	}
}
