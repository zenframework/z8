package org.zenframework.z8.pde.debug.model;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;

import org.zenframework.z8.pde.Plugin;
import org.zenframework.z8.pde.debug.JDXDebug;
import org.zenframework.z8.pde.debug.breakpoints.JDXBreakpointListener;
import org.zenframework.z8.pde.debug.breakpoints.JDXClassPrepareBreakpoint;
import org.zenframework.z8.pde.debug.breakpoints.JDXExceptionBreakpoint;
import org.zenframework.z8.pde.debug.breakpoints.JDXLineBreakpoint;
import org.zenframework.z8.pde.debug.breakpoints.JDXMethodBreakpoint;
import org.zenframework.z8.pde.debug.breakpoints.JDXMethodEntryBreakpoint;
import org.zenframework.z8.pde.debug.breakpoints.JDXPatternBreakpoint;
import org.zenframework.z8.pde.debug.breakpoints.JDXStratumLineBreakpoint;
import org.zenframework.z8.pde.debug.breakpoints.JDXTargetPatternBreakpoint;
import org.zenframework.z8.pde.debug.breakpoints.JDXWatchpoint;

import com.sun.jdi.VirtualMachine;

@SuppressWarnings("deprecation")
public class JDXDebugModel {
	public static final String PREF_REQUEST_TIMEOUT = getModelIdentifier() + ".PREF_REQUEST_TIMEOUT";
	public static final int DEF_REQUEST_TIMEOUT = 3000;

	private JDXDebugModel() {
		super();
	}

	public static IDebugTarget newDebugTarget(ILaunch launch, VirtualMachine vm, String name, IProcess process, boolean allowTerminate, boolean allowDisconnect) {
		return newDebugTarget(launch, vm, name, process, allowTerminate, allowDisconnect, true);
	}

