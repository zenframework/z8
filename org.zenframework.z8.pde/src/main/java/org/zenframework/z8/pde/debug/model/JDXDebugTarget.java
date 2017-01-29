package org.zenframework.z8.pde.debug.model;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.IBreakpointManagerListener;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IThread;

import org.zenframework.z8.pde.Plugin;
import org.zenframework.z8.pde.debug.JDXMessages;
import org.zenframework.z8.pde.debug.breakpoints.JDXBreakpoint;
import org.zenframework.z8.pde.debug.breakpoints.JDXLineBreakpoint;

import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.ThreadDeathEvent;
import com.sun.jdi.event.ThreadStartEvent;
import com.sun.jdi.event.VMDeathEvent;
import com.sun.jdi.event.VMDisconnectEvent;
import com.sun.jdi.event.VMStartEvent;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;

public class JDXDebugTarget extends JDXDebugElement implements IDebugTarget, ILaunchListener, IBreakpointManagerListener, IDebugEventSetListener {
	protected ArrayList<JDXThread> m_threads;
	private IProcess m_process;
	private VirtualMachine m_virtualMachine;

	private boolean m_supportsTerminate;
	private boolean m_terminated;
	private boolean m_terminating;
	private boolean m_supportsDisconnect;
	private boolean m_disconnected;

	private ArrayList<JDXBreakpoint> m_breakpoints;
	private String m_name;

	protected JDXEventDispatcher m_eventDispatcher = null;
	private ThreadStartHandler m_threadStartHandler = null;
	private boolean m_suspended = true;
	private boolean m_resumeOnStartup = false;

	private ILaunch m_launch;
	private int m_suspendCount = 0;

	public JDXDebugTarget(ILaunch launch, VirtualMachine jvm, String name, boolean supportTerminate, boolean supportDisconnect, IProcess process, boolean resume) {
		super(null);
		setLaunch(launch);
		setResumeOnStartup(resume);
		setSupportsTerminate(supportTerminate);
		setSupportsDisconnect(supportDisconnect);
		setVM(jvm);
		jvm.setDebugTraceMode(VirtualMachine.TRACE_NONE);
		setProcess(process);
		setTerminated(false);
		setTerminating(false);
		setDisconnected(false);
		setName(name);
		setBreakpoints(new ArrayList<JDXBreakpoint>(5));
		setThreadList(new ArrayList<JDXThread>(5));
		initialize();
		DebugPlugin.getDefault().getLaunchManager().addLaunchListener(this);
		DebugPlugin.getDefault().getBreakpointManager().addBreakpointManagerListener(this);
	}

	public JDXEventDispatcher getEventDispatcher() {
		return m_eventDispatcher;
	}

