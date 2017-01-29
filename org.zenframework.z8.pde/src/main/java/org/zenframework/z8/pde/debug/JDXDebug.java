package org.zenframework.z8.pde.debug;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.debug.core.DebugException;

import org.zenframework.z8.pde.debug.breakpoints.JDXBreakpoint;
import org.zenframework.z8.pde.debug.breakpoints.JDXBreakpointListener;
import org.zenframework.z8.pde.debug.model.JDXDebugTarget;
import org.zenframework.z8.pde.debug.model.JDXThread;
import org.zenframework.z8.pde.debug.model.type.JDXType;

public class JDXDebug {
	static private JDXDebug m_instance;

	private List<JDXBreakpointListener> m_breakpointListeners;

	private static final int BREAKPOINT_ADDING = 1;
	private static final int BREAKPOINT_INSTALLED = 2;
	private static final int BREAKPOINT_REMOVED = 3;

	public static final int INTERNAL_ERROR = 120;

	private JDXDebug() {
		m_breakpointListeners = new ArrayList<JDXBreakpointListener>();
	}

	static public JDXDebug getDefault() {
		if(m_instance == null) {
			m_instance = new JDXDebug();
		}

		return m_instance;
	}

	public void addBreakpointListener(JDXBreakpointListener listener) {
		m_breakpointListeners.add(listener);
	}

	public void removeBreakpointListener(JDXBreakpointListener listener) {
		m_breakpointListeners.remove(listener);
	}

	JDXBreakpointListener[] getBreakpointListeners() {
		return m_breakpointListeners.toArray(new JDXBreakpointListener[m_breakpointListeners.size()]);
	}

	class BreakpointNotifier implements ISafeRunnable {
		private JDXDebugTarget fTarget;
		private JDXBreakpoint fBreakpoint;
		private int fKind;
		private JDXBreakpointListener fListener;

		@Override
		public void handleException(Throwable exception) {
		}

		@Override
		public void run() throws Exception {
			switch(fKind) {
			case BREAKPOINT_ADDING:
				fListener.addingBreakpoint(fTarget, fBreakpoint);
				break;
			case BREAKPOINT_INSTALLED:
				fListener.breakpointInstalled(fTarget, fBreakpoint);
				break;
			case BREAKPOINT_REMOVED:
				fListener.breakpointRemoved(fTarget, fBreakpoint);
				break;
			}
		}

		public void notify(JDXDebugTarget target, JDXBreakpoint breakpoint, int kind, DebugException exception) {
			fTarget = target;
			fBreakpoint = breakpoint;
			fKind = kind;

			JDXBreakpointListener[] listeners = getBreakpointListeners();
			for(int i = 0; i < listeners.length; i++) {
				fListener = listeners[i];
				SafeRunner.run(this);
			}
			fTarget = null;
			fBreakpoint = null;
			fListener = null;
		}
	}

	private BreakpointNotifier getBreakpointNotifier() {
		return new BreakpointNotifier();
	}

	public void fireBreakpointAdding(JDXDebugTarget target, JDXBreakpoint breakpoint) {
		getBreakpointNotifier().notify(target, breakpoint, BREAKPOINT_ADDING, null);
	}

	public void fireBreakpointInstalled(JDXDebugTarget target, JDXBreakpoint breakpoint) {
		getBreakpointNotifier().notify(target, breakpoint, BREAKPOINT_INSTALLED, null);
	}

	public void fireBreakpointRemoved(JDXDebugTarget target, JDXBreakpoint breakpoint) {
		getBreakpointNotifier().notify(target, breakpoint, BREAKPOINT_REMOVED, null);
	}

	private HitNotifier getHitNotifier() {
		return new HitNotifier();
	}

	class HitNotifier implements ISafeRunnable {
		private JDXThread fThread;
		private JDXBreakpoint fBreakpoint;
		private JDXBreakpointListener fListener;
		private int fSuspend;

		@Override
		public void handleException(Throwable exception) {
		}

		@Override
		public void run() throws Exception {
			fSuspend = fSuspend | fListener.breakpointHit(fThread, fBreakpoint);
		}

		public boolean notifyHit(JDXThread thread, JDXBreakpoint breakpoint) {
			fThread = thread;
			fBreakpoint = breakpoint;
			JDXBreakpointListener[] listeners = getBreakpointListeners();
			fSuspend = JDXBreakpointListener.DONT_CARE;
			for(int i = 0; i < listeners.length; i++) {
				fListener = listeners[i];
				SafeRunner.run(this);
			}
			fThread = null;
			fBreakpoint = null;
			fListener = null;
			return (fSuspend & JDXBreakpointListener.SUSPEND) > 0 || (fSuspend & JDXBreakpointListener.DONT_SUSPEND) == 0;
		}
	}

	public boolean fireBreakpointHit(JDXThread thread, JDXBreakpoint breakpoint) {
		return getHitNotifier().notifyHit(thread, breakpoint);
	}

	private InstallingNotifier getInstallingNotifier() {
		return new InstallingNotifier();
	}

	class InstallingNotifier implements ISafeRunnable {
		private JDXDebugTarget fTarget;
		private JDXBreakpoint fBreakpoint;
		private JDXType fType;
		private JDXBreakpointListener fListener;
		private int fInstall;

		@Override
		public void handleException(Throwable exception) {
		}

		@Override
		public void run() throws Exception {
			fInstall = fInstall | fListener.installingBreakpoint(fTarget, fBreakpoint, fType);
		}

		private void dispose() {
			fTarget = null;
			fBreakpoint = null;
			fType = null;
			fListener = null;
		}

		public boolean notifyInstalling(JDXDebugTarget target, JDXBreakpoint breakpoint, JDXType type) {
			fTarget = target;
			fBreakpoint = breakpoint;
			fType = type;
			fInstall = JDXBreakpointListener.DONT_CARE;
			JDXBreakpointListener[] listeners = getBreakpointListeners();

			for(int i = 0; i < listeners.length; i++) {
				fListener = listeners[i];
				SafeRunner.run(this);
			}
			dispose();

			return (fInstall & JDXBreakpointListener.INSTALL) > 0 || (fInstall & JDXBreakpointListener.DONT_INSTALL) == 0;
		}
	}

	public boolean fireInstalling(JDXDebugTarget target, JDXBreakpoint breakpoint, JDXType type) {
		return getInstallingNotifier().notifyInstalling(target, breakpoint, type);
	}
}
