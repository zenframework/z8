package org.zenframework.z8.pde.debug.breakpoints;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IStatusHandler;

import org.zenframework.z8.pde.Plugin;
import org.zenframework.z8.pde.debug.JDXMessages;
import org.zenframework.z8.pde.debug.model.JDXDebugTarget;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassNotPreparedException;
import com.sun.jdi.Location;
import com.sun.jdi.NativeMethodException;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.VirtualMachine;

public class JDXStratumLineBreakpoint extends JDXLineBreakpoint {
	private static final String PATTERN = "org.zenframework.z8.pde.pattern";
	private static final String STRATUM = "org.zenframework.z8.pde.stratum";
	private static final String SOURCE_PATH = "org.zenframework.z8.pde.source_path";
	private static final String STRATUM_BREAKPOINT = "v.pde.jdxStratumLineBreakpointMarker";

	private String[] m_typeNamePatterns;
	private String[] m_suffix;
	private String[] m_prefix;

	public JDXStratumLineBreakpoint() {
	}

	public JDXStratumLineBreakpoint(IResource resource, String stratum, String sourceName, String sourcePath, String classNamePattern, int lineNumber, int javaLineNumber, int charStart, int charEnd, int hitCount, boolean register, Map<String, Object> attributes)
			throws DebugException {
		this(resource, stratum, sourceName, sourcePath, classNamePattern, lineNumber, javaLineNumber, charStart, charEnd, hitCount, register, attributes, STRATUM_BREAKPOINT);
	}