	private void setEventDispatcher(JDXEventDispatcher dispatcher) {
		m_eventDispatcher = dispatcher;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Iterator<JDXThread> getThreadIterator() {
		List<JDXThread> threadList;
		synchronized(m_threads) {
			threadList = (List)m_threads.clone();
		}
		return threadList.iterator();
	}

	private void setThreadList(ArrayList<JDXThread> threads) {
		m_threads = threads;
	}

	public ArrayList<JDXBreakpoint> getBreakpoints() {
		return m_breakpoints;
	}

	private void setBreakpoints(ArrayList<JDXBreakpoint> breakpoints) {
		m_breakpoints = breakpoints;
	}

	public void handleVMStart(VMStartEvent event) {
		if(isResumeOnStartup()) {
			try {
				setSuspended(true);
				resume();
			} catch(DebugException e) {
				logError(e);
			}
		}

		IThread[] threads = getThreads();
		for(int i = 0; i < threads.length; i++) {
			JDXThread thread = (JDXThread)threads[i];
			if(thread.isSuspended()) {
				try {
					boolean suspended = thread.getUnderlyingThread().isSuspended();
					if(!suspended) {
						thread.setRunning(true);
						thread.fireResumeEvent(DebugEvent.CLIENT_REQUEST);
					}
				} catch(VMDisconnectedException e) {
				} catch(ObjectCollectedException e) {
				} catch(RuntimeException e) {
					logError(e);
				}
			}
		}
	}

	protected synchronized void initialize() {
		setEventDispatcher(new JDXEventDispatcher(this));
		initializeRequests();
		initializeState();
		initializeBreakpoints();
		getLaunch().addDebugTarget(this);
		DebugPlugin.getDefault().addDebugEventListener(this);
		fireCreationEvent();
	}

	protected void initializeState() {
		List<ThreadReference> threads = null;
		VirtualMachine vm = getVM();
		if(vm != null) {
			try {
				threads = vm.allThreads();
			} catch(RuntimeException e) {
				internalError(e);
			}
			if(threads != null) {
				Iterator<ThreadReference> initialThreads = threads.iterator();
				while(initialThreads.hasNext()) {
					createThread(initialThreads.next());
				}
			}
		}
		if(isResumeOnStartup()) {
			setSuspended(false);
		}
	}

	protected void initializeRequests() {
		setThreadStartHandler(new ThreadStartHandler());
		new ThreadDeathHandler();
	}

	protected void initializeBreakpoints() {
		IBreakpointManager manager = DebugPlugin.getDefault().getBreakpointManager();
		manager.addBreakpointListener(this);
		IBreakpoint[] bps = manager.getBreakpoints(JDXDebugModel.getModelIdentifier());
		for(int i = 0; i < bps.length; i++) {
			if(bps[i] instanceof JDXBreakpoint) {
				breakpointAdded(bps[i]);
			}
		}
	}

	protected JDXThread createThread(ThreadReference thread) {
		JDXThread jdxThread = null;
		try {
			jdxThread = new JDXThread(this, thread);
		} catch(ObjectCollectedException exception) {
			return null;
		}
		if(isDisconnected()) {
			return null;
		}
		synchronized(m_threads) {
			m_threads.add(jdxThread);
		}
		jdxThread.fireCreationEvent();
		return jdxThread;
	}

	@Override
	public IThread[] getThreads() {
		synchronized(m_threads) {
			return m_threads.toArray(new IThread[0]);
		}
	}

	@Override
	public boolean canResume() {
		return isSuspended() && isAvailable();
	}

	@Override
	public boolean canSuspend() {
		if(!isSuspended() && isAvailable()) {
			// only allow suspend if no threads are currently suspended
			IThread[] threads = getThreads();
			for(int i = 0, numThreads = threads.length; i < numThreads; i++) {
				if(((JDXThread)threads[i]).isSuspended()) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean canTerminate() {
		return supportsTerminate() && isAvailable();
	}

	@Override
	public boolean canDisconnect() {
		return supportsDisconnect() && !isDisconnected();
	}

	protected boolean supportsDisconnect() {
		return m_supportsDisconnect;
	}

	private void setSupportsDisconnect(boolean supported) {
		m_supportsDisconnect = supported;
	}

	protected boolean supportsTerminate() {
		return m_supportsTerminate;
	}

	private void setSupportsTerminate(boolean supported) {
		m_supportsTerminate = supported;
	}

	public boolean canPopFrames() {
		if(isAvailable()) {
			VirtualMachine vm = getVM();
			if(vm != null) {
				return vm.canPopFrames();
			}
		}
		return false;
	}

	@Override
	public void disconnect() throws DebugException {
		if(!isAvailable()) {
			return;
		}
		if(!canDisconnect()) {
			notSupported(JDXMessages.JDXDebugTarget_does_not_support_disconnect);
		}
		try {
			disposeThreadHandler();
			VirtualMachine vm = getVM();
			if(vm != null) {
				vm.dispose();
			}
		} catch(VMDisconnectedException e) {
			disconnected();
		} catch(RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDXMessages.JDXDebugTarget_exception_disconnecting, new Object[] { e.toString() }), e);
		}
	}

	private void disposeThreadHandler() {
		ThreadStartHandler handler = getThreadStartHandler();
		if(handler != null) {
			handler.deleteRequest();
		}
	}

	@Override
	public VirtualMachine getVM() {
		return m_virtualMachine;
	}

	private void setVM(VirtualMachine vm) {
		m_virtualMachine = vm;
	}

	@SuppressWarnings("rawtypes")
	public void reinstallBreakpointsIn(List resources, List classNames) {
		ArrayList<JDXBreakpoint> breakpoints = getBreakpoints();

		JDXBreakpoint[] copy = breakpoints.toArray(new JDXBreakpoint[breakpoints.size()]);

		JDXBreakpoint breakpoint = null;

		String installedType = null;

		for(int i = 0; i < copy.length; i++) {
			breakpoint = copy[i];
			if(breakpoint instanceof JDXLineBreakpoint) {
				try {
					installedType = breakpoint.getTypeName();
					if(classNames.contains(installedType)) {
						breakpointRemoved(breakpoint, null);
						breakpointAdded(breakpoint);
					}
				} catch(CoreException ce) {
					logError(ce);
					continue;
				}
			}
		}
	}

	public JDXThread findThread(ThreadReference tr) {
		Iterator<JDXThread> iter = getThreadIterator();

		while(iter.hasNext()) {
			JDXThread thread = iter.next();

			if(thread.getUnderlyingThread().equals(tr))
				return thread;
		}
		return null;
	}

	@Override
	public String getName() throws DebugException {
		if(m_name == null) {
			VirtualMachine vm = getVM();
			if(vm == null) {
				requestFailed(JDXMessages.JDXDebugTarget_Unable_to_retrieve_name___VM_disconnected__1, null);
			}
			try {
				setName(vm.name());
			} catch(RuntimeException e) {
				targetRequestFailed(MessageFormat.format(JDXMessages.JDXDebugTarget_exception_retrieving_name, new Object[] { e.toString() }), e);
				return null;
			}
		}
		return m_name;
	}

	protected void setName(String name) {
		m_name = name;
	}

	protected void setProcess(IProcess process) {
		m_process = process;
	}

	@Override
	public IProcess getProcess() {
		return m_process;
	}

	public void handleVMDeath(VMDeathEvent event) {
		terminated();
	}

	public void handleVMDisconnect(VMDisconnectEvent event) {
		if(isTerminating()) {
			terminated();
		} else {
			disconnected();
		}
	}

	@Override
	public boolean isSuspended() {
		return m_suspended;
	}

	private void setSuspended(boolean suspended) {
		m_suspended = suspended;
	}

	public boolean isAvailable() {
		return !(isTerminated() || isTerminating() || isDisconnected());
	}

	@Override
	public boolean isTerminated() {
		return m_terminated;
	}

	protected void setTerminated(boolean terminated) {
		m_terminated = terminated;
	}

	protected void setDisconnected(boolean disconnected) {
		m_disconnected = disconnected;
	}

	@Override
	public boolean isDisconnected() {
		return m_disconnected;
	}

	public ClassPrepareRequest createClassPrepareRequest(String classPattern) throws CoreException {
		return createClassPrepareRequest(classPattern, null);
	}

	public ClassPrepareRequest createClassPrepareRequest(String classPattern, String classExclusionPattern) throws CoreException {
		return createClassPrepareRequest(classPattern, classExclusionPattern, true);
	}

	public ClassPrepareRequest createClassPrepareRequest(String classPattern, String classExclusionPattern, boolean enabled) throws CoreException {
		EventRequestManager manager = getEventRequestManager();
		if(!isAvailable() || manager == null) {
			requestFailed(JDXMessages.JDXDebugTarget_Unable_to_create_class_prepare_request___VM_disconnected__2, null);
		}
		ClassPrepareRequest req = null;
		try {
			req = manager.createClassPrepareRequest();
			req.addClassFilter(classPattern);
			if(classExclusionPattern != null) {
				req.addClassExclusionFilter(classExclusionPattern);
			}
			req.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
			if(enabled) {
				req.enable();
			}
		} catch(RuntimeException e) {
			targetRequestFailed(JDXMessages.JDXDebugTarget_Unable_to_create_class_prepare_request__3, e);
			return null;
		}
		return req;
	}

	@Override
	public void resume() throws DebugException {
		setResumeOnStartup(true);
		resume(true);
	}

	public void resumeQuiet() throws DebugException {
		resume(false);
	}

	protected void resume(boolean fireNotification) throws DebugException {
		if(!isSuspended() || !isAvailable()) {
			return;
		}
		try {
			setSuspended(false);
			resumeThreads();
			VirtualMachine vm = getVM();
			if(vm != null) {
				vm.resume();
			}
			if(fireNotification) {
				fireResumeEvent(DebugEvent.CLIENT_REQUEST);
			}
		} catch(VMDisconnectedException e) {
			disconnected();
			return;
		} catch(RuntimeException e) {
			setSuspended(true);
			fireSuspendEvent(DebugEvent.CLIENT_REQUEST);
			targetRequestFailed(MessageFormat.format(JDXMessages.JDXDebugTarget_exception_resume, new Object[] { e.toString() }), e);
		}
	}

	@Override
	public boolean supportsBreakpoint(IBreakpoint breakpoint) {
		return breakpoint instanceof JDXBreakpoint;
	}

	@Override
	public void breakpointAdded(IBreakpoint breakpoint) {
		if(!isAvailable()) {
			return;
		}
		if(supportsBreakpoint(breakpoint)) {
			try {
				JDXBreakpoint jdxBreakpoint = (JDXBreakpoint)breakpoint;

				if(!getBreakpoints().contains(breakpoint)) {
					if(!jdxBreakpoint.shouldSkipBreakpoint()) {
						jdxBreakpoint.addToTarget(this);
					}
					getBreakpoints().add(jdxBreakpoint);
				}
			} catch(CoreException e) {
				logError(e);
			}
		}
	}

	@Override
	public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta) {
	}

	@Override
	public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {
		if(!isAvailable()) {
			return;
		}
		if(supportsBreakpoint(breakpoint)) {
			try {
				((JDXBreakpoint)breakpoint).removeFromTarget(this);
				getBreakpoints().remove(breakpoint);

				Iterator<JDXThread> threads = getThreadIterator();

				while(threads.hasNext()) {
					threads.next().removeCurrentBreakpoint(breakpoint);
				}
			} catch(CoreException e) {
				logError(e);
			}
		}
	}

	@Override
	public void suspend() throws DebugException {
		if(isSuspended()) {
			return;
		}
		try {
			VirtualMachine vm = getVM();
			if(vm != null) {
				vm.suspend();
			}
			suspendThreads();
			setSuspended(true);
			fireSuspendEvent(DebugEvent.CLIENT_REQUEST);
		} catch(RuntimeException e) {
			setSuspended(false);
			fireResumeEvent(DebugEvent.CLIENT_REQUEST);
			targetRequestFailed(MessageFormat.format(JDXMessages.JDXDebugTarget_exception_suspend, new Object[] { e.toString() }), e);
		}
	}

	protected void suspendThreads() {
		Iterator<JDXThread> threads = getThreadIterator();
		while(threads.hasNext()) {
			threads.next().suspendedByVM();
		}
	}

	protected void resumeThreads() throws DebugException {
		Iterator<JDXThread> threads = getThreadIterator();
		while(threads.hasNext()) {
			threads.next().resumedByVM();
		}
	}

	public void prepareToSuspendByBreakpoint(JDXBreakpoint breakpoint) {
		setSuspended(true);
		suspendThreads();
	}

	protected void suspendedByBreakpoint(JDXBreakpoint breakpoint, boolean queueEvent) {
		if(queueEvent) {
			queueSuspendEvent(DebugEvent.BREAKPOINT);
		} else {
			fireSuspendEvent(DebugEvent.BREAKPOINT);
		}
	}

	protected void cancelSuspendByBreakpoint(JDXBreakpoint breakpoint) throws DebugException {
		setSuspended(false);
		resumeThreads();
	}

	@Override
	public void terminate() throws DebugException {
		if(!isAvailable()) {
			return;
		}
		if(!supportsTerminate()) {
			notSupported(JDXMessages.JDXDebugTarget_does_not_support_termination);
		}
		try {
			setTerminating(true);
			disposeThreadHandler();
			VirtualMachine vm = getVM();
			if(vm != null) {
				vm.exit(1);
			}
			IProcess process = getProcess();
			if(process != null) {
				process.terminate();
			}
		} catch(VMDisconnectedException e) {
			terminated();
		} catch(RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDXMessages.JDXDebugTarget_exception_terminating, new Object[] { e.toString() }), e);
		}
	}

	protected void terminated() {
		setTerminating(false);
		if(!isTerminated()) {
			setTerminated(true);
			setDisconnected(true);
			cleanup();
			fireTerminateEvent();
		}
	}

	@Override
	protected void disconnected() {
		if(!isDisconnected()) {
			setDisconnected(true);
			cleanup();
			fireTerminateEvent();
		}
	}

	protected void cleanup() {
		removeAllThreads();
		DebugPlugin plugin = DebugPlugin.getDefault();
		plugin.getBreakpointManager().removeBreakpointListener(this);
		plugin.getLaunchManager().removeLaunchListener(this);
		plugin.getBreakpointManager().removeBreakpointManagerListener(this);
		plugin.removeDebugEventListener(this);
		removeAllBreakpoints();
		m_virtualMachine = null;
		setThreadStartHandler(null);
		setEventDispatcher(null);
	}

	protected void removeAllThreads() {
		Iterator<JDXThread> iterator = getThreadIterator();

		while(iterator.hasNext()) {
			iterator.next().terminated();
		}
		synchronized(m_threads) {
			m_threads.clear();
		}
	}

	@SuppressWarnings("rawtypes")
	protected void removeAllBreakpoints() {
		Iterator breakpoints = ((List)getBreakpoints().clone()).iterator();

		while(breakpoints.hasNext()) {
			JDXBreakpoint breakpoint = (JDXBreakpoint)breakpoints.next();

			try {
				breakpoint.removeFromTarget(this);
			} catch(CoreException e) {
				logError(e);
			}
		}
		getBreakpoints().clear();
	}

	@SuppressWarnings("rawtypes")
	protected void reinstallAllBreakpoints() {
		Iterator breakpoints = ((List)getBreakpoints().clone()).iterator();

		while(breakpoints.hasNext()) {
			JDXBreakpoint breakpoint = (JDXBreakpoint)breakpoints.next();
			try {
				breakpoint.addToTarget(this);
			} catch(CoreException e) {
				logError(e);
			}
		}
	}

	public List<ReferenceType> classesByName(String className) {
		VirtualMachine vm = getVM();

		if(vm != null) {
			try {
				return vm.classesByName(className);
			} catch(VMDisconnectedException e) {
				if(!isAvailable()) {
					return new ArrayList<ReferenceType>();
				}
				logError(e);
			} catch(RuntimeException e) {
				internalError(e);
			}
		}
		return new ArrayList<ReferenceType>();
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		if(adapter == IDebugTarget.class) {
			return this;
		}
		return super.getAdapter(adapter);
	}

	public void shutdown() {
		JDXEventDispatcher dispatcher = getJDXDebugTarget().getEventDispatcher();

		if(dispatcher != null) {
			dispatcher.shutdown();
		}
		try {
			if(supportsTerminate()) {
				terminate();
			} else if(supportsDisconnect()) {
				disconnect();
			}
		} catch(DebugException e) {
			Plugin.log(e);
		}
		cleanup();
	}

	protected boolean isTerminating() {
		return m_terminating;
	}

	protected void setTerminating(boolean terminating) {
		m_terminating = terminating;
	}

	class ThreadStartHandler implements JDXEventListener {
		protected EventRequest fRequest;

		protected ThreadStartHandler() {
			createRequest();
		}

		protected void createRequest() {
			EventRequestManager manager = getEventRequestManager();
			if(manager != null) {
				try {
					EventRequest req = manager.createThreadStartRequest();
					req.setSuspendPolicy(EventRequest.SUSPEND_NONE);
					req.enable();
					addEventListener(this, req);
					setRequest(req);
				} catch(RuntimeException e) {
					logError(e);
				}
			}
		}

		@Override
		public boolean handleEvent(Event event, JDXDebugTarget target) {
			ThreadReference thread = ((ThreadStartEvent)event).thread();
			try {
				if(thread.isCollected()) {
					return false;
				}
			} catch(VMDisconnectedException exception) {
				return false;
			} catch(ObjectCollectedException e) {
				return false;
			}
			JDXThread jdxThread = findThread(thread);
			if(jdxThread == null) {
				jdxThread = createThread(thread);
				if(jdxThread == null) {
					return false;
				}
			} else {
				jdxThread.disposeStackFrames();
				jdxThread.fireChangeEvent(DebugEvent.CONTENT);
			}
			return !jdxThread.isSuspended();
		}

		@Override
		public void wonSuspendVote(Event event, JDXDebugTarget target) {
		}

		protected void deleteRequest() {
			if(getRequest() != null) {
				removeEventListener(this, getRequest());
				setRequest(null);
			}
		}

		protected EventRequest getRequest() {
			return fRequest;
		}

		protected void setRequest(EventRequest request) {
			fRequest = request;
		}
	}

	class ThreadDeathHandler implements JDXEventListener {
		protected ThreadDeathHandler() {
			createRequest();
		}

		protected void createRequest() {
			EventRequestManager manager = getEventRequestManager();
			if(manager != null) {
				try {
					EventRequest req = manager.createThreadDeathRequest();
					req.setSuspendPolicy(EventRequest.SUSPEND_NONE);
					req.enable();
					addEventListener(this, req);
				} catch(RuntimeException e) {
					logError(e);
				}
			}
		}

		@Override
		public boolean handleEvent(Event event, JDXDebugTarget target) {
			ThreadReference ref = ((ThreadDeathEvent)event).thread();
			JDXThread thread = findThread(ref);
			if(thread != null) {
				synchronized(m_threads) {
					m_threads.remove(thread);
				}
				thread.terminated();
			}
			return true;
		}

		@Override
		public void wonSuspendVote(Event event, JDXDebugTarget target) {
		}
	}

	class CleanUpJob extends Job {
		public CleanUpJob() {
			super(JDXMessages.JDXDebugTarget_0);
			setSystem(true);
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			if(isAvailable()) {
				if(m_eventDispatcher != null) {
					m_eventDispatcher.shutdown();
				}
				disconnected();
			}
			return Status.OK_STATUS;
		}

		@Override
		public boolean shouldRun() {
			return isAvailable();
		}

		@Override
		public boolean shouldSchedule() {
			return isAvailable();
		}
	}

	protected ThreadStartHandler getThreadStartHandler() {
		return m_threadStartHandler;
	}

	protected void setThreadStartHandler(ThreadStartHandler threadStartHandler) {
		m_threadStartHandler = threadStartHandler;
	}

	@Override
	public boolean supportsStorageRetrieval() {
		return false;
	}

	@Override
	public IMemoryBlock getMemoryBlock(long startAddress, long length) throws DebugException {
		notSupported(JDXMessages.JDXDebugTarget_does_not_support_storage_retrieval);
		return null;
	}

	@Override
	public void launchRemoved(ILaunch launch) {
		if(!isAvailable()) {
			return;
		}
		if(launch.equals(getLaunch())) {
			disconnected();
		}
	}

	@Override
	public void launchAdded(ILaunch launch) {
	}

	@Override
	public void launchChanged(ILaunch launch) {
	}

	private synchronized void setResumeOnStartup(boolean resume) {
		m_resumeOnStartup = resume;
	}

	protected synchronized boolean isResumeOnStartup() {
		return m_resumeOnStartup;
	}

	@Override
	public boolean hasThreads() {
		return m_threads.size() > 0;
	}

	@Override
	public ILaunch getLaunch() {
		return m_launch;
	}

	private void setLaunch(ILaunch launch) {
		m_launch = launch;
	}

	public int getSuspendCount() {
		return m_suspendCount;
	}

	protected void incrementSuspendCount(int eventDetail) {
		if(eventDetail != DebugEvent.EVALUATION_IMPLICIT) {
			m_suspendCount++;
		}
	}

	public boolean supportsAccessWatchpoints() {
		VirtualMachine vm = getVM();
		if(isAvailable() && vm != null) {
			return vm.canWatchFieldAccess();
		}
		return false;
	}

	public boolean supportsModificationWatchpoints() {
		VirtualMachine vm = getVM();
		if(isAvailable() && vm != null) {
			return vm.canWatchFieldModification();
		}
		return false;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void breakpointManagerEnablementChanged(boolean enabled) {
		if(!isAvailable()) {
			return;
		}

		Iterator breakpoints = ((List)getBreakpoints().clone()).iterator();

		while(breakpoints.hasNext()) {
			JDXBreakpoint breakpoint = (JDXBreakpoint)breakpoints.next();
			try {
				if(enabled) {
					breakpoint.addToTarget(this);
				} else if(breakpoint.shouldSkipBreakpoint()) {
					breakpoint.removeFromTarget(this);
				}
			} catch(CoreException e) {
				logError(e);
			}
		}
	}

	@Override
	public void handleDebugEvents(DebugEvent[] events) {
		if(events.length == 1) {
			DebugEvent event = events[0];
			if(event.getSource() == this && event.getKind() == DebugEvent.CREATE) {
				JDXEventDispatcher dispatcher = getJDXDebugTarget().getEventDispatcher();
				if(dispatcher != null) {
					Thread t = new Thread(dispatcher, JDXDebugModel.getModelIdentifier() + JDXMessages.JDXDebugTarget_JDX_Event_Dispatcher);
					t.setDaemon(true);
					t.start();
				}
			} else if(event.getSource().equals(getProcess()) && event.getKind() == DebugEvent.TERMINATE) {
				new CleanUpJob().schedule(3000);
			}
		}
	}

	@Override
	public IDebugTarget getDebugTarget() {
		return this;
	}
}
