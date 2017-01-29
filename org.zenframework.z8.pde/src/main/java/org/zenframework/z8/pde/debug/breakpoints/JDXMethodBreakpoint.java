package org.zenframework.z8.pde.debug.breakpoints;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.model.IDebugTarget;

import org.zenframework.z8.pde.Plugin;
import org.zenframework.z8.pde.debug.JDXMessages;
import org.zenframework.z8.pde.debug.model.JDXDebugTarget;
import org.zenframework.z8.pde.debug.model.JDXThread;

import com.sun.jdi.ClassType;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.event.MethodEntryEvent;
import com.sun.jdi.event.MethodExitEvent;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.MethodEntryRequest;
import com.sun.jdi.request.MethodExitRequest;

public class JDXMethodBreakpoint extends JDXLineBreakpoint {
	private static final String JDX_METHOD_BREAKPOINT = "org.zenframework.z8.pde.jdxMethodBreakpointMarker";
	private static final String METHOD_NAME = "org.zenframework.z8.pde.methodName";
	private static final String METHOD_SIGNATURE = "org.zenframework.z8.pde.methodSignature";
	private static final String ENTRY = "org.zenframework.z8.pde.entry";
	private static final String EXIT = "v.pde.exit";
	private static final String NATIVE = "org.zenframework.z8.pde.native";

	private String m_methodName = null;
	private String m_methodSignature = null;

	protected static final Integer ENTRY_EVENT = new Integer(0);
	protected static final Integer EXIT_EVENT = new Integer(1);

	private Map<JDXDebugTarget, Integer> fLastEventTypes = new HashMap<JDXDebugTarget, Integer>(10);

	private Pattern fPattern;
	private Boolean fUsesTypePattern = null;

	public JDXMethodBreakpoint() {
	}

