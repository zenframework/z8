package org.zenframework.z8.pde.debug.model;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;

import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.workspace.Workspace;
import org.zenframework.z8.pde.Plugin;
import org.zenframework.z8.pde.debug.JDXDebug;
import org.zenframework.z8.pde.debug.JDXMessages;
import org.zenframework.z8.pde.debug.breakpoints.JDXBreakpoint;
import org.zenframework.z8.pde.debug.model.value.JDXObjectValue;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassType;
import com.sun.jdi.Field;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.InvocationException;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadGroupReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.Value;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.StepRequest;

public class JDXThread extends JDXDebugElement implements IThread {
	public static final int ERR_THREAD_NOT_SUSPENDED = 100;
	public static final int ERR_NESTED_METHOD_INVOCATION = 101;
	public static final int ERR_INCOMPATIBLE_THREAD_STATE = 102;

	private static final String MAIN_THREAD_GROUP = "main";
	public static final int SUSPEND_TIMEOUT = 161;

	private ThreadReference fThread;
	private String fPreviousName;

	private ThreadGroupReference fThreadGroup;
	private String fThreadGroupName;

	private List<IStackFrame> fStackFrames;
	private boolean fRefreshChildren = true;

	private StepHandler fStepHandler = null;

	private boolean fRunning;
	private boolean fTerminated;
	private boolean fSuspendedQuiet;
	private boolean fIsSystemThread;

	private List<IBreakpoint> fCurrentBreakpoints = new ArrayList<IBreakpoint>(2);

	private int fOriginalStepKind;
	private Location fOriginalStepLocation;
	private int fOriginalStepStackDepth;
	private IType fOriginalType;
	private int fOriginalSourceLineNumber;

	protected boolean fIsSuspending = false;
	private boolean fIsInvokingMethod = false;

	private ThreadJob fAsyncJob;
	protected ThreadJob fRunningAsyncJob;

	public JDXThread(JDXDebugTarget target, ThreadReference thread) throws ObjectCollectedException {
		super(target);
		setUnderlyingThread(thread);
		initialize();
	}

	protected void initialize() throws ObjectCollectedException {
		fStackFrames = new ArrayList<IStackFrame>();

		try {
			determineIfSystemThread();
		} catch(DebugException e) {
			Throwable underlyingException = e.getStatus().getException();
			if(underlyingException instanceof VMDisconnectedException) {
				disconnected();
				return;
			}
			if(underlyingException instanceof ObjectCollectedException) {
				throw (ObjectCollectedException)underlyingException;
			}
			logError(e);
		}

		setTerminated(false);
		setRunning(false);

		try {
			if(getUnderlyingThread().status() == ThreadReference.THREAD_STATUS_UNKNOWN) {
				setRunning(true);
				return;
			}
		} catch(VMDisconnectedException e) {
			disconnected();
			return;
		} catch(ObjectCollectedException e) {
			throw e;
		} catch(RuntimeException e) {
			logError(e);
		}
		try {
			setRunning(!getUnderlyingThread().isSuspended());
		} catch(VMDisconnectedException e) {
			disconnected();
			return;
		} catch(ObjectCollectedException e) {
			throw e;
		} catch(RuntimeException e) {
			logError(e);
		}
	}

	protected void addCurrentBreakpoint(IBreakpoint bp) {
		fCurrentBreakpoints.add(bp);
	}

	protected void removeCurrentBreakpoint(IBreakpoint bp) {
		fCurrentBreakpoints.remove(bp);
	}

	@Override
	public IBreakpoint[] getBreakpoints() {
		return fCurrentBreakpoints.toArray(new IBreakpoint[fCurrentBreakpoints.size()]);
	}

	@Override
	public boolean canResume() {
		return isSuspended() && !isSuspendedQuiet();
	}

	@Override
	public boolean canSuspend() {
		return !isSuspended() || isSuspendedQuiet();
	}

	@Override
	public boolean canTerminate() {
		return getDebugTarget().canTerminate();
	}

