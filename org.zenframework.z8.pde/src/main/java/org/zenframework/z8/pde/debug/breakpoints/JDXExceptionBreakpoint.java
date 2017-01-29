package org.zenframework.z8.pde.debug.breakpoints;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IBreakpoint;

import org.zenframework.z8.pde.Plugin;
import org.zenframework.z8.pde.debug.JDXMessages;
import org.zenframework.z8.pde.debug.model.JDXDebugTarget;
import org.zenframework.z8.pde.debug.model.JDXThread;

import com.sun.jdi.Location;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.ExceptionEvent;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.ExceptionRequest;

public class JDXExceptionBreakpoint extends JDXBreakpoint {
	private static final String JAVA_EXCEPTION_BREAKPOINT = "org.zenframework.z8.pde.jdxExceptionBreakpointMarker";
	protected static final String CAUGHT = "org.zenframework.z8.pde.caught";
	protected static final String UNCAUGHT = "org.zenframework.z8.pde.uncaught";
	protected static final String CHECKED = "org.zenframework.z8.pde.checked";
	protected static final String INCLUSION_FILTERS = "org.zenframework.z8.pde.inclusion_filters";
	protected static final String EXCLUSION_FILTERS = "org.zenframework.z8.pde.exclusion_filters";

	protected String m_exceptionName = null;
	protected String[] m_inclusionClassFilters = null;
	protected String[] m_exclusionClassFilters = null;

	public JDXExceptionBreakpoint() {
	}

