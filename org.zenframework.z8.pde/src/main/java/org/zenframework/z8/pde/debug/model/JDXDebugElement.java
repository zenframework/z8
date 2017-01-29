package org.zenframework.z8.pde.debug.model;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.DebugElement;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IDisconnect;

import org.zenframework.z8.pde.Plugin;
import org.zenframework.z8.pde.debug.JDXMessages;

import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;

public abstract class JDXDebugElement extends DebugElement implements IDisconnect {
	public JDXDebugElement(JDXDebugTarget target) {
		super(target);
	}

	protected void logError(Exception e) {
		if(!((JDXDebugTarget)getDebugTarget()).isAvailable()) {
			if(e instanceof VMDisconnectedException || (e instanceof CoreException && ((CoreException)e).getStatus().getException() instanceof VMDisconnectedException)) {
				return;
			}
		}
		Plugin.log(e);
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object getAdapter(Class adapter) {
		if(adapter == IDebugElement.class) {
			return this;
		}
		if(adapter == IDebugTarget.class) {
			return getDebugTarget();
		}
		return super.getAdapter(adapter);
	}

	@Override
	public String getModelIdentifier() {
		return JDXDebugModel.getModelIdentifier();
	}

	public void queueEvent(DebugEvent event) {
		JDXEventDispatcher dispatcher = getJDXDebugTarget().getEventDispatcher();
		if(dispatcher != null) {
			dispatcher.queue(event);
		}
	}

	@Override
	public void fireSuspendEvent(int detail) {
		getJDXDebugTarget().incrementSuspendCount(detail);
		super.fireSuspendEvent(detail);
	}

	public void queueSuspendEvent(int detail) {
		getJDXDebugTarget().incrementSuspendCount(detail);
		queueEvent(new DebugEvent(this, DebugEvent.SUSPEND, detail));
	}

	public void requestFailed(String message, Exception e) throws DebugException {
		requestFailed(message, e, DebugException.REQUEST_FAILED);
	}

	public void targetRequestFailed(String message, RuntimeException e) throws DebugException {
		if(e == null || e.getClass().getName().startsWith("com.sun.jdi")) {
			requestFailed(message, e, DebugException.TARGET_REQUEST_FAILED);
		} else {
			throw e;
		}
	}

	public void requestFailed(String message, Throwable e, int code) throws DebugException {
		throwDebugException(message, code, e);
	}

	public void targetRequestFailed(String message, Throwable e) throws DebugException {
		throwDebugException(message, DebugException.TARGET_REQUEST_FAILED, e);
	}

	public void notSupported(String message) throws DebugException {
		throwDebugException(message, DebugException.NOT_SUPPORTED, null);
	}

	protected void throwDebugException(String message, int code, Throwable exception) throws DebugException {
		throw new DebugException(new Status(IStatus.ERROR, JDXDebugModel.getModelIdentifier(), code, message, exception));
	}

	public void internalError(RuntimeException e) {
		if(e.getClass().getName().startsWith("com.sun.jdi")) {
			logError(e);
		} else {
			throw e;
		}
	}

	protected void internalError(String message) {
		logError(new DebugException(new Status(IStatus.ERROR, JDXDebugModel.getModelIdentifier(), DebugException.INTERNAL_ERROR, message, null)));
	}

	protected String getUnknownMessage() {
		return JDXMessages.JDXDebugElement_unknown;
	}

	public JDXDebugTarget getJDXDebugTarget() {
		return (JDXDebugTarget)getDebugTarget();
	}

	protected VirtualMachine getVM() {
		return getJDXDebugTarget().getVM();
	}

	public EventRequestManager getEventRequestManager() {
		VirtualMachine vm = getVM();
		if(vm == null) {
			return null;
		}
		return vm.eventRequestManager();
	}

	public void addEventListener(JDXEventListener listener, EventRequest request) {
		JDXEventDispatcher dispatcher = getJDXDebugTarget().getEventDispatcher();
		if(dispatcher != null) {
			dispatcher.addEventListener(listener, request);
		}
	}

	public void removeEventListener(JDXEventListener listener, EventRequest request) {
		JDXEventDispatcher dispatcher = getJDXDebugTarget().getEventDispatcher();
		if(dispatcher != null) {
			dispatcher.removeEventListener(listener, request);
		}
	}

	protected void disconnected() {
		if(getDebugTarget() != null) {
			getJDXDebugTarget().disconnected();
		}
	}

	@Override
	public boolean canDisconnect() {
		return getDebugTarget().canDisconnect();
	}

	@Override
	public void disconnect() throws DebugException {
		getDebugTarget().disconnect();
	}

	@Override
	public boolean isDisconnected() {
		return getDebugTarget().isDisconnected();
	}
}