	@Override
	public boolean canStepInto() {
		return canStep();
	}

	@Override
	public boolean canStepOver() {
		return canStep();
	}

	@Override
	public boolean canStepReturn() {
		return canStep();
	}

	protected boolean canStep() {
		try {
			return isSuspended() && !isSuspendedQuiet() && !isStepping() && getTopStackFrame() != null;
		} catch(DebugException e) {
			return false;
		}
	}

	protected boolean isInvokingMethod() {
		return fIsInvokingMethod;
	}

	protected void setInvokingMethod(boolean isInvokingMethod) {
		fIsInvokingMethod = isInvokingMethod;
	}

	protected void determineIfSystemThread() throws DebugException {
		fIsSystemThread = false;
		ThreadGroupReference tgr = getUnderlyingThreadGroup();
		fIsSystemThread = tgr != null;
		while(tgr != null) {
			String tgn = null;
			try {
				tgn = tgr.name();
				tgr = tgr.parent();
			} catch(UnsupportedOperationException e) {
				fIsSystemThread = false;
				break;
			} catch(RuntimeException e) {
				Plugin.log(e);
				return;
			}
			if(tgn != null && tgn.equals(MAIN_THREAD_GROUP)) {
				fIsSystemThread = false;
				break;
			}
		}
	}

	@Override
	public IStackFrame[] getStackFrames() throws DebugException {
		if(isSuspendedQuiet()) {
			return new IStackFrame[0];
		}
		List<IStackFrame> list = computeStackFrames();
		return list.toArray(new IStackFrame[list.size()]);
	}

	protected synchronized List<IStackFrame> computeStackFrames(boolean refreshChildren) throws DebugException {
		if(isSuspended()) {
			if(isTerminated()) {
				fStackFrames.clear();
			} else if(refreshChildren) {
				List<StackFrame> frames = getUnderlyingFrames();

				int oldSize = fStackFrames.size();
				int newSize = frames.size();
				int discard = oldSize - newSize; // number of old frames to
													// discard, if any
				for(int i = 0; i < discard; i++) {
					JDXStackFrame invalid = (JDXStackFrame)fStackFrames.remove(0);
					invalid.bind(null, -1);
				}
				int newFrames = newSize - oldSize; // number of frames to
													// create, if any
				int depth = oldSize;
				for(int i = newFrames - 1; i >= 0; i--) {
					fStackFrames.add(0, new JDXStackFrame(this, frames.get(i), depth));
					depth++;
				}

				int numToRebind = Math.min(newSize, oldSize);

				int offset = newSize - 1;

				for(depth = 0; depth < numToRebind; depth++) {
					JDXStackFrame oldFrame = (JDXStackFrame)fStackFrames.get(offset);
					StackFrame frame = frames.get(offset);
					JDXStackFrame newFrame = oldFrame.bind(frame, depth);
					if(newFrame != oldFrame) {
						fStackFrames.set(offset, newFrame);
					}
					offset--;
				}
			}
			fRefreshChildren = false;
		} else {
			return new ArrayList<IStackFrame>();
		}
		return fStackFrames;
	}

	public synchronized List<IStackFrame> computeStackFrames() throws DebugException {
		return computeStackFrames(fRefreshChildren);
	}

	public List<IStackFrame> computeNewStackFrames() throws DebugException {
		return computeStackFrames(true);
	}