	public JDXExceptionBreakpoint(final IResource resource, final String exceptionName, final boolean caught, final boolean uncaught, final boolean checked, final boolean add, final Map<String, Object> attributes) throws DebugException {
		IWorkspaceRunnable wr = new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				setMarker(resource.createMarker(JAVA_EXCEPTION_BREAKPOINT));
				attributes.put(IBreakpoint.ID, getModelIdentifier());
				attributes.put(TYPE_NAME, exceptionName);
				attributes.put(ENABLED, Boolean.TRUE);
				attributes.put(CAUGHT, new Boolean(caught));
				attributes.put(UNCAUGHT, new Boolean(uncaught));
				attributes.put(CHECKED, new Boolean(checked));
				ensureMarker().setAttributes(attributes);
				register(add);
			}
		};
		run(getMarkerRule(resource), wr);
	}

	@Override
	protected EventRequest[] newRequests(JDXDebugTarget target, ReferenceType type) throws CoreException {
		if(!isCaught() && !isUncaught()) {
			return null;
		}
		ExceptionRequest request = null;
		EventRequestManager manager = target.getEventRequestManager();
		if(manager == null) {
			target.requestFailed(JDXMessages.JDXExceptionBreakpoint_Unable_to_create_breakpoint_request___VM_disconnected__1, null);
		}
		try {
			request = manager.createExceptionRequest(type, isCaught(), isUncaught());
			configureRequest(request, target);
		} catch(VMDisconnectedException e) {
			if(target.isAvailable()) {
				Plugin.log(e);
			}
			return null;
		} catch(RuntimeException e) {
			target.internalError(e);
			return null;
		}
		return new EventRequest[] { request };
	}

	@Override
	public void setEnabled(boolean enabled) throws CoreException {
		if(enabled) {
			if(!(isCaught() || isUncaught())) {
				setAttributes(new String[] { CAUGHT, UNCAUGHT }, new Object[] { Boolean.TRUE, Boolean.TRUE });
			}
		}
		super.setEnabled(enabled);
	}

	protected void setCaughtAndUncaught(boolean caught, boolean uncaught) throws CoreException {
		Object[] values = new Object[] { new Boolean(caught), new Boolean(uncaught) };
		String[] attributes = new String[] { CAUGHT, UNCAUGHT };
		setAttributes(attributes, values);
	}

	public boolean isCaught() throws CoreException {
		return ensureMarker().getAttribute(CAUGHT, false);
	}

	public void setCaught(boolean caught) throws CoreException {
		if(caught == isCaught()) {
			return;
		}
		setAttribute(CAUGHT, caught);
		if(caught && !isEnabled()) {
			setEnabled(true);
		} else if(!(caught || isUncaught())) {
			setEnabled(false);
		}
		recreate();
	}

	public boolean isUncaught() throws CoreException {
		return ensureMarker().getAttribute(UNCAUGHT, false);
	}

	public void setUncaught(boolean uncaught) throws CoreException {
		if(uncaught == isUncaught()) {
			return;
		}
		setAttribute(UNCAUGHT, uncaught);
		if(uncaught && !isEnabled()) {
			setEnabled(true);
		} else if(!(uncaught || isCaught())) {
			setEnabled(false);
		}
		recreate();
	}

	public boolean isChecked() throws CoreException {
		return ensureMarker().getAttribute(CHECKED, false);
	}

	@Override
	protected void setRequestThreadFilter(EventRequest request, ThreadReference thread) {
		((ExceptionRequest)request).addThreadFilter(thread);
	}

	@Override
	public boolean handleBreakpointEvent(Event event, JDXDebugTarget target, JDXThread thread) {
		if(event instanceof ExceptionEvent) {
			setExceptionName(((ExceptionEvent)event).exception().type().name());
			if(getExclusionClassFilters().length > 1 || getInclusionClassFilters().length > 1 || (getExclusionClassFilters().length + getInclusionClassFilters().length) >= 2 || filtersIncludeDefaultPackage(m_inclusionClassFilters)
					|| filtersIncludeDefaultPackage(m_exclusionClassFilters)) {
				Location location = ((ExceptionEvent)event).location();
				String typeName = location.declaringType().name();
				boolean defaultPackage = typeName.indexOf('.') == -1;
				boolean included = true;
				String[] filters = getInclusionClassFilters();
				if(filters.length > 0) {
					included = matchesFilters(filters, typeName, defaultPackage);
				}
				boolean excluded = false;
				filters = getExclusionClassFilters();
				if(filters.length > 0) {
					excluded = matchesFilters(filters, typeName, defaultPackage);
				}
				if(included && !excluded) {
					return !suspend(thread);
				}
				return true;
			}
			return !suspend(thread);
		}
		return true;
	}

	protected boolean filtersIncludeDefaultPackage(String[] filters) {
		for(int i = 0; i < filters.length; i++) {
			if(filters[i].length() == 0 || (filters[i].indexOf('.') == -1)) {
				return true;
			}
		}
		return false;
	}

	protected boolean matchesFilters(String[] filters, String typeName, boolean defaultPackage) {
		for(int i = 0; i < filters.length; i++) {
			String filter = filters[i];
			if(defaultPackage && filter.length() == 0) {
				return true;
			}
			filter = filter.replaceAll("\\.", "\\\\.");
			filter = filter.replaceAll("\\*", "\\.\\*");
			Pattern pattern = Pattern.compile(filter);
			if(pattern.matcher(typeName).find()) {
				return true;
			}
		}
		return false;
	}

	protected void setExceptionName(String name) {
		m_exceptionName = name;
	}

	public String getExceptionTypeName() {
		return m_exceptionName;
	}

	@Override
	protected void configureRequest(EventRequest eRequest, JDXDebugTarget target) throws CoreException {
		String[] iFilters = getInclusionClassFilters();
		String[] eFilters = getExclusionClassFilters();
		ExceptionRequest request = (ExceptionRequest)eRequest;
		if(iFilters.length == 1) {
			if(eFilters.length == 0) {
				request.addClassFilter(iFilters[0]);
			}
		} else if(eFilters.length == 1) {
			if(iFilters.length == 0) {
				request.addClassExclusionFilter(eFilters[0]);
			}
		}
		super.configureRequest(eRequest, target);
	}

	protected String serializeList(String[] list) {
		if(list == null) {
			return "";
		}

		Set<String> set = new HashSet<String>(list.length);

		StringBuffer buffer = new StringBuffer();

		for(int i = 0; i < list.length; i++) {
			if(i > 0) {
				buffer.append(',');
			}

			String pattern = list[i];

			if(!set.contains(pattern)) {
				if(pattern.length() == 0) {
					pattern = ".";
				}
				buffer.append(pattern);
			}
		}
		return buffer.toString();
	}

	protected String[] parseList(String listString) {
		List<String> list = new ArrayList<String>(10);
		StringTokenizer tokenizer = new StringTokenizer(listString, ",");
		while(tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			if(token.equals(".")) {
				token = "";
			}
			list.add(token);
		}
		return list.toArray(new String[list.size()]);
	}

	protected String[] getInclusionClassFilters() {
		if(m_inclusionClassFilters == null) {
			try {
				m_inclusionClassFilters = parseList(ensureMarker().getAttribute(INCLUSION_FILTERS, ""));
			} catch(CoreException ce) {
				m_inclusionClassFilters = new String[] {};
			}
		}
		return m_inclusionClassFilters;
	}

	protected void setInclusionClassFilters(String[] filters) {
		m_inclusionClassFilters = filters;
	}

	protected String[] getExclusionClassFilters() {
		if(m_exclusionClassFilters == null) {
			try {
				m_exclusionClassFilters = parseList(ensureMarker().getAttribute(EXCLUSION_FILTERS, ""));
			} catch(CoreException ce) {
				m_exclusionClassFilters = new String[] {};
			}
		}
		return m_exclusionClassFilters;
	}

	protected void setExclusionClassFilters(String[] filters) {
		m_exclusionClassFilters = filters;
	}

	@Override
	protected boolean installableReferenceType(ReferenceType type, JDXDebugTarget target) throws CoreException {
		String installableType = getTypeName();
		String queriedType = type.name();
		if(installableType == null || queriedType == null) {
			return false;
		}
		if(installableType.equals(queriedType)) {
			return queryInstallListeners(target, type);
		}
		return false;
	}

	public String[] getExclusionFilters() {
		return getExclusionClassFilters();
	}

	public String[] getInclusionFilters() {
		return getInclusionClassFilters();
	}

	public void setExclusionFilters(String[] filters) throws CoreException {
		String serializedFilters = serializeList(filters);
		if(serializedFilters.equals(ensureMarker().getAttribute(EXCLUSION_FILTERS, ""))) {
			// no change
			return;
		}
		setExclusionClassFilters(filters);
		setAttribute(EXCLUSION_FILTERS, serializedFilters);
		recreate();
	}

	public void setInclusionFilters(String[] filters) throws CoreException {
		String serializedFilters = serializeList(filters);
		if(serializedFilters.equals(ensureMarker().getAttribute(INCLUSION_FILTERS, ""))) {
			// no change
			return;
		}
		setInclusionClassFilters(filters);
		setAttribute(INCLUSION_FILTERS, serializedFilters);
		recreate();
	}

	@Override
	protected void addInstanceFilter(EventRequest request, ObjectReference object) {
		if(request instanceof ExceptionRequest) {
			((ExceptionRequest)request).addInstanceFilter(object);
		}
	}
}