	public JDXMethodBreakpoint(final IResource resource, final String typePattern, final String methodName, final String methodSignature, final boolean entry, final boolean exit, final boolean nativeOnly, final int lineNumber, final int javaLineNumber, final int charStart,
			final int charEnd, final int hitCount, final boolean register, final Map<String, Object> attributes) throws CoreException {
		IWorkspaceRunnable wr = new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				setMarker(resource.createMarker(JDX_METHOD_BREAKPOINT));
				addLineBreakpointAttributes(attributes, getModelIdentifier(), true, lineNumber, javaLineNumber, charStart, charEnd);
				addMethodNameAndSignature(attributes, methodName, methodSignature);
				addTypeNameAndHitCount(attributes, typePattern, hitCount);
				attributes.put(ENTRY, new Boolean(entry));
				attributes.put(EXIT, new Boolean(exit));
				attributes.put(NATIVE, new Boolean(nativeOnly));
				ensureMarker().setAttributes(attributes);
				register(register);
			}
		};
		run(getMarkerRule(resource), wr);
		String type = convertToRegularExpression(typePattern);
		fPattern = Pattern.compile(type);
	}

	protected void createRequest(JDXDebugTarget target, String typePattern) throws CoreException {
		MethodEntryRequest entryRequest = createMethodEntryRequest(target, typePattern);
		MethodExitRequest exitRequest = createMethodExitRequest(target, typePattern);
		registerRequest(entryRequest, target);
		registerRequest(exitRequest, target);
	}

	protected MethodEntryRequest createMethodEntryRequest(JDXDebugTarget target, String typePattern) throws CoreException {
		return (MethodEntryRequest)createMethodRequest(target, typePattern, true);
	}

	protected MethodExitRequest createMethodExitRequest(JDXDebugTarget target, String typePattern) throws CoreException {
		return (MethodExitRequest)createMethodRequest(target, typePattern, false);
	}

	protected EventRequest createMethodEntryRequest(JDXDebugTarget target, ReferenceType type) throws CoreException {
		return createMethodRequest(target, type, true);
	}

	protected EventRequest createMethodExitRequest(JDXDebugTarget target, ReferenceType type) throws CoreException {
		return createMethodRequest(target, type, false);
	}

	private EventRequest createMethodRequest(JDXDebugTarget target, Object classFilter, boolean entry) throws CoreException {
		EventRequest request = null;
		EventRequestManager manager = target.getEventRequestManager();
		if(manager == null) {
			target.requestFailed(JDXMessages.JDXMethodBreakpoint_Unable_to_create_breakpoint_request___VM_disconnected__1, null); // $NON-NLS-1$
		}
		try {
			if(entry) {
				if(classFilter instanceof ClassType && getMethodName() != null && getMethodSignature() != null) {
					ClassType clazz = (ClassType)classFilter;
					if(clazz.name().equals(getTypeName())) {
						Method method = clazz.concreteMethodByName(getMethodName(), getMethodSignature());
						if(method != null && !method.isNative()) {
							Location location = method.location();
							if(location != null && location.codeIndex() != -1) {
								request = manager.createBreakpointRequest(location);
							}
						}
					}
				}
				if(request == null) {
					request = manager.createMethodEntryRequest();
					if(classFilter instanceof String) {
						((MethodEntryRequest)request).addClassFilter((String)classFilter);
					} else if(classFilter instanceof ReferenceType) {
						((MethodEntryRequest)request).addClassFilter((ReferenceType)classFilter);
					}
				}
			} else {
				request = manager.createMethodExitRequest();
				if(classFilter instanceof String) {
					((MethodExitRequest)request).addClassFilter((String)classFilter);
				} else if(classFilter instanceof ReferenceType) {
					((MethodExitRequest)request).addClassFilter((ReferenceType)classFilter);
				}
			}
			configureRequest(request, target);
		} catch(VMDisconnectedException e) {
			if(!target.isAvailable()) {
				return null;
			}
			Plugin.log(e);
		} catch(RuntimeException e) {
			target.internalError(e);
		}
		return request;
	}

	@Override
	protected void setRequestThreadFilter(EventRequest request, ThreadReference thread) {
		if(request instanceof MethodEntryRequest) {
			((MethodEntryRequest)request).addThreadFilter(thread);
		} else if(request instanceof MethodExitRequest) {
			((MethodExitRequest)request).addThreadFilter(thread);
		} else if(request instanceof BreakpointRequest) {
			((BreakpointRequest)request).addThreadFilter(thread);
		}
	}

	@Override
	protected void configureRequestHitCount(EventRequest request) throws CoreException {
		if(request instanceof BreakpointRequest) {
			super.configureRequestHitCount(request);
		} else {
			int hitCount = getHitCount();
			if(hitCount > 0) {
				request.putProperty(HIT_COUNT, new Integer(hitCount));
			}
		}
	}

	@Override
	protected void updateEnabledState(EventRequest request, JDXDebugTarget target) throws CoreException {
		boolean enabled = isEnabled();
		if(request instanceof MethodEntryRequest || request instanceof BreakpointRequest) {
			enabled = enabled && isEntry();
		} else if(request instanceof MethodExitRequest) {
			enabled = enabled && isExit();
		}
		if(enabled != request.isEnabled()) {
			internalUpdateEnabledState(request, enabled, target);
		}
	}

	protected void addMethodNameAndSignature(Map<String, Object> attributes, String methodName, String methodSignature) {
		if(methodName != null) {
			attributes.put(METHOD_NAME, methodName);
		}
		if(methodSignature != null) {
			attributes.put(METHOD_SIGNATURE, methodSignature);
		}
		m_methodName = methodName;
		m_methodSignature = methodSignature;
	}

	public boolean isEntrySuspend(IDebugTarget target) {
		Integer lastEventType = fLastEventTypes.get(target);
		if(lastEventType == null) {
			return false;
		}
		return lastEventType.equals(ENTRY_EVENT);
	}

	public boolean handleBreakpoirntEvent(Event event, JDXDebugTarget target, JDXThread thread) {
		if(event instanceof MethodEntryEvent) {
			MethodEntryEvent entryEvent = (MethodEntryEvent)event;
			fLastEventTypes.put(target, ENTRY_EVENT);
			return handleMethodEvent(entryEvent, entryEvent.method(), target, thread);
		} else if(event instanceof MethodExitEvent) {
			MethodExitEvent exitEvent = (MethodExitEvent)event;
			fLastEventTypes.put(target, EXIT_EVENT);
			return handleMethodEvent(exitEvent, exitEvent.method(), target, thread);
		} else if(event instanceof BreakpointEvent) {
			fLastEventTypes.put(target, ENTRY_EVENT);
			return super.handleBreakpointEvent(event, target, thread);
		}
		return true;
	}

	protected boolean handleMethodEvent(LocatableEvent event, Method method, JDXDebugTarget target, JDXThread thread) {
		try {
			if(isNativeOnly()) {
				if(!method.isNative()) {
					return true;
				}
			}
			if(getMethodName() != null) {
				if(!method.name().equals(getMethodName())) {
					return true;
				}
			}
			if(getMethodSignature() != null) {
				if(!method.signature().equals(getMethodSignature())) {
					return true;
				}
			}
			if(fPattern != null) {
				if(!fPattern.matcher(method.declaringType().name()).find()) {
					return true;
				}
			}
			Integer count = (Integer)event.request().getProperty(HIT_COUNT);
			if(count != null && handleHitCount(event, count)) {
				return true;
			}
			return !suspendForEvent(event, thread); // Resume if suspend fails
		} catch(CoreException e) {
			Plugin.log(e);
		}
		return true;
	}

	private boolean handleHitCount(LocatableEvent event, Integer count) {
		int hitCount = count.intValue();
		if(hitCount > 0) {
			hitCount--;
			count = new Integer(hitCount);
			event.request().putProperty(HIT_COUNT, count);
			if(hitCount == 0) {
				try {
					setExpired(true);
					setEnabled(false);
				} catch(CoreException e) {
					Plugin.log(e);
				}
				return false;
			}
			return true;
		}
		return true;
	}

	public String getMethodName() {
		return m_methodName;
	}

	public String getMethodSignature() {
		return m_methodSignature;
	}

	public boolean isEntry() throws CoreException {
		return ensureMarker().getAttribute(ENTRY, false);
	}

	public boolean isExit() throws CoreException {
		return ensureMarker().getAttribute(EXIT, false);
	}

	public boolean isNativeOnly() throws CoreException {
		return ensureMarker().getAttribute(NATIVE, false);
	}

	public void setEntry(boolean entry) throws CoreException {
		if(isEntry() != entry) {
			setAttribute(ENTRY, entry);
			if(entry && !isEnabled()) {
				setEnabled(true);
			} else if(!(entry || isExit())) {
				setEnabled(false);
			}
			recreate();
		}
	}

	public void setExit(boolean exit) throws CoreException {
		if(isExit() != exit) {
			setAttribute(EXIT, exit);
			if(exit && !isEnabled()) {
				setEnabled(true);
			} else if(!(exit || isEntry())) {
				setEnabled(false);
			}
			recreate();
		}
	}

	public void setNativeOnly(boolean nativeOnly) throws CoreException {
		if(isNativeOnly() != nativeOnly) {
			setAttribute(NATIVE, nativeOnly);
			recreate();
		}
	}

	@Override
	public void setMarker(IMarker marker) throws CoreException {
		super.setMarker(marker);
		m_methodName = marker.getAttribute(METHOD_NAME, null);
		m_methodSignature = marker.getAttribute(METHOD_SIGNATURE, null);
		String typePattern = marker.getAttribute(TYPE_NAME, ""); //$NON-NLS-1$
		if(typePattern != null) {
			fPattern = Pattern.compile(convertToRegularExpression(typePattern));
		}
	}

	private String convertToRegularExpression(String stringMatcherPattern) {
		String regex = stringMatcherPattern.replaceAll("\\.", "\\\\.");
		regex = regex.replaceAll("\\*", "\\.\\*");
		return regex;
	}

	@Override
	public void setEnabled(boolean enabled) throws CoreException {
		if(enabled) {
			if(!(isEntry() || isExit())) {
				setDefaultEntryAndExit();
			}
		}
		super.setEnabled(enabled);
	}

	protected void setDefaultEntryAndExit() throws CoreException {
		Object[] values = new Object[] { Boolean.TRUE, Boolean.FALSE };
		String[] attributes = new String[] { ENTRY, EXIT };
		setAttributes(attributes, values);
	}

	public boolean supportsCondition() {
		return true;
	}

	@Override
	public void addToTarget(JDXDebugTarget target) throws CoreException {
		if(usesTypePattern()) {
			fireAdding(target);
			String referenceTypeNamePattern = getTypeName();
			if(referenceTypeNamePattern == null) {
				return;
			}
			createRequest(target, referenceTypeNamePattern);
		} else {
			super.addToTarget(target);
		}
	}

	@Override
	public void removeFromTarget(JDXDebugTarget target) throws CoreException {
		fLastEventTypes.remove(target);
		super.removeFromTarget(target);
	}

	protected boolean usesTypePattern() throws CoreException {
		if(fUsesTypePattern == null) {
			String name = getTypeName();
			fUsesTypePattern = new Boolean(name != null && (name.startsWith("*") || name.endsWith("*"))); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return fUsesTypePattern.booleanValue();
	}

	@Override
	protected boolean createRequest(JDXDebugTarget target, ReferenceType type) throws CoreException {
		if(!type.name().equals(getTypeName()) || shouldSkipBreakpoint()) {
			return false;
		}
		EventRequest entryRequest = createMethodEntryRequest(target, type);
		EventRequest exitRequest = createMethodExitRequest(target, type);
		registerRequest(entryRequest, target);
		registerRequest(exitRequest, target);
		return true;
	}

	@Override
	protected void setTypeName(String typeName) throws CoreException {
		fUsesTypePattern = null;
		super.setTypeName(typeName);
	}

	@Override
	protected void addInstanceFilter(EventRequest request, ObjectReference object) {
		if(request instanceof MethodEntryRequest) {
			((MethodEntryRequest)request).addInstanceFilter(object);
		} else if(request instanceof MethodExitRequest) {
			((MethodExitRequest)request).addInstanceFilter(object);
		} else {
			super.addInstanceFilter(request, object);
		}
	}
}