	private List<StackFrame> getUnderlyingFrames() throws DebugException {
		if(!isSuspended()) {
			requestFailed(JDXMessages.JDXThread_Unable_to_retrieve_stack_frame___thread_not_suspended__1, null, JDXThread.ERR_THREAD_NOT_SUSPENDED);
		}
		try {
			return getUnderlyingThread().frames();
		} catch(IncompatibleThreadStateException e) {
			requestFailed(JDXMessages.JDXThread_Unable_to_retrieve_stack_frame___thread_not_suspended__1, e, JDXThread.ERR_THREAD_NOT_SUSPENDED);
		} catch(RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDXMessages.JDXThread_exception_retrieving_stack_frames_2, new Object[] { e.toString() }), e);
		} catch(InternalError e) {
			targetRequestFailed(MessageFormat.format(JDXMessages.JDXThread_exception_retrieving_stack_frames_2, new Object[] { e.toString() }), e);
		}
		return null;
	}

	protected int getUnderlyingFrameCount() throws DebugException {
		try {
			return getUnderlyingThread().frameCount();
		} catch(RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDXMessages.JDXThread_exception_retrieving_frame_count, new Object[] { e.toString() }), e);
		} catch(IncompatibleThreadStateException e) {
			requestFailed(MessageFormat.format(JDXMessages.JDXThread_exception_retrieving_frame_count, new Object[] { e.toString() }), e, JDXThread.ERR_THREAD_NOT_SUSPENDED);
		}
		return -1;
	}

	protected boolean canRunEvaluation() {
		try {
			return isSuspendedQuiet() || (isSuspended() && !isStepping() && getTopStackFrame() != null);
		} catch(DebugException e) {
			return false;
		}
	}

	public synchronized Value invokeMethod(ObjectReference receiverObject, Method method, List<Value> args) {
		Value result = null;

		try {
			assert (isSuspended() && !isInvokingMethod());
			setRunning(true);
			setInvokingMethod(true);

			preserveStackFrames();
			int flags = ClassType.INVOKE_SINGLE_THREADED;

			result = receiverObject.invokeMethod(getUnderlyingThread(), method, args, flags);
		} catch(InvalidTypeException e) {
			Plugin.log(e);
		} catch(ClassNotLoadedException e) {
			Plugin.log(e);
		} catch(IncompatibleThreadStateException e) {
			Plugin.log(e);
		} catch(InvocationException e) {
			Plugin.log(e);
		} catch(RuntimeException e) {
			Plugin.log(e);
		}

		invokeComplete();

		return result;
	}

	protected void invokeComplete() {
		setInvokingMethod(false);
		setRunning(false);

		try {
			computeStackFrames();
		} catch(DebugException e) {
			Plugin.log(e);
		}
	}

	public void queueRunnable(Runnable evaluation) {
		if(fAsyncJob == null) {
			fAsyncJob = new ThreadJob(this);
		}
		fAsyncJob.addRunnable(evaluation);
	}

	@Override
	public String getName() throws DebugException {
		try {
			fPreviousName = getUnderlyingThread().name();
		} catch(RuntimeException e) {
			if(e instanceof ObjectCollectedException) {
				if(fPreviousName == null) {
					fPreviousName = JDXMessages.JDXThread_garbage_collected_1;
				}
			} else if(e instanceof VMDisconnectedException) {
				if(fPreviousName == null) {
					fPreviousName = JDXMessages.JDXThread_42;
				}
			} else {
				targetRequestFailed(MessageFormat.format(JDXMessages.JDXThread_exception_retrieving_thread_name, new Object[] { e.toString() }), e);
			}
		}
		return fPreviousName;
	}

	@Override
	public int getPriority() throws DebugException {
		Field p = null;
		try {
			p = getUnderlyingThread().referenceType().fieldByName("priority");
			if(p == null) {
				requestFailed(JDXMessages.JDXThread_no_priority_field, null);
			}
			Value v = getUnderlyingThread().getValue(p);
			if(v instanceof IntegerValue) {
				return ((IntegerValue)v).value();
			}
			requestFailed(JDXMessages.JDXThread_priority_not_an_integer, null);
		} catch(RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDXMessages.JDXThread_exception_retrieving_thread_priority, new Object[] { e.toString() }), e);
		}

		return -1;
	}

	@Override
	public IStackFrame getTopStackFrame() throws DebugException {
		List<IStackFrame> c = computeStackFrames();

		if(c.isEmpty()) {
			return null;
		}
		return c.get(0);
	}

	public synchronized boolean handleSuspendForBreakpoint(JDXBreakpoint breakpoint, boolean queueEvent) {
		addCurrentBreakpoint(breakpoint);
		setSuspendedQuiet(false);
		try {
			if(breakpoint.getSuspendPolicy() == JDXBreakpoint.SUSPEND_VM) {
				getJDXDebugTarget().prepareToSuspendByBreakpoint(breakpoint);
			} else {
				setRunning(false);
			}

			boolean suspend = JDXDebug.getDefault().fireBreakpointHit(this, breakpoint);

			if(suspend) {
				if(breakpoint.getSuspendPolicy() == JDXBreakpoint.SUSPEND_VM) {
					getJDXDebugTarget().suspendedByBreakpoint(breakpoint, queueEvent);
				}
				abortStep();
				if(queueEvent) {
					queueSuspendEvent(DebugEvent.BREAKPOINT);
				} else {
					fireSuspendEvent(DebugEvent.BREAKPOINT);
				}
			} else {
				if(breakpoint.getSuspendPolicy() == JDXBreakpoint.SUSPEND_VM) {
					getJDXDebugTarget().cancelSuspendByBreakpoint(breakpoint);
				} else {
					setRunning(true);
					preserveStackFrames();
				}
			}
			return suspend;
		} catch(CoreException e) {
			logError(e);
			return true;
		}
	}

	public void wonSuspendVote(JDXBreakpoint breakpoint) {
		setSuspendedQuiet(false);
		try {
			setRunning(false);
			if(breakpoint.getSuspendPolicy() == JDXBreakpoint.SUSPEND_VM) {
				getJDXDebugTarget().suspendedByBreakpoint(breakpoint, false);
			}
		} catch(CoreException e) {
			logError(e);
		}
	}

	public synchronized boolean handleSuspendForBreakpointQuiet(JDXBreakpoint breakpoint) {
		addCurrentBreakpoint(breakpoint);
		setSuspendedQuiet(true);
		setRunning(false);
		return true;
	}

	@Override
	public boolean isStepping() {
		return getPendingStepHandler() != null;
	}

	@Override
	public boolean isSuspended() {
		return !fRunning && !fTerminated;
	}

	public boolean isSuspendedQuiet() {
		return fSuspendedQuiet;
	}

	public boolean isSystemThread() {
		return fIsSystemThread;
	}

	public String getThreadGroupName() throws DebugException {
		if(fThreadGroupName == null) {
			ThreadGroupReference tgr = getUnderlyingThreadGroup();

			if(tgr == null) {
				return null;
			}
			try {
				fThreadGroupName = tgr.name();
			} catch(RuntimeException e) {
				targetRequestFailed(MessageFormat.format(JDXMessages.JDXThread_exception_retrieving_thread_group_name, new Object[] { e.toString() }), e);
				return null;
			}
		}
		return fThreadGroupName;
	}

	@Override
	public boolean isTerminated() {
		return fTerminated;
	}

	protected void setTerminated(boolean terminated) {
		fTerminated = terminated;
	}

	@Override
	public synchronized void resume() throws DebugException {
		if(getDebugTarget().isSuspended()) {
			getDebugTarget().resume();
		} else {
			resumeThread(true);
		}
	}

	public synchronized void resumeQuiet() {
		if(isSuspendedQuiet()) {
			resumeThread(false);
		}
	}

	private synchronized void resumeThread(boolean fireNotification) {
		if(!isSuspended()) {
			return;
		}
		try {
			setRunning(true);
			setSuspendedQuiet(false);
			if(fireNotification) {
				fireResumeEvent(DebugEvent.CLIENT_REQUEST);
			}
			preserveStackFrames();
			getUnderlyingThread().resume();
		} catch(VMDisconnectedException e) {
			disconnected();
		} catch(RuntimeException e) {
			fireSuspendEvent(DebugEvent.CLIENT_REQUEST);
			Plugin.log(e);
		}
	}

	protected void setRunning(boolean running) {
		fRunning = running;
		if(running) {
			fCurrentBreakpoints.clear();
		}
	}

	protected void setSuspendedQuiet(boolean suspendedQuiet) {
		fSuspendedQuiet = suspendedQuiet;
	}

	protected synchronized void preserveStackFrames() {
		fRefreshChildren = true;

		Iterator<IStackFrame> frames = fStackFrames.iterator();

		while(frames.hasNext()) {
			((JDXStackFrame)frames.next()).setUnderlyingStackFrame(null);
		}
	}

	protected synchronized void disposeStackFrames() {
		fStackFrames.clear();
		fRefreshChildren = true;
	}

	@Override
	public synchronized void stepInto() throws DebugException {
		if(!canStepInto()) {
			return;
		}
		StepHandler handler = new StepIntoHandler();
		handler.step();
	}

	@Override
	public synchronized void stepOver() throws DebugException {
		if(!canStepOver()) {
			return;
		}
		StepHandler handler = new StepOverHandler();
		handler.step();
	}

	@Override
	public synchronized void stepReturn() throws DebugException {
		if(!canStepReturn()) {
			return;
		}
		StepHandler handler = new StepReturnHandler();
		handler.step();
	}

	protected void setOriginalStepKind(int stepKind) {
		fOriginalStepKind = stepKind;
	}

	protected int getOriginalStepKind() {
		return fOriginalStepKind;
	}

	protected void setOriginalStepLocation(Location location) {
		fOriginalStepLocation = location;
	}

	protected Location getOriginalStepLocation() {
		return fOriginalStepLocation;
	}

	protected void setOriginalStepStackDepth(int depth) {
		fOriginalStepStackDepth = depth;
	}

	protected int getOriginalStepStackDepth() {
		return fOriginalStepStackDepth;
	}

	protected void setOriginalStepType(IType classType) {
		fOriginalType = classType;
	}

	protected IType getOriginalStepType() {
		return fOriginalType;
	}

	protected void setOriginalStepSourceLineNumber(int lineNumber) {
		fOriginalSourceLineNumber = lineNumber;
	}

	protected int getOriginalStepSourceLineNumber() {
		return fOriginalSourceLineNumber;
	}

	public IType getType(Location location) {
		if(location != null) {
			try {
				String sourcePath = location.sourcePath();
				if(sourcePath != null) {
					String qualifiedName = new Path(sourcePath).removeFileExtension().toString().replace(IPath.SEPARATOR, '.');
					return Workspace.getInstance().lookupType(qualifiedName);
				}
			} catch(AbsentInformationException e) {
			}
		}
		return null;
	}

	protected boolean shouldDoExtraStepInto(Location location) throws DebugException {
		if(getOriginalStepKind() != StepRequest.STEP_INTO) {
			return false;
		}
		if(getOriginalStepStackDepth() != getUnderlyingFrameCount()) {
			return false;
		}
		Location origLocation = getOriginalStepLocation();
		if(origLocation == null) {
			return false;
		}
		// We cannot simply check if the two Locations are equal using the
		// equals()
		// method, since this checks the code index within the method. Even if
		// the
		// code indices are different, the line numbers may be the same, in
		// which case
		// we need to do the extra step into.
		Method origMethod = origLocation.method();
		Method currMethod = location.method();
		if(!origMethod.equals(currMethod)) {
			return false;
		}
		if(origLocation.lineNumber() != location.lineNumber()) {
			return false;
		}
		return true;
	}

	@Override
	public synchronized void suspend() {
		try {
			abortStep();
			setSuspendedQuiet(false);
			suspendUnderlyingThread();
		} catch(RuntimeException e) {
			setRunning(true);
			Plugin.log(e);
		}
	}

	protected synchronized void suspendUnderlyingThread() {
		if(fIsSuspending) {
			return;
		}
		if(isSuspended()) {
			fireSuspendEvent(DebugEvent.CLIENT_REQUEST);
			return;
		}
		fIsSuspending = true;
		Thread thread = new Thread(new Runnable() {
			@Override
			@SuppressWarnings("deprecation")
			public void run() {
				try {
					getUnderlyingThread().suspend();
					int timeout = JDXDebugModel.getPreferences().getInt(JDXDebugModel.PREF_REQUEST_TIMEOUT);
					long stop = System.currentTimeMillis() + timeout;
					boolean suspended = isUnderlyingThreadSuspended();
					while(System.currentTimeMillis() < stop && !suspended) {
						try {
							Thread.sleep(50);
						} catch(InterruptedException e) {
						}
						suspended = isUnderlyingThreadSuspended();
						if(suspended) {
							break;
						}
					}
					if(!suspended) {
						IStatus status = new Status(IStatus.ERROR, Plugin.getUniqueIdentifier(), SUSPEND_TIMEOUT, MessageFormat.format(JDXMessages.JDXThread_suspend_timeout, new Object[] { new Integer(timeout).toString() }), null);
						IStatusHandler handler = DebugPlugin.getDefault().getStatusHandler(status);
						if(handler != null) {
							try {
								handler.handleStatus(status, JDXThread.this);
							} catch(CoreException e) {
							}
						}
					}
					setRunning(false);
					fireSuspendEvent(DebugEvent.CLIENT_REQUEST);
				} catch(RuntimeException exception) {
				} finally {
					fIsSuspending = false;
				}
			}
		});
		thread.setDaemon(true);
		thread.start();
	}

	public boolean isUnderlyingThreadSuspended() {
		return getUnderlyingThread().isSuspended();
	}

	protected synchronized void suspendedByVM() {
		setRunning(false);
		setSuspendedQuiet(false);
	}

	protected synchronized void resumedByVM() {
		setRunning(true);
		preserveStackFrames();

		ThreadReference thread = getUnderlyingThread();

		while(thread.suspendCount() > 1) {
			try {
				thread.resume();
			} catch(ObjectCollectedException e) {
			} catch(VMDisconnectedException e) {
				disconnected();
			} catch(RuntimeException e) {
				setRunning(false);
				fireSuspendEvent(DebugEvent.CLIENT_REQUEST);
				Plugin.log(e);
			}
		}
	}

	@Override
	public void terminate() throws DebugException {
		getDebugTarget().terminate();
	}

	protected synchronized void stepToFrame(IStackFrame frame) throws DebugException {
		if(!canStepReturn()) {
			return;
		}
		StepHandler handler = new StepToFrameHandler(frame);
		handler.step();
	}

	protected void abortStep() {
		StepHandler handler = getPendingStepHandler();
		if(handler != null) {
			handler.abort();
		}
	}

	public IVariable findVariable(String varName) throws DebugException {
		if(isSuspended()) {
			try {
				IStackFrame[] stackFrames = getStackFrames();
				for(int i = 0; i < stackFrames.length; i++) {
					JDXStackFrame sf = (JDXStackFrame)stackFrames[i];
					IVariable var = sf.findVariable(varName);
					if(var != null) {
						return var;
					}
				}
			} catch(DebugException e) {
				if(e.getStatus().getCode() != JDXThread.ERR_THREAD_NOT_SUSPENDED) {
					throw e;
				}
			}
		}
		return null;
	}

	protected void terminated() {
		setTerminated(true);
		setRunning(false);
		fireTerminateEvent();
	}

	public ThreadReference getUnderlyingThread() {
		return fThread;
	}

	protected void setUnderlyingThread(ThreadReference thread) {
		fThread = thread;
	}

	protected ThreadGroupReference getUnderlyingThreadGroup() throws DebugException {
		if(fThreadGroup == null) {
			try {
				fThreadGroup = getUnderlyingThread().threadGroup();
			} catch(UnsupportedOperationException e) {
				requestFailed(MessageFormat.format(JDXMessages.JDXThread_exception_retrieving_thread_group, new Object[] { e.toString() }), e);
				return null;
			} catch(RuntimeException e) {
				targetRequestFailed(MessageFormat.format(JDXMessages.JDXThread_exception_retrieving_thread_group, new Object[] { e.toString() }), e);
				return null;
			}
		}
		return fThreadGroup;
	}

	protected void setPendingStepHandler(StepHandler handler) {
		fStepHandler = handler;
	}

	protected StepHandler getPendingStepHandler() {
		return fStepHandler;
	}

	abstract class StepHandler implements JDXEventListener {
		private StepRequest fStepRequest;

		protected void step() throws DebugException {
			JDXStackFrame top = (JDXStackFrame)getTopStackFrame();

			if(top == null) {
				return;
			}

			Location location = top.getUnderlyingStackFrame().location();

			IType type = getType(location);
			int lineNumber = type != null ? type.getCompilationUnit().getSourceLineNumber(location.lineNumber() - 1) : -1;

			setOriginalStepKind(getStepKind());
			setOriginalStepLocation(location);
			setOriginalStepStackDepth(computeStackFrames().size());
			setOriginalStepType(type);
			setOriginalStepSourceLineNumber(lineNumber);

			setStepRequest(createStepRequest());
			setPendingStepHandler(this);
			addEventListener(this, getStepRequest());
			setRunning(true);
			preserveStackFrames();
			fireResumeEvent(getStepDetail());
			invokeThread();
		}

		protected void invokeThread() throws DebugException {
			try {
				getUnderlyingThread().resume();
			} catch(RuntimeException e) {
				stepEnd();
				targetRequestFailed(MessageFormat.format(JDXMessages.JDXThread_exception_stepping, new Object[] { e.toString() }), e);
			}
		}

		protected StepRequest createStepRequest() throws DebugException {
			EventRequestManager manager = getEventRequestManager();
			if(manager == null) {
				requestFailed(JDXMessages.JDXThread_Unable_to_create_step_request___VM_disconnected__1, null);
			}
			try {
				StepRequest request = manager.createStepRequest(getUnderlyingThread(), StepRequest.STEP_LINE, getStepKind());
				request.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
				request.addCountFilter(1);
				request.enable();
				return request;
			} catch(RuntimeException e) {
				targetRequestFailed(MessageFormat.format(JDXMessages.JDXThread_exception_creating_step_request, new Object[] { e.toString() }), e);
			}
			return null;
		}

		protected abstract int getStepKind();

		protected abstract int getStepDetail();

		protected void setStepRequest(StepRequest request) {
			fStepRequest = request;
		}

		protected StepRequest getStepRequest() {
			return fStepRequest;
		}

		protected void deleteStepRequest() {
			removeEventListener(this, getStepRequest());
			try {
				EventRequestManager manager = getEventRequestManager();
				if(manager != null) {
					manager.deleteEventRequest(getStepRequest());
				}
				setStepRequest(null);
			} catch(RuntimeException e) {
				logError(e);
			}
		}

		@Override
		public boolean handleEvent(Event event, JDXDebugTarget target) {
			try {
				StepEvent stepEvent = (StepEvent)event;
				Location currentLocation = stepEvent.location();

				if(shouldDoExtraStepInto(currentLocation)) {
					setRunning(true);
					deleteStepRequest();
					createSecondaryStepRequest();
					return true;
				}
				stepEnd();
				return false;
			} catch(DebugException e) {
				logError(e);
				stepEnd();
				return false;
			}
		}

		@Override
		public void wonSuspendVote(Event event, JDXDebugTarget target) {
		}

		protected void stepEnd() {
			setRunning(false);
			deleteStepRequest();
			setPendingStepHandler(null);
			setOriginalStepType(null);
			queueSuspendEvent(DebugEvent.STEP_END);
		}

		protected void createSecondaryStepRequest() throws DebugException {
			setStepRequest(createStepRequest());
			setPendingStepHandler(this);
			addEventListener(this, getStepRequest());
		}

		protected void abort() {
			if(getStepRequest() != null) {
				deleteStepRequest();
				setPendingStepHandler(null);
			}
		}
	}

	class StepOverHandler extends StepHandler {
		@Override
		protected int getStepKind() {
			return StepRequest.STEP_OVER;
		}

		@Override
		protected int getStepDetail() {
			return DebugEvent.STEP_OVER;
		}
	}

	class StepIntoHandler extends StepHandler {
		@Override
		protected int getStepKind() {
			return StepRequest.STEP_INTO;
		}

		@Override
		protected int getStepDetail() {
			return DebugEvent.STEP_INTO;
		}
	}

	class StepReturnHandler extends StepHandler {
		@Override
		protected int getStepKind() {
			return StepRequest.STEP_OUT;
		}

		@Override
		protected int getStepDetail() {
			return DebugEvent.STEP_RETURN;
		}
	}

	class StepToFrameHandler extends StepReturnHandler {
		private int fRemainingFrames;

		protected StepToFrameHandler(IStackFrame frame) throws DebugException {
			List<IStackFrame> frames = computeStackFrames();
			setRemainingFrames(frames.size() - frames.indexOf(frame));
		}

		protected void setRemainingFrames(int num) {
			fRemainingFrames = num;
		}

		protected int getRemainingFrames() {
			return fRemainingFrames;
		}

		@Override
		public boolean handleEvent(Event event, JDXDebugTarget target) {
			try {
				int numFrames = getUnderlyingFrameCount();
				if(numFrames <= getRemainingFrames()) {
					stepEnd();
					return false;
				}
				setRunning(true);
				deleteStepRequest();
				createSecondaryStepRequest();
				return true;
			} catch(DebugException e) {
				logError(e);
				stepEnd();
				return false;
			}
		}
	}

	@Override
	public boolean hasStackFrames() {
		return isSuspended();
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		if(adapter == IThread.class) {
			return this;
		}
		if(adapter == IStackFrame.class) {
			try {
				return getTopStackFrame();
			} catch(DebugException e) {
			}
		}
		return super.getAdapter(adapter);
	}

	static class ThreadJob extends Job {
		private Vector<Runnable> fRunnables;
		private JDXThread fJDXThread;

		public ThreadJob(JDXThread thread) {
			super(JDXMessages.JDXThread_39);
			fJDXThread = thread;
			fRunnables = new Vector<Runnable>(5);
			setSystem(true);
		}

		public void addRunnable(Runnable runnable) {
			synchronized(fRunnables) {
				fRunnables.add(runnable);
			}
			schedule();
		}

		public boolean isEmpty() {
			return fRunnables.isEmpty();
		}

		@Override
		public IStatus run(IProgressMonitor monitor) {
			fJDXThread.fRunningAsyncJob = this;

			Object[] runnables;
			synchronized(fRunnables) {
				runnables = fRunnables.toArray();
				fRunnables.clear();
			}
			MultiStatus failed = null;
			monitor.beginTask(this.getName(), runnables.length);
			int i = 0;
			while(i < runnables.length && !fJDXThread.isTerminated() && !monitor.isCanceled()) {
				try {
					((Runnable)runnables[i]).run();
				} catch(Exception e) {
					if(failed == null) {
						failed = new MultiStatus(Plugin.getUniqueIdentifier(), JDXDebug.INTERNAL_ERROR, JDXMessages.JDXThread_0, null);
					}
					failed.add(new Status(IStatus.ERROR, Plugin.getUniqueIdentifier(), JDXDebug.INTERNAL_ERROR, JDXMessages.JDXThread_0, e));
				}
				i++;
				monitor.worked(1);
			}
			fJDXThread.fRunningAsyncJob = null;
			monitor.done();
			if(failed == null) {
				return Status.OK_STATUS;
			}
			return failed;
		}

		@Override
		public boolean shouldRun() {
			return !fJDXThread.isTerminated() && !fRunnables.isEmpty();
		}
	}

	public void stop(IValue exception) throws DebugException {
		try {
			getUnderlyingThread().stop(((JDXObjectValue)exception).getUnderlyingObject());
		} catch(InvalidTypeException e) {
			targetRequestFailed(MessageFormat.format(JDXMessages.JDXThread_exception_stoping_thread, new Object[] { e.toString() }), e);
		}
	}
}
