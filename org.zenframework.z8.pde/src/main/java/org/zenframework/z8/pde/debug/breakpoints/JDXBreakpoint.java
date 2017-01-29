package org.zenframework.z8.pde.debug.breakpoints;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.model.Breakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IValue;

import org.zenframework.z8.pde.Plugin;
import org.zenframework.z8.pde.debug.JDXDebug;
import org.zenframework.z8.pde.debug.JDXMessages;
import org.zenframework.z8.pde.debug.model.JDXDebugModel;
import org.zenframework.z8.pde.debug.model.JDXDebugTarget;
import org.zenframework.z8.pde.debug.model.JDXEventListener;
import org.zenframework.z8.pde.debug.model.JDXThread;
import org.zenframework.z8.pde.debug.model.type.JDXType;
import org.zenframework.z8.pde.debug.model.value.JDXObjectValue;

import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;

public abstract class JDXBreakpoint extends Breakpoint implements JDXEventListener, IDebugEventSetListener {
	public static final int SUSPEND_VM = 1;
	public static final int SUSPEND_THREAD = 2;

	protected static final String JAVA_LINE_NUMBER = "org.zenframework.z8.pde.javaLineNumber";

	protected static final String EXPIRED = "org.zenframework.z8.pde.expired";
	protected static final String HIT_COUNT = "org.zenframework.z8.pde.hitCount";
	protected static final String INSTALL_COUNT = "org.zenframework.z8.pde.installCount";
	protected static final String TYPE_NAME = "org.zenframework.z8.pde.typeName";
	protected static final String SUSPEND_POLICY = "org.zenframework.z8.pde.suspendPolicy";

	protected HashMap<JDXDebugTarget, ArrayList<EventRequest>> m_requestsByTarget;
	protected Map<JDXDebugTarget, JDXThread> m_filteredThreadsByTarget;
	protected Set<JDXDebugTarget> m_installedTargets = null;
	protected List<JDXObjectValue> m_instanceFilters = null;
	protected String m_installedTypeName = null;

	protected static final JDXObjectValue[] m_emptyInstanceFilters = new JDXObjectValue[0];

	public static final String JDX_BREAKPOINT_PROPERTY = "org.zenframework.z8.breakpoint";
	protected static final String[] m_expiredEnabledAttributes = new String[] { EXPIRED, ENABLED };

	public JDXBreakpoint() {
		m_requestsByTarget = new HashMap<JDXDebugTarget, ArrayList<EventRequest>>(1);
		m_filteredThreadsByTarget = new HashMap<JDXDebugTarget, JDXThread>(1);
	}

	@Override
	public String getModelIdentifier() {
		return JDXDebugModel.getModelIdentifier();
	}

	@Override
	public void setMarker(IMarker marker) throws CoreException {
		super.setMarker(marker);
		configureAtStartup();
	}

	protected void register(boolean register) throws CoreException {
		if(register) {
			DebugPlugin.getDefault().getBreakpointManager().addBreakpoint(this);
		} else {
			setRegistered(false);
		}
	}

	protected void registerRequest(EventRequest request, JDXDebugTarget target) throws CoreException {
		if(request == null) {
			return;
		}

		ArrayList<EventRequest> reqs = getRequests(target);

		if(reqs.isEmpty()) {
			m_requestsByTarget.put(target, reqs);
		}

		reqs.add(request);

		target.addEventListener(this, request);

		if(!(request instanceof ClassPrepareRequest)) {
			incrementInstallCount();
			// notification
			fireInstalled(target);
		}
	}

	protected String getEnclosingReferenceTypeName() throws CoreException {
		String name = getTypeName();
		int index = name.indexOf('$');
		if(index == -1) {
			return name;
		}
		return name.substring(0, index);
	}

	protected ArrayList<EventRequest> getRequests(JDXDebugTarget target) {
		ArrayList<EventRequest> list = m_requestsByTarget.get(target);

		if(list == null) {
			list = new ArrayList<EventRequest>(2);
		}
		return list;
	}