	protected JDXStratumLineBreakpoint(final IResource resource, final String stratum, final String sourceName, final String sourcePath, final String classNamePattern, final int lineNumber, final int javaLineNumber, final int charStart, final int charEnd, final int hitCount,
			final boolean register, final Map<String, Object> attributes, final String markerType) throws DebugException {
		IWorkspaceRunnable wr = new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				setMarker(resource.createMarker(markerType));
				String pattern = classNamePattern;
				if(pattern != null && pattern.length() == 0) {
					pattern = null;
				}
				addLineBreakpointAttributes(attributes, getModelIdentifier(), true, lineNumber, javaLineNumber, charStart, charEnd);
				addStratumPatternAndHitCount(attributes, stratum, sourceName, sourcePath, pattern, hitCount);
				ensureMarker().setAttributes(attributes);
				register(register);
			}
		};
		run(getMarkerRule(resource), wr);
	}

	protected void addStratumPatternAndHitCount(Map<String, Object> attributes, String stratum, String sourceName, String sourcePath, String pattern, int hitCount) {
		attributes.put(PATTERN, pattern);
		attributes.put(STRATUM, stratum);
		if(sourceName != null) {
			attributes.put(SOURCE_NAME, sourceName);
		}
		if(sourcePath != null) {
			attributes.put(SOURCE_PATH, sourcePath);
		}
		if(hitCount > 0) {
			attributes.put(HIT_COUNT, new Integer(hitCount));
			attributes.put(EXPIRED, Boolean.FALSE);
		}
	}

	@Override
	protected boolean installableReferenceType(ReferenceType type, JDXDebugTarget target) throws CoreException {
		String typeName = type.name();

		if(!validType(typeName)) {
			return false;

		}
		String stratum = getStratum();
		String bpSourceName = getSourceName();

		if(bpSourceName != null) {
			List<String> sourceNames;

			try {
				sourceNames = type.sourceNames(stratum);
			} catch(AbsentInformationException e1) {
				return false;
			} catch(VMDisconnectedException e) {
				if(!target.isAvailable()) {
					return false;
				}
				throw e;
			}
			if(!containsMatch(sourceNames, bpSourceName)) {
				return false;
			}
		}

		String bpSourcePath = getSourcePath();

		if(bpSourcePath != null) {
			List<String> sourcePaths;

			try {
				sourcePaths = type.sourcePaths(stratum);
			} catch(AbsentInformationException e1) {
				return false;
			} catch(VMDisconnectedException e) {
				if(!target.isAvailable()) {
					return false;
				}
				throw e;
			}
			if(!containsMatch(sourcePaths, bpSourcePath)) {
				return false;
			}
		}
		return queryInstallListeners(target, type);
	}

	private boolean containsMatch(List<String> strings, String key) {
		for(Iterator<String> iter = strings.iterator(); iter.hasNext();) {
			if(iter.next().equals(key)) {
				return true;
			}
		}
		return false;
	}

	private boolean validType(String typeName) throws CoreException {
		String[] patterns = getTypeNamePatterns();
		for(int i = 0; i < patterns.length; i++) {
			if(m_suffix[i] != null) {
				if(m_suffix[i].length() == 0) {
					return true;
				}
				if(typeName.endsWith(m_suffix[i]))
					return true;
			} else if(m_prefix[i] != null) {
				if(typeName.startsWith(m_prefix[i]))
					return true;
			} else {
				if(typeName.startsWith(patterns[i]))
					return true;
			}
		}
		return false;
	}

	@Override
	protected List<Location> determineLocations(int lineNumber, ReferenceType type, JDXDebugTarget target) {
		List<Location> locations;
		String sourcePath;

		try {
			locations = type.locationsOfLine(getStratum(), getSourceName(), lineNumber);
			sourcePath = getSourcePath();
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
		} catch(CoreException e) {
			Plugin.log(e);
			return null;
		}
		if(sourcePath == null) {
			if(locations.size() > 0) {
				return locations;
			}
		} else {
			for(ListIterator<Location> iter = locations.listIterator(); iter.hasNext();) {
				Location location = iter.next();
				try {
					if(!sourcePath.equals(location.sourcePath())) {
						iter.remove();
					}
				} catch(AbsentInformationException e1) {
					// nothing to do;
				}
			}
			if(locations.size() > 0) {
				return locations;
			}
		}
		return null;
	}

	public String getPattern() throws CoreException {
		return ensureMarker().getAttribute(PATTERN, "*");
	}

	public String getSourceName() throws CoreException {
		return (String)ensureMarker().getAttribute(SOURCE_NAME);
	}

	public String getStratum() throws CoreException {
		return (String)ensureMarker().getAttribute(STRATUM);
	}

	public String getSourcePath() throws CoreException {
		return (String)ensureMarker().getAttribute(SOURCE_PATH);
	}

	@Override
	protected void createRequests(JDXDebugTarget target) throws CoreException {
		if(target.isTerminated() || shouldSkipBreakpoint()) {
			return;
		}
		String[] patterns = null;
		try {
			patterns = getTypeNamePatterns();
		} catch(CoreException e1) {
			Plugin.log(e1);
			return;
		}
		for(int i = 0; i < patterns.length; i++) {
			String classPrepareTypeName = patterns[i];
			registerRequest(target.createClassPrepareRequest(classPrepareTypeName), target);
		}
		// create breakpoint requests for each class currently loaded
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

			while(iter.hasNext()) {
				ReferenceType type = iter.next();

				if(installableReferenceType(type, target)) {
					createRequest(target, type);
				}
			}
		}
	}

	public synchronized String[] getTypeNamePatterns() throws CoreException {
		if(m_typeNamePatterns != null)
			return m_typeNamePatterns;
		String patterns = getPattern();
		m_typeNamePatterns = patterns.split(",");
		m_suffix = new String[m_typeNamePatterns.length];
		m_prefix = new String[m_typeNamePatterns.length];
		for(int i = 0; i < m_typeNamePatterns.length; i++) {
			m_typeNamePatterns[i] = m_typeNamePatterns[i].trim();
			String pattern = m_typeNamePatterns[i];
			if(pattern.charAt(0) == '*') {
				if(pattern.length() > 1) {
					m_suffix[i] = pattern.substring(1);
				} else {
					m_suffix[i] = "";
				}
			} else if(pattern.charAt(pattern.length() - 1) == '*') {
				m_prefix[i] = pattern.substring(0, pattern.length() - 1);
			}
		}
		return m_typeNamePatterns;
	}
}
