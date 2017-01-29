package org.zenframework.z8.pde.debug.breakpoints;

import java.text.MessageFormat;
import java.util.HashMap;
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

public class JDXTargetPatternBreakpoint extends JDXLineBreakpoint {
	private static final String TARGET_PATTERN_BREAKPOINT = "org.zenframework.z8.pde.jdxTargetPatternBreakpointMarker";
	private HashMap<JDXDebugTarget, String> m_patterns;

	public JDXTargetPatternBreakpoint() {
	}

	public JDXTargetPatternBreakpoint(IResource resource, String sourceName, int lineNumber, int javaLineNumber, int charStart, int charEnd, int hitCount, boolean add, Map<String, Object> attributes) throws DebugException {
		this(resource, sourceName, lineNumber, javaLineNumber, charStart, charEnd, hitCount, add, attributes, TARGET_PATTERN_BREAKPOINT);
	}

	public JDXTargetPatternBreakpoint(final IResource resource, final String sourceName, final int lineNumber, final int javaLineNumber, final int charStart, final int charEnd, final int hitCount, final boolean add, final Map<String, Object> attributes, final String markerType)
			throws DebugException {
		IWorkspaceRunnable wr = new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				setMarker(resource.createMarker(markerType));
				addLineBreakpointAttributes(attributes, getModelIdentifier(), true, lineNumber, javaLineNumber, charStart, charEnd);
				addSourceNameAndHitCount(attributes, sourceName, hitCount);
				ensureMarker().setAttributes(attributes);
				register(add);
			}
		};
		run(getMarkerRule(resource), wr);
	}

	@Override
	public void addToTarget(JDXDebugTarget target) throws CoreException {
		fireAdding(target);
		String referenceTypeName = getPattern(target);
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
			target.requestFailed(JDXMessages.JDXTargetPatternBreakpoint_Unable_to_add_breakpoint___VM_disconnected__1, null);
		}

		List<ReferenceType> classes = vm.allClasses();
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

	protected String getReferenceTypeName() {
		String name = "*";
		try {
			name = getSourceName();
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
				// unable to compare
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

		String pattern = getPattern(target);
		String queriedType = type.name();
		if(pattern == null || queriedType == null) {
			return false;
		}
		if(queriedType.startsWith(pattern)) {
			return queryInstallListeners(target, type);
		}
		return false;
	}

	protected void addSourceNameAndHitCount(Map<String, Object> attributes, String sourceName, int hitCount) {
		if(sourceName != null) {
			attributes.put(SOURCE_NAME, sourceName);
		}
		if(hitCount > 0) {
			attributes.put(HIT_COUNT, new Integer(hitCount));
			attributes.put(EXPIRED, Boolean.FALSE);
		}
	}

	public String getPattern(JDXDebugTarget target) {
		if(m_patterns != null) {
			return m_patterns.get(target);
		}
		return null;
	}

	public void setPattern(JDXDebugTarget target, String pattern) throws CoreException {
		if(m_patterns == null) {
			m_patterns = new HashMap<JDXDebugTarget, String>(2);
		}
		String oldPattern = getPattern(target);
		m_patterns.put(target, pattern);
		if(oldPattern != null && !oldPattern.equals(pattern)) {
			recreate(target);
			fireChanged();
		}
	}

	public String getSourceName() throws CoreException {
		return (String)ensureMarker().getAttribute(SOURCE_NAME);
	}

	@Override
	public void removeFromTarget(JDXDebugTarget target) throws CoreException {
		m_patterns.remove(target);
		super.removeFromTarget(target);
	}
}