	protected void deregisterRequest(EventRequest request, JDXDebugTarget target) throws CoreException {
		target.removeEventListener(this, request);

		if(!(request instanceof ClassPrepareRequest) && getMarker().exists()) {
			decrementInstallCount();
		}
	}

	@Override
	public boolean handleEvent(Event event, JDXDebugTarget target) {
		if(event instanceof ClassPrepareEvent) {
			return handleClassPrepareEvent((ClassPrepareEvent)event, target);
		}

		ThreadReference threadRef = ((LocatableEvent)event).thread();
		JDXThread thread = target.findThread(threadRef);
		return thread == null ? true : handleBreakpointEvent(event, target, thread);
	}

	@Override
	public void wonSuspendVote(Event event, JDXDebugTarget target) {
		ThreadReference threadRef = null;
		if(event instanceof ClassPrepareEvent) {
			threadRef = ((ClassPrepareEvent)event).thread();
		} else if(event instanceof LocatableEvent) {
			threadRef = ((LocatableEvent)event).thread();
		}
		if(threadRef == null) {
			return;
		}
		JDXThread thread = target.findThread(threadRef);

		if(thread != null) {
			thread.wonSuspendVote(this);
		}
	}

	public boolean handleClassPrepareEvent(ClassPrepareEvent event, JDXDebugTarget target) {
		try {
			if(!installableReferenceType(event.referenceType(), target)) {
				// Don't install this breakpoint in an
				// inappropriate type
				return true;
			}
			createRequest(target, event.referenceType());
		} catch(CoreException e) {
			Plugin.log(e);
		}
		return true;
	}

	public boolean handleBreakpointEvent(Event event, JDXDebugTarget target, JDXThread thread) {
		expireHitCount(event);
		return !suspend(thread);
	}

	protected boolean suspend(JDXThread thread) {
		return thread.handleSuspendForBreakpoint(this, true);
	}

	protected boolean installableReferenceType(ReferenceType type, JDXDebugTarget target) throws CoreException {
		String installableType = getTypeName();
		String queriedType = type.name();
		if(installableType == null || queriedType == null) {
			return false;
		}

		int index = queriedType.indexOf('<');
		if(index != -1) {
			queriedType = queriedType.substring(0, index);
		}
		if(installableType.equals(queriedType)) {
			return queryInstallListeners(target, type);
		}
		index = queriedType.indexOf('$', 0);
		if(index == -1) {
			return false;
		}
		if(installableType.regionMatches(0, queriedType, 0, index)) {
			return queryInstallListeners(target, type);
		}
		return false;
	}

	protected void expireHitCount(Event event) {
		Integer requestCount = null;
		EventRequest request = null;
		if(event != null) {
			request = event.request();
			requestCount = (Integer)request.getProperty(HIT_COUNT);
		}
		if(requestCount != null) {
			if(request != null) {
				request.putProperty(EXPIRED, Boolean.TRUE);
			}
			try {
				setAttributes(m_expiredEnabledAttributes, new Object[] { Boolean.TRUE, Boolean.FALSE });
				// make a note that we auto-disabled this breakpoint.
			} catch(CoreException ce) {
				Plugin.log(ce);
			}
		}
	}

	public boolean shouldSkipBreakpoint() throws CoreException {
		return isRegistered() && !DebugPlugin.getDefault().getBreakpointManager().isEnabled();
	}

	protected boolean createRequest(JDXDebugTarget target, ReferenceType type) throws CoreException {
		if(shouldSkipBreakpoint()) {
			return false;
		}
		EventRequest[] requests = newRequests(target, type);
		if(requests == null) {
			return false;
		}
		m_installedTypeName = type.name();
		for(int i = 0; i < requests.length; i++) {
			EventRequest request = requests[i];
			registerRequest(request, target);
		}
		return true;
	}

	protected void configureRequest(EventRequest request, JDXDebugTarget target) throws CoreException {
		request.setSuspendPolicy(getJDXSuspendPolicy());
		request.putProperty(JDX_BREAKPOINT_PROPERTY, this);
		configureRequestThreadFilter(request, target);
		configureRequestHitCount(request);
		configureInstanceFilters(request, target);
		updateEnabledState(request, target);
	}

