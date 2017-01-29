package org.zenframework.z8.pde.debug.breakpoints;

import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.zenframework.z8.pde.debug.model.JDXDebugTarget;

import com.sun.jdi.ClassType;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequest;

public class JDXMethodEntryBreakpoint extends JDXLineBreakpoint {
	private static final String JDX_METHOD_ENTRY_BREAKPOINT = "org.zenframework.z8.pde.jdxMethodEntryBreakpointMarker";
	private static final String METHOD_NAME = "org.zenframework.z8.pde.methodName";
	private static final String METHOD_SIGNATURE = "org.zenframework.z8.pde.methodSignature";

	private String m_methodName = null;
	private String m_methodSignature = null;

	public JDXMethodEntryBreakpoint() {
	}

	public JDXMethodEntryBreakpoint(final IResource resource, final String typeName, final String methodName, final String methodSignature, final int lineNumber, final int javaLineNumber, final int charStart, final int charEnd, final int hitCount, final boolean register,
			final Map<String, Object> attributes) throws CoreException {
		IWorkspaceRunnable wr = new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				setMarker(resource.createMarker(JDX_METHOD_ENTRY_BREAKPOINT));
				addLineBreakpointAttributes(attributes, getModelIdentifier(), true, lineNumber, javaLineNumber, charStart, charEnd);
				addMethodNameAndSignature(attributes, methodName, methodSignature);
				addTypeNameAndHitCount(attributes, typeName, hitCount);
				ensureMarker().setAttributes(attributes);
				register(register);
			}
		};
		run(getMarkerRule(resource), wr);
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

	public String getMethodName() {
		return m_methodName;
	}

	public String getMethodSignature() {
		return m_methodSignature;
	}

	@Override
	public void setMarker(IMarker marker) throws CoreException {
		super.setMarker(marker);
		m_methodName = marker.getAttribute(METHOD_NAME, null);
		m_methodSignature = marker.getAttribute(METHOD_SIGNATURE, null);
	}

	@Override
	protected EventRequest[] newRequests(JDXDebugTarget target, ReferenceType type) throws CoreException {
		try {
			if(type instanceof ClassType) {
				ClassType clazz = (ClassType)type;
				Method method = clazz.concreteMethodByName(getMethodName(), getMethodSignature());
				if(method == null) {
					return null;
				}
				Location location = method.location();
				if(location == null || location.codeIndex() == -1) {
					return null;
				}
				BreakpointRequest req = type.virtualMachine().eventRequestManager().createBreakpointRequest(location);
				configureRequest(req, target);
				return new EventRequest[] { req };
			}
			return null;
		} catch(RuntimeException e) {
			target.internalError(e);
			return null;
		}
	}
}
