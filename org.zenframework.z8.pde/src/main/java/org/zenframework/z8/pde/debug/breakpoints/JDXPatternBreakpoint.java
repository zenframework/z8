package org.zenframework.z8.pde.debug.breakpoints;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugException;

import org.zenframework.z8.pde.Plugin;
import org.zenframework.z8.pde.debug.JDXMessages;
import org.zenframework.z8.pde.debug.model.JDXDebugTarget;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.VirtualMachine;

public class JDXPatternBreakpoint extends JDXLineBreakpoint {
	private static final String PATTERN_BREAKPOINT = "org.zenframework.z8.pde.jdxPatternBreakpointMarker";
	protected static final String PATTERN = "org.zenframework.z8.pde.pattern";

	public JDXPatternBreakpoint() {
	}

	public JDXPatternBreakpoint(IResource resource, String sourceName, String pattern, int lineNumber, int javaLineNumber, int charStart, int charEnd, int hitCount, boolean add, Map<String, Object> attributes) throws DebugException {
		this(resource, sourceName, pattern, lineNumber, javaLineNumber, charStart, charEnd, hitCount, add, attributes, PATTERN_BREAKPOINT);
	}

	public JDXPatternBreakpoint(final IResource resource, final String sourceName, final String pattern, final int lineNumber, final int javaLineNumber, final int charStart, final int charEnd, final int hitCount, final boolean add, final Map<String, Object> attributes,
			final String markerType) throws DebugException {
		IWorkspaceRunnable wr = new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				setMarker(resource.createMarker(markerType));
				addLineBreakpointAttributes(attributes, getModelIdentifier(), true, lineNumber, javaLineNumber, charStart, charEnd);
				addPatternAndHitCount(attributes, sourceName, pattern, hitCount);
				ensureMarker().setAttributes(attributes);
				register(add);
			}
		};
		run(getMarkerRule(resource), wr);
	}

	protected String getReferenceTypeName() {
		String name = "";
		try {
			name = getPattern();
		} catch(CoreException ce) {
			Plugin.log(ce);
		}
		return name;
	}

	@Override
	protected boolean installableReferenceType(ReferenceType type, JDXDebugTarget target) throws CoreException {
		if(getSourceName() != null) {
			String sourceName = null;
			try {
				sourceName = type.sourceName();
			} catch(AbsentInformationException e) {
			} catch(VMDisconnectedException e) {
				if(!target.isAvailable()) {
					return false;
				}
				target.targetRequestFailed(MessageFormat.format(JDXMessages.JDXPatternBreakpoint_exception_source_name, new Object[] { e.toString(), type.name() }), e);
				return false;
			} catch(RuntimeException e) {
				target.targetRequestFailed(MessageFormat.format(JDXMessages.JDXPatternBreakpoint_exception_source_name, new Object[] { e.toString(), type.name() }), e);
				return false;
			}

			if(sourceName != null) {
				if(!getSourceName().equalsIgnoreCase(sourceName)) {
					return false;
				}
			}
		}
		String pattern = getPattern();
		String queriedType = type.name();
		if(pattern == null || queriedType == null) {
			return false;
		}
		if(queriedType.startsWith(pattern)) {
			return queryInstallListeners(target, type);
		}
		return false;
	}

	/**
	 * Adds the class name pattern and hit count attributes to the given map.
	 */
	protected void addPatternAndHitCount(Map<String, Object> attributes, String sourceName, String pattern, int hitCount) {
		attributes.put(PATTERN, pattern);
		if(sourceName != null) {
			attributes.put(SOURCE_NAME, sourceName);
		}
		if(hitCount > 0) {
			attributes.put(HIT_COUNT, new Integer(hitCount));
			attributes.put(EXPIRED, Boolean.FALSE);
		}
	}

	public String getPattern() throws CoreException {
		return (String)ensureMarker().getAttribute(PATTERN);
	}

	public String getSourceName() throws CoreException {
		return (String)ensureMarker().getAttribute(SOURCE_NAME);
	}

	@Override
	protected void createRequests(JDXDebugTarget target) throws CoreException {
		if(target.isTerminated() || shouldSkipBreakpoint()) {
			return;
		}
		String referenceTypeName = getReferenceTypeName();

		if(referenceTypeName == null) {
			return;
		}
		String classPrepareTypeName = referenceTypeName;

		if(!referenceTypeName.endsWith("*")) {
			classPrepareTypeName = classPrepareTypeName + '*';
		}

		registerRequest(target.createClassPrepareRequest(classPrepareTypeName), target);

		VirtualMachine vm = target.getVM();

		if(vm == null) {
			target.requestFailed(JDXMessages.JDXPatternBreakpoint_Unable_to_add_breakpoint___VM_disconnected__1, null);
		}

		List<ReferenceType> classes = null;

		try {
			classes = vm.allClasses();
		} catch(RuntimeException e) {
			target.targetRequestFailed(JDXMessages.JDXPatternBreakpoint_0, e);
		}
		if(classes != null) {
			Iterator<ReferenceType> iter = classes.iterator();
			String typeName = null;
			ReferenceType type = null;
			while(iter.hasNext()) {
				type = iter.next();
				typeName = type.name();
				if(typeName != null && typeName.startsWith(referenceTypeName)) {
					createRequest(target, type);
				}
			}
		}
	}
}