	protected abstract void addInstanceFilter(EventRequest request, ObjectReference object);

	protected void configureRequestThreadFilter(EventRequest request, JDXDebugTarget target) {
		JDXThread thread = m_filteredThreadsByTarget.get(target);

		if(thread == null) {
			return;
		}

		setRequestThreadFilter(request, thread.getUnderlyingThread());
	}

	protected void configureRequestHitCount(EventRequest request) throws CoreException {
		int hitCount = getHitCount();
		if(hitCount > 0) {
			request.addCountFilter(hitCount);
			request.putProperty(HIT_COUNT, new Integer(hitCount));
		}
	}

	protected void configureInstanceFilters(EventRequest request, JDXDebugTarget target) {
		if(m_instanceFilters != null && !m_instanceFilters.isEmpty()) {
			Iterator<JDXObjectValue> iter = m_instanceFilters.iterator();

			while(iter.hasNext()) {
				IValue object = (IValue)iter.next();

				if(object.getDebugTarget().equals(target)) {
					addInstanceFilter(request, ((JDXObjectValue)object).getUnderlyingObject());
				}
			}
		}
	}

	protected abstract EventRequest[] newRequests(JDXDebugTarget target, ReferenceType type) throws CoreException;

	public void addToTarget(JDXDebugTarget target) throws CoreException {
		fireAdding(target);
		createRequests(target);
	}

	protected void createRequests(JDXDebugTarget target) throws CoreException {
		if(target.isTerminated() || shouldSkipBreakpoint()) {
			return;
		}
		String referenceTypeName = getTypeName();
		String enclosingTypeName = getEnclosingReferenceTypeName();
		if(referenceTypeName == null || enclosingTypeName == null) {
			return;
		}
		if(referenceTypeName.indexOf('$') == -1) {
			registerRequest(target.createClassPrepareRequest(enclosingTypeName), target);
			registerRequest(target.createClassPrepareRequest(enclosingTypeName + "$*"), target);
		} else {
			registerRequest(target.createClassPrepareRequest(referenceTypeName), target);
			registerRequest(target.createClassPrepareRequest(enclosingTypeName + "$*", referenceTypeName), target);
		}

		List<ReferenceType> classes = target.classesByName(referenceTypeName);

		if(classes.isEmpty() && enclosingTypeName.equals(referenceTypeName)) {
			return;
		}

		boolean success = false;
		Iterator<ReferenceType> iter = classes.iterator();
		while(iter.hasNext()) {
			ReferenceType type = iter.next();
			if(createRequest(target, type)) {
				success = true;
			}
		}
		if(!success) {
			addToTargetForLocalType(target, enclosingTypeName);
		}
	}

	protected void addToTargetForLocalType(JDXDebugTarget target, String enclosingTypeName) throws CoreException {
		List<ReferenceType> classes = target.classesByName(enclosingTypeName);
		if(!classes.isEmpty()) {
			Iterator<ReferenceType> iter = classes.iterator();

			while(iter.hasNext()) {
				ReferenceType type = iter.next();

				Iterator<ReferenceType> nestedTypes = type.nestedTypes().iterator();

				while(nestedTypes.hasNext()) {
					ReferenceType nestedType = nestedTypes.next();

					if(createRequest(target, nestedType)) {
						break;
					}
				}
			}
		}
	}

	protected int getJDXSuspendPolicy() throws CoreException {
		int breakpointPolicy = getSuspendPolicy();
		if(breakpointPolicy == JDXBreakpoint.SUSPEND_THREAD) {
			return EventRequest.SUSPEND_EVENT_THREAD;
		}
		return EventRequest.SUSPEND_ALL;
	}

	protected boolean hasHitCountChanged(EventRequest request) throws CoreException {
		int hitCount = getHitCount();
		Integer requestCount = (Integer)request.getProperty(HIT_COUNT);
		int oldCount = -1;
		if(requestCount != null) {
			oldCount = requestCount.intValue();
		}
		return hitCount != oldCount;
	}

