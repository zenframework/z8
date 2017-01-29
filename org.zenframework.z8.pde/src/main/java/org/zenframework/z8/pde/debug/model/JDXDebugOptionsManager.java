package org.zenframework.z8.pde.debug.model;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointsListener;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.sourcelookup.ISourceLookupResult;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ILabelProvider;

import org.zenframework.z8.pde.Plugin;
import org.zenframework.z8.pde.debug.JDXDebug;
import org.zenframework.z8.pde.debug.JDXMessages;
import org.zenframework.z8.pde.debug.JDXPreferences;
import org.zenframework.z8.pde.debug.breakpoints.JDXBreakpoint;
import org.zenframework.z8.pde.debug.breakpoints.JDXBreakpointListener;
import org.zenframework.z8.pde.debug.breakpoints.JDXExceptionBreakpoint;
import org.zenframework.z8.pde.debug.breakpoints.JDXLineBreakpoint;
import org.zenframework.z8.pde.debug.breakpoints.JDXMethodBreakpoint;
import org.zenframework.z8.pde.debug.breakpoints.JDXMethodEntryBreakpoint;
import org.zenframework.z8.pde.debug.breakpoints.JDXWatchpoint;
import org.zenframework.z8.pde.debug.model.type.JDXType;

public class JDXDebugOptionsManager implements IDebugEventSetListener, IPropertyChangeListener, JDXBreakpointListener, ILaunchListener, IBreakpointsListener {
	protected static JDXDebugOptionsManager m_optionsManager = null;
	protected JDXExceptionBreakpoint m_suspendOnExceptionBreakpoint = null;
	protected static ILabelProvider m_labelProvider = DebugUITools.newDebugModelPresentation();

	private static final int ADDED = 0;
	private static final int REMOVED = 1;
	private static final int CHANGED = 2;

	private boolean m_activated = false;

	class InitJob extends Job {
		public InitJob() {
			super(JDXMessages.JDXDebugOptionsManager_0);
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			MultiStatus status = new MultiStatus(Plugin.getUniqueIdentifier(), JDXDebug.INTERNAL_ERROR, JDXMessages.JDXDebugOptionsManager_1, null);

			try {
				JDXExceptionBreakpoint bp = JDXDebugModel.createExceptionBreakpoint(ResourcesPlugin.getWorkspace().getRoot(), "java.lang.Throwable", false, true, false, false, null);
				bp.setPersisted(false);
				setSuspendOnUncaughtExceptionBreakpoint(bp);
			} catch(CoreException e) {
				status.add(e.getStatus());
			}
			if(status.getChildren().length == 0) {
				return Status.OK_STATUS;
			}
			return status;
		}
	}

	private JDXDebugOptionsManager() {
	}

	public static JDXDebugOptionsManager getDefault() {
		if(m_optionsManager == null) {
			m_optionsManager = new JDXDebugOptionsManager();
		}
		return m_optionsManager;
	}

	public void startup() {
		DebugPlugin debugPlugin = DebugPlugin.getDefault();
		debugPlugin.getLaunchManager().addLaunchListener(this);
		debugPlugin.getBreakpointManager().addBreakpointListener(this);
	}

	public void shutdown() {
		DebugPlugin debugPlugin = DebugPlugin.getDefault();
		debugPlugin.removeDebugEventListener(this);
		debugPlugin.getLaunchManager().removeLaunchListener(this);
		debugPlugin.getBreakpointManager().removeBreakpointListener(this);
		if(!Plugin.getDefault().isShuttingDown()) {
			Plugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
		}
		JDXDebugModel.removeBreakpointListener(this);
		System.getProperties().remove(Plugin.getUniqueIdentifier() + ".debuggerActive");
	}

	protected void initializeProblemHandling() {
		InitJob job = new InitJob();
		job.setSystem(true);
		job.schedule();
	}

