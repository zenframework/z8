package org.zenframework.z8.pde.debug.model.type;

import java.text.MessageFormat;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;

import org.zenframework.z8.pde.debug.JDXMessages;
import org.zenframework.z8.pde.debug.model.JDXDebugModel;
import org.zenframework.z8.pde.debug.model.JDXDebugTarget;
import org.zenframework.z8.pde.debug.model.JDXThread;
import org.zenframework.z8.pde.debug.model.variable.JDXVariable;

import com.sun.jdi.ArrayType;
import com.sun.jdi.ClassType;
import com.sun.jdi.InterfaceType;
import com.sun.jdi.Type;

public class JDXType {
	private Type m_type;
	private JDXDebugTarget m_debugTarget;
	private JDXThread m_thread;
	private JDXVariable m_variable;

	protected JDXType(JDXDebugTarget target, JDXThread thread, JDXVariable variable, Type type) {
		setDebugTarget(target);
		setUnderlyingType(type);
		setJDXThread(thread);
	}

	public void requestFailed(String message, Throwable e, int code) throws DebugException {
		throwDebugException(message, code, e);
	}

	protected void throwDebugException(String message, int code, Throwable exception) throws DebugException {
		throw new DebugException(new Status(IStatus.ERROR, JDXDebugModel.getModelIdentifier(), code, message, exception));
	}

	public void targetRequestFailed(String message, RuntimeException e) throws DebugException {
		if(e == null || e.getClass().getName().startsWith("com.sun.jdi")) {
			requestFailed(message, e, DebugException.TARGET_REQUEST_FAILED);
		} else {
			throw e;
		}
	}

	public static JDXType createType(JDXDebugTarget target, JDXThread thread, JDXVariable variable, Type type) {
		if(type instanceof ArrayType) {
			return new JDXArrayType(target, thread, variable, (ArrayType)type);
		}
		if(type instanceof ClassType) {
			return new JDXClassType(target, thread, variable, (ClassType)type);
		}
		if(type instanceof InterfaceType) {
			return new JDXInterfaceType(target, thread, variable, (InterfaceType)type);
		}
		return new JDXType(target, thread, variable, type);
	}

	public String getSignature() throws DebugException {
		try {
			return getUnderlyingType().signature();
		} catch(RuntimeException e) {
			getDebugTarget().targetRequestFailed(MessageFormat.format(JDXMessages.JDXType_exception_while_retrieving_signature, new Object[] { e.toString() }), e);
			return null;
		}
	}

	protected JDXDebugTarget getDebugTarget() {
		return m_debugTarget;
	}

	protected void setDebugTarget(JDXDebugTarget debugTarget) {
		m_debugTarget = debugTarget;
	}

	protected void setJDXThread(JDXThread thread) {
		m_thread = thread;
	}

	protected JDXThread getJDXThread() {
		return m_thread;
	}

	protected void setJDXVariable(JDXVariable variable) {
		m_variable = variable;
	}

	protected JDXVariable getJDXVariable() {
		return m_variable;
	}

	public Type getUnderlyingType() {
		return m_type;
	}

	protected void setUnderlyingType(Type type) {
		m_type = type;
	}

	@Override
	public String toString() {
		return getUnderlyingType().toString();
	}

	public String getName() throws DebugException {
		try {
			return getUnderlyingType().name();
		} catch(RuntimeException e) {
			getDebugTarget().targetRequestFailed(MessageFormat.format(JDXMessages.JDXType_exception_while_retrieving_type_name, new Object[] { e.toString() }), e);
		}
		return null;
	}

	/**
	 * @see java.lang.Object#equals(Object)
	 */
	@Override
	public boolean equals(Object object) {
		return object instanceof JDXType && m_type.equals(((JDXType)object).m_type);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return m_type.hashCode();
	}
}