	public void removeFromTarget(final JDXDebugTarget target) throws CoreException {
		removeRequests(target);
		JDXThread removed = m_filteredThreadsByTarget.remove(target);
		boolean changed = removed != null;
		boolean markerExists = markerExists();
		if(!markerExists || (markerExists && getInstallCount() == 0)) {
			m_installedTypeName = null;
		}

		if(m_instanceFilters != null && !m_instanceFilters.isEmpty()) {
			for(int i = 0; i < m_instanceFilters.size(); i++) {
				JDXObjectValue object = m_instanceFilters.get(i);
				if(object.getDebugTarget().equals(target)) {
					m_instanceFilters.remove(i);
					changed = true;
				}
			}
		}
		// fire change notification if required
		if(changed) {
			fireChanged();
		}
		// notification
		fireRemoved(target);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void removeRequests(final JDXDebugTarget target) throws CoreException {
		ArrayList<EventRequest> requests = (ArrayList)getRequests(target).clone();

		Iterator<EventRequest> iter = requests.iterator();
		EventRequest req;

		while(iter.hasNext()) {
			req = (EventRequest)iter.next();
			try {
				if(target.isAvailable() && !isExpired(req)) {
					EventRequestManager manager = target.getEventRequestManager();
					if(manager != null) {
						manager.deleteEventRequest(req);
					}
				}
			} catch(VMDisconnectedException e) {
				if(target.isAvailable()) {
					Plugin.log(e);
				}
			} catch(RuntimeException e) {
				target.internalError(e);
			} finally {
				deregisterRequest(req, target);
			}
		}
		m_requestsByTarget.remove(target);
	}

	protected void updateEnabledState(EventRequest request, JDXDebugTarget target) throws CoreException {
		internalUpdateEnabledState(request, isEnabled(), target);
	}

	protected void internalUpdateEnabledState(EventRequest request, boolean enabled, JDXDebugTarget target) {
		if(request.isEnabled() != enabled) {
			// change the enabled state
			try {
				// if the request has expired, do not disable.
				// BreakpointRequests that have expired cannot be deleted.
				if(!isExpired(request)) {
					request.setEnabled(enabled);
				}
			} catch(VMDisconnectedException e) {
			} catch(RuntimeException e) {
				target.internalError(e);
			}
		}
	}

	public boolean isExpired() throws CoreException {
		return ensureMarker().getAttribute(EXPIRED, false);
	}

	protected boolean isExpired(EventRequest request) {
		Boolean requestExpired = (Boolean)request.getProperty(EXPIRED);
		if(requestExpired == null) {
			return false;
		}
		return requestExpired.booleanValue();
	}

	public boolean isInstalled() throws CoreException {
		return ensureMarker().getAttribute(INSTALL_COUNT, 0) > 0;
	}

	protected void incrementInstallCount() throws CoreException {
		int count = getInstallCount();
		setAttribute(INSTALL_COUNT, count + 1);
	}

	public int getInstallCount() throws CoreException {
		return ensureMarker().getAttribute(INSTALL_COUNT, 0);
	}

	protected void decrementInstallCount() throws CoreException {
		int count = getInstallCount();
		if(count > 0) {
			setAttribute(INSTALL_COUNT, count - 1);
		}
		if(count == 1) {
			if(isExpired()) {
				// if breakpoint was auto-disabled, re-enable it
				setAttributes(m_expiredEnabledAttributes, new Object[] { Boolean.FALSE, Boolean.TRUE });
			}
		}
	}

	protected void setTypeName(String typeName) throws CoreException {
		setAttribute(TYPE_NAME, typeName);
	}

	public String getTypeName() throws CoreException {
		if(m_installedTypeName == null) {
			return ensureMarker().getAttribute(TYPE_NAME, null);
		}
		return m_installedTypeName;
	}

	private void configureAtStartup() throws CoreException {
		List<String> attributes = null;
		List<Object> values = null;

		if(isInstalled()) {
			attributes = new ArrayList<String>(3);
			values = new ArrayList<Object>(3);
			attributes.add(INSTALL_COUNT);
			values.add(new Integer(0));
		}

		if(isExpired()) {
			if(attributes == null) {
				attributes = new ArrayList<String>(3);
				values = new ArrayList<Object>(3);
			}

			attributes.add(EXPIRED);
			values.add(Boolean.FALSE);
			attributes.add(ENABLED);
			values.add(Boolean.TRUE);
		}
		if(attributes != null) {
			setAttributes(attributes.toArray(new String[attributes.size()]), values.toArray());
		}
	}

	public int getHitCount() throws CoreException {
		return ensureMarker().getAttribute(HIT_COUNT, -1);
	}

	public void setHitCount(int count) throws CoreException {
		if(getHitCount() != count) {
			if(!isEnabled() && count > -1) {
				setAttributes(new String[] { ENABLED, HIT_COUNT, EXPIRED }, new Object[] { Boolean.TRUE, new Integer(count), Boolean.FALSE });
			} else {
				setAttributes(new String[] { HIT_COUNT, EXPIRED }, new Object[] { new Integer(count), Boolean.FALSE });
			}
			recreate();
		}
	}

	protected String getMarkerMessage(int hitCount, int suspendPolicy) {
		StringBuffer buff = new StringBuffer();
		if(hitCount > 0) {
			buff.append(MessageFormat.format(JDXMessages.JDXBreakpoint___Hit_Count___0___1, new Object[] { Integer.toString(hitCount) }));
			buff.append(' ');
		}
		String suspendPolicyString;
		if(suspendPolicy == JDXBreakpoint.SUSPEND_THREAD) {
			suspendPolicyString = JDXMessages.JDXBreakpoint__suspend_policy__thread__1;
		} else {
			suspendPolicyString = JDXMessages.JDXBreakpoint__suspend_policy__VM__2;
		}
		buff.append(suspendPolicyString);
		return buff.toString();
	}

	public void setExpired(boolean expired) throws CoreException {
		setAttribute(EXPIRED, expired);
	}

	public int getSuspendPolicy() throws CoreException {
		return ensureMarker().getAttribute(SUSPEND_POLICY, JDXBreakpoint.SUSPEND_THREAD);
	}

	public void setSuspendPolicy(int suspendPolicy) throws CoreException {
		if(getSuspendPolicy() != suspendPolicy) {
			setAttributes(new String[] { SUSPEND_POLICY }, new Object[] { new Integer(suspendPolicy) });
		}
		recreate();
	}

	protected void fireAdding(JDXDebugTarget target) {
		JDXDebug.getDefault().fireBreakpointAdding(target, this);
	}

	protected void fireRemoved(JDXDebugTarget target) {
		JDXDebug.getDefault().fireBreakpointRemoved(target, this);
		setInstalledIn(target, false);
	}

	protected void fireInstalled(JDXDebugTarget target) {
		if(!isInstalledIn(target)) {
			JDXDebug.getDefault().fireBreakpointInstalled(target, this);
			setInstalledIn(target, true);
		}
	}

	protected boolean isInstalledIn(JDXDebugTarget target) {
		return m_installedTargets != null && m_installedTargets.contains(target);
	}

	protected void setInstalledIn(JDXDebugTarget target, boolean installed) {
		if(installed) {
			if(m_installedTargets == null) {
				m_installedTargets = new HashSet<JDXDebugTarget>();
			}
			m_installedTargets.add(target);
		} else {
			if(m_installedTargets != null) {
				m_installedTargets.remove(target);
			}
		}
	}

	public void setThreadFilter(JDXThread thread) throws CoreException {
		if(!(thread.getDebugTarget() instanceof JDXDebugTarget)) {
			return;
		}
		JDXDebugTarget target = (JDXDebugTarget)thread.getDebugTarget();
		if(thread != m_filteredThreadsByTarget.put(target, thread)) {
			recreate(target);
			fireChanged();
		}
	}

	@Override
	public void handleDebugEvents(DebugEvent[] events) {
		for(int i = 0; i < events.length; i++) {
			DebugEvent event = events[i];
			if(event.getKind() == DebugEvent.TERMINATE) {
				Object source = event.getSource();
				if(!(source instanceof JDXThread)) {
					return;
				}
				try {
					cleanupForThreadTermination((JDXThread)source);
				} catch(VMDisconnectedException exception) {
				}
			}
		}
	}

	protected void cleanupForThreadTermination(JDXThread thread) {
		JDXDebugTarget target = (JDXDebugTarget)thread.getDebugTarget();
		try {
			if(thread == getThreadFilter(target)) {
				removeThreadFilter(target);
			}
		} catch(CoreException exception) {
			Plugin.log(exception);
		}
	}

	protected abstract void setRequestThreadFilter(EventRequest request, ThreadReference thread);

	public JDXThread getThreadFilter(JDXDebugTarget target) {
		return m_filteredThreadsByTarget.get(target);
	}

	public JDXThread[] getThreadFilters() {
		JDXThread[] threads = null;
		Collection<JDXThread> values = m_filteredThreadsByTarget.values();
		threads = new JDXThread[values.size()];
		values.toArray(threads);
		return threads;
	}

	public void removeThreadFilter(JDXDebugTarget jdxTarget) throws CoreException {
		if(m_filteredThreadsByTarget.remove(jdxTarget) != null) {
			recreate(jdxTarget);
			fireChanged();
		}
	}

	protected boolean queryInstallListeners(JDXDebugTarget target, ReferenceType type) {
		JDXType jt = null;
		if(type != null) {
			jt = JDXType.createType(target, null, null, type);
		}
		return JDXDebug.getDefault().fireInstalling(target, this, jt);
	}

	public void addInstanceFilter(JDXObjectValue object) throws CoreException {
		if(m_instanceFilters == null) {
			m_instanceFilters = new ArrayList<JDXObjectValue>();
		}
		if(!m_instanceFilters.contains(object)) {
			m_instanceFilters.add(object);
			recreate(object.getJDXDebugTarget());
			fireChanged();
		}
	}

	protected void fireChanged() {
		if(markerExists()) {
			DebugPlugin.getDefault().getBreakpointManager().fireBreakpointChanged(this);
		}
	}

	public JDXObjectValue[] getInstanceFilters() {
		if(m_instanceFilters == null || m_instanceFilters.isEmpty()) {
			return m_emptyInstanceFilters;
		}
		return m_instanceFilters.toArray(new JDXObjectValue[m_instanceFilters.size()]);
	}

	public void removeInstanceFilter(JDXObjectValue object) throws CoreException {
		if(m_instanceFilters == null) {
			return;
		}
		if(m_instanceFilters.remove(object)) {
			recreate(object.getJDXDebugTarget());
			fireChanged();
		}
	}

	protected void recreate() throws CoreException {
		IDebugTarget[] targets = DebugPlugin.getDefault().getLaunchManager().getDebugTargets();
		for(int i = 0; i < targets.length; i++) {
			IDebugTarget target = targets[i];
			MultiStatus multiStatus = new MultiStatus(Plugin.getUniqueIdentifier(), JDXDebug.INTERNAL_ERROR, JDXMessages.JDXBreakpoint_Exception, null);
			JDXDebugTarget jdxTarget = (JDXDebugTarget)target.getAdapter(JDXDebugTarget.class);
			if(jdxTarget != null) {
				try {
					recreate(jdxTarget);
				} catch(CoreException e) {
					multiStatus.add(e.getStatus());
				}
			}
			if(!multiStatus.isOK()) {
				throw new CoreException(multiStatus);
			}
		}
	}

	protected void recreate(JDXDebugTarget target) throws CoreException {
		if(target.isAvailable() && target.getBreakpoints().contains(this)) {
			removeRequests(target);
			createRequests(target);
		}
	}

	@Override
	public void setEnabled(boolean enabled) throws CoreException {
		super.setEnabled(enabled);
		recreate();
	}

	public boolean supportsInstanceFilters() {
		return true;
	}

	public boolean supportsThreadFilters() {
		return true;
	}
}