	public static IDebugTarget newDebugTarget(final ILaunch launch, final VirtualMachine vm, final String name, final IProcess process, final boolean allowTerminate, final boolean allowDisconnect, final boolean resume) {
		final JDXDebugTarget[] target = new JDXDebugTarget[1];

		IWorkspaceRunnable r = new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor m) {
				target[0] = new JDXDebugTarget(launch, vm, name, allowTerminate, allowDisconnect, process, resume);
			}
		};
		try {
			ResourcesPlugin.getWorkspace().run(r, null, 0, null);
		} catch(CoreException e) {
			Plugin.log(e);
		}
		return target[0];
	}

	public static String getModelIdentifier() {
		return "org.zenframework.z8.pde.DebugModelPresentation";
	}

	public static void addBreakpointListener(JDXBreakpointListener listener) {
		JDXDebug.getDefault().addBreakpointListener(listener);
	}

	public static void removeBreakpointListener(JDXBreakpointListener listener) {
		JDXDebug.getDefault().removeBreakpointListener(listener);
	}

	public static JDXLineBreakpoint createLineBreakpoint(IResource resource, String typeName, int lineNumber, int javaLineNumber, int charStart, int charEnd, int hitCount, boolean register, Map<String, Object> attributes) throws CoreException {
		if(attributes == null) {
			attributes = new HashMap<String, Object>(10);
		}
		return new JDXLineBreakpoint(resource, typeName, lineNumber, javaLineNumber, charStart, charEnd, hitCount, register, attributes);
	}

	public static JDXPatternBreakpoint createPatternBreakpoint(IResource resource, String sourceName, String pattern, int lineNumber, int javaLineNumber, int charStart, int charEnd, int hitCount, boolean register, Map<String, Object> attributes) throws CoreException {
		if(attributes == null) {
			attributes = new HashMap<String, Object>(10);
		}
		return new JDXPatternBreakpoint(resource, sourceName, pattern, lineNumber, javaLineNumber, charStart, charEnd, hitCount, register, attributes);
	}

	public static JDXStratumLineBreakpoint createStratumBreakpoint(IResource resource, String stratum, String sourceName, String sourcePath, String classNamePattern, int lineNumber, int javaLineNumber, int charStart, int charEnd, int hitCount, boolean register,
			Map<String, Object> attributes) throws CoreException {
		if(attributes == null) {
			attributes = new HashMap<String, Object>(10);
		}
		return new JDXStratumLineBreakpoint(resource, stratum, sourceName, sourcePath, classNamePattern, lineNumber, javaLineNumber, charStart, charEnd, hitCount, register, attributes);
	}

	public static JDXTargetPatternBreakpoint createTargetPatternBreakpoint(IResource resource, String sourceName, int lineNumber, int javaLineNumber, int charStart, int charEnd, int hitCount, boolean register, Map<String, Object> attributes) throws CoreException {
		if(attributes == null) {
			attributes = new HashMap<String, Object>(10);
		}
		return new JDXTargetPatternBreakpoint(resource, sourceName, lineNumber, javaLineNumber, charStart, charEnd, hitCount, register, attributes);
	}

	public static JDXExceptionBreakpoint createExceptionBreakpoint(IResource resource, String exceptionName, boolean caught, boolean uncaught, boolean checked, boolean register, Map<String, Object> attributes) throws CoreException {
		if(attributes == null) {
			attributes = new HashMap<String, Object>(10);
		}
		return new JDXExceptionBreakpoint(resource, exceptionName, caught, uncaught, checked, register, attributes);
	}

	public static JDXWatchpoint createWatchpoint(IResource resource, String typeName, String fieldName, int lineNumber, int javaLineNumber, int charStart, int charEnd, int hitCount, boolean register, Map<String, Object> attributes) throws CoreException {
		if(attributes == null) {
			attributes = new HashMap<String, Object>(10);
		}
		return new JDXWatchpoint(resource, typeName, fieldName, lineNumber, javaLineNumber, charStart, charEnd, hitCount, register, attributes);
	}

	public static JDXMethodBreakpoint createMethodBreakpoint(IResource resource, String typePattern, String methodName, String methodSignature, boolean entry, boolean exit, boolean nativeOnly, int lineNumber, int javaLineNumber, int charStart, int charEnd, int hitCount,
			boolean register, Map<String, Object> attributes) throws CoreException {
		if(attributes == null) {
			attributes = new HashMap<String, Object>(10);
		}
		return new JDXMethodBreakpoint(resource, typePattern, methodName, methodSignature, entry, exit, nativeOnly, lineNumber, javaLineNumber, charStart, charEnd, hitCount, register, attributes);
	}

	public static JDXMethodEntryBreakpoint createMethodEntryBreakpoint(IResource resource, String typeName, String methodName, String methodSignature, int lineNumber, int javaLineNumber, int charStart, int charEnd, int hitCount, boolean register, Map<String, Object> attributes)
			throws CoreException {
		if(attributes == null) {
			attributes = new HashMap<String, Object>(10);
		}
		return new JDXMethodEntryBreakpoint(resource, typeName, methodName, methodSignature, lineNumber, javaLineNumber, charStart, charEnd, hitCount, register, attributes);
	}

	public static JDXLineBreakpoint lineBreakpointExists(String typeName, int lineNumber) throws CoreException {
		String modelId = getModelIdentifier();
		String markerType = JDXLineBreakpoint.getMarkerType();
		IBreakpointManager manager = DebugPlugin.getDefault().getBreakpointManager();
		IBreakpoint[] breakpoints = manager.getBreakpoints(modelId);
		for(int i = 0; i < breakpoints.length; i++) {
			if(!(breakpoints[i] instanceof JDXLineBreakpoint)) {
				continue;
			}
			JDXLineBreakpoint breakpoint = (JDXLineBreakpoint)breakpoints[i];
			IMarker marker = breakpoint.getMarker();
			if(marker != null && marker.exists() && marker.getType().equals(markerType)) {
				String breakpointTypeName = breakpoint.getTypeName();
				if(breakpointTypeName.equals(typeName) || breakpointTypeName.startsWith(typeName + '$')) {
					if(breakpoint.getLineNumber() == lineNumber) {
						return breakpoint;
					}
				}
			}
		}
		return null;
	}

	public static JDXLineBreakpoint lineBreakpointExists(IResource resource, String typeName, int lineNumber) throws CoreException {
		String modelId = getModelIdentifier();
		String markerType = JDXLineBreakpoint.getMarkerType();
		IBreakpointManager manager = DebugPlugin.getDefault().getBreakpointManager();
		IBreakpoint[] breakpoints = manager.getBreakpoints(modelId);
		for(int i = 0; i < breakpoints.length; i++) {
			if(!(breakpoints[i] instanceof JDXLineBreakpoint)) {
				continue;
			}
			JDXLineBreakpoint breakpoint = (JDXLineBreakpoint)breakpoints[i];
			IMarker marker = breakpoint.getMarker();
			if(marker != null && marker.exists() && marker.getType().equals(markerType)) {
				String breakpointTypeName = breakpoint.getTypeName();
				if((breakpointTypeName.equals(typeName) || breakpointTypeName.startsWith(typeName + '$')) && breakpoint.getLineNumber() == lineNumber && resource.equals(marker.getResource())) {
					return breakpoint;
				}
			}
		}
		return null;
	}

	public static JDXClassPrepareBreakpoint createClassPrepareBreakpoint(IResource resource, String typeName, int memberType, int charStart, int charEnd, boolean register, Map<String, Object> attributes) throws CoreException {
		if(attributes == null) {
			attributes = new HashMap<String, Object>(10);
		}
		return new JDXClassPrepareBreakpoint(resource, typeName, memberType, charStart, charEnd, register, attributes);
	}

	public static Preferences getPreferences() {
		Plugin plugin = Plugin.getDefault();

		if(plugin != null) {
			return plugin.getPluginPreferences();
		}

		return null;
	}

	public static void savePreferences() {
		Plugin.getDefault().savePluginPreferences();
	}

}