	protected void notifyTargets(IBreakpoint breakpoint, int kind) {
		IDebugTarget[] targets = DebugPlugin.getDefault().getLaunchManager().getDebugTargets();
		for(int i = 0; i < targets.length; i++) {
			if(targets[i] instanceof JDXDebugTarget) {
				JDXDebugTarget target = (JDXDebugTarget)targets[i];
				notifyTarget(target, breakpoint, kind);
			}
		}
	}

	protected void notifyTarget(JDXDebugTarget target, IBreakpoint breakpoint, int kind) {
		switch(kind) {
		case ADDED:
			target.breakpointAdded(breakpoint);
			break;
		case REMOVED:
			target.breakpointRemoved(breakpoint, null);
			break;
		case CHANGED:
			target.breakpointChanged(breakpoint, null);
			break;
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		if(event.getProperty().equals(JDXPreferences.PREF_SUSPEND_ON_UNCAUGHT_EXCEPTIONS)) {
			IBreakpoint breakpoint = getSuspendOnUncaughtExceptionBreakpoint();
			if(breakpoint != null) {
				int kind = REMOVED;
				if(isSuspendOnUncaughtExceptions()) {
					kind = ADDED;
				}
				notifyTargets(breakpoint, kind);
			}
		}
	}

	protected void setEnabled(IBreakpoint breakpoint, boolean enabled) {
		try {
			breakpoint.setEnabled(enabled);
			notifyTargets(breakpoint, CHANGED);
		} catch(CoreException e) {
			Plugin.log(e);
		}
	}

	protected boolean isSuspendOnCompilationErrors() {
		return Plugin.getDefault().getPreferenceStore().getBoolean(JDXPreferences.PREF_SUSPEND_ON_COMPILATION_ERRORS);
	}

	protected boolean isSuspendOnUncaughtExceptions() {
		return Plugin.getDefault().getPreferenceStore().getBoolean(JDXPreferences.PREF_SUSPEND_ON_UNCAUGHT_EXCEPTIONS);
	}

	protected void setSuspendOnUncaughtExceptionBreakpoint(JDXExceptionBreakpoint breakpoint) {
		m_suspendOnExceptionBreakpoint = breakpoint;
	}

	public JDXExceptionBreakpoint getSuspendOnUncaughtExceptionBreakpoint() {
		return m_suspendOnExceptionBreakpoint;
	}

	public static String[] parseList(String listString) {
		List<String> list = new ArrayList<String>(10);
		StringTokenizer tokenizer = new StringTokenizer(listString, ",");
		while(tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			list.add(token);
		}
		return list.toArray(new String[list.size()]);
	}

	public static String serializeList(String[] list) {
		if(list == null) {
			return "";
		}
		StringBuffer buffer = new StringBuffer();
		for(int i = 0; i < list.length; i++) {
			if(i > 0) {
				buffer.append(',');
			}
			buffer.append(list[i]);
		}
		return buffer.toString();
	}

	@Override
	public void handleDebugEvents(DebugEvent[] events) {
		for(int i = 0; i < events.length; i++) {
			DebugEvent event = events[i];
			if(event.getKind() == DebugEvent.CREATE) {
				Object source = event.getSource();
				if(source instanceof JDXDebugTarget) {
					JDXDebugTarget javaTarget = (JDXDebugTarget)source;

					if(isSuspendOnUncaughtExceptions()) {
						notifyTarget(javaTarget, getSuspendOnUncaughtExceptionBreakpoint(), ADDED);
					}
				}
			}
		}
	}

	@Override
	public void addingBreakpoint(JDXDebugTarget target, JDXBreakpoint breakpoint) {
	}

	@Override
	public int installingBreakpoint(JDXDebugTarget target, JDXBreakpoint breakpoint, JDXType type) {
		return DONT_CARE;
	}

	@Override
	public int breakpointHit(JDXThread thread, JDXBreakpoint breakpoint) {
		if(breakpoint == getSuspendOnUncaughtExceptionBreakpoint()) {
			try {
				if(getProblem(thread) != null) {
					return DONT_SUSPEND;
				}
			} catch(DebugException e) {
				Plugin.log(e);
			}
			return SUSPEND;
		}
		return DONT_CARE;
	}

	private IMarker getProblem(JDXThread thread) throws DebugException {
		JDXStackFrame frame = (JDXStackFrame)thread.getTopStackFrame();

		if(frame != null) {
			return getProblem(frame);
		}
		return null;
	}

	@Override
	public void breakpointInstalled(JDXDebugTarget target, JDXBreakpoint breakpoint) {
	}

	@Override
	public void breakpointRemoved(JDXDebugTarget target, JDXBreakpoint breakpoint) {
	}

	protected IMarker getProblem(JDXStackFrame frame) {
		ILaunch launch = frame.getLaunch();

		if(launch != null) {
			ISourceLookupResult result = DebugUITools.lookupSource(frame, null);
			Object sourceElement = result.getSourceElement();
			if(sourceElement instanceof IResource) {
				try {
					IResource resource = (IResource)sourceElement;
					IMarker[] markers = resource.findMarkers("org.eclipse.jdt.core.problem", true, IResource.DEPTH_INFINITE);
					int line = frame.getLineNumber();
					for(int i = 0; i < markers.length; i++) {
						IMarker marker = markers[i];
						if(marker.getAttribute(IMarker.LINE_NUMBER, -1) == line) {
							return marker;
						}
					}
				} catch(CoreException e) {
				}
			}
		}
		return null;
	}

	private void activate() {
		if(m_activated) {
			return;
		}
		m_activated = true;
		initializeProblemHandling();
		DebugPlugin.getDefault().addDebugEventListener(this);
		JDXDebugModel.addBreakpointListener(this);
	}

	@Override
	public void launchAdded(ILaunch launch) {
		launchChanged(launch);
	}

	@Override
	public void launchChanged(ILaunch launch) {
		activate();
		DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(this);
	}

	@Override
	public void launchRemoved(ILaunch launch) {
	}

	@Override
	public void breakpointsAdded(final IBreakpoint[] breakpoints) {
		List<IBreakpoint> update = new ArrayList<IBreakpoint>();
		for(int i = 0; i < breakpoints.length; i++) {
			IBreakpoint breakpoint = breakpoints[i];
			try {
				if(breakpoint instanceof JDXBreakpoint && breakpoint.getMarker().getAttribute(IMarker.MESSAGE) == null) {
					update.add(breakpoint);
				}
			} catch(CoreException e) {
				Plugin.log(e);
			}
		}
		if(!update.isEmpty()) {
			updateBreakpointMessages(update.toArray(new IBreakpoint[update.size()]));
		}
	}

	private void updateBreakpointMessages(final IBreakpoint[] breakpoints) {
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				for(int i = 0; i < breakpoints.length; i++) {
					IBreakpoint breakpoint = breakpoints[i];
					if(breakpoint instanceof JDXBreakpoint) {
						String info = m_labelProvider.getText(breakpoint);
						String type = JDXMessages.JDXDebugOptionsManager_Breakpoint___1;

						if(breakpoint instanceof JDXMethodBreakpoint || breakpoint instanceof JDXMethodEntryBreakpoint) {
							type = JDXMessages.JDXDebugOptionsManager_Method_breakpoint___2;
						} else if(breakpoint instanceof JDXWatchpoint) {
							type = JDXMessages.JDXDebugOptionsManager_Watchpoint___3;
						} else if(breakpoint instanceof JDXLineBreakpoint) {
							type = JDXMessages.JDXDebugOptionsManager_Line_breakpoint___4;
						}
						breakpoint.getMarker().setAttribute(IMarker.MESSAGE, type + info);
					}
				}
			}
		};
		try {
			ResourcesPlugin.getWorkspace().run(runnable, null, 0, null);
		} catch(CoreException e) {
			Plugin.log(e);
		}
	}

	@Override
	public void breakpointsChanged(IBreakpoint[] breakpoints, IMarkerDelta[] deltas) {
		updateBreakpointMessages(breakpoints);
	}

	@Override
	public void breakpointsRemoved(IBreakpoint[] breakpoints, IMarkerDelta[] deltas) {
	}
}
