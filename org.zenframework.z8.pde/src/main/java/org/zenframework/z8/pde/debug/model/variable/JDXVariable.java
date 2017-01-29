package org.zenframework.z8.pde.debug.model.variable;

import java.text.MessageFormat;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;

import org.zenframework.z8.pde.debug.JDXMessages;
import org.zenframework.z8.pde.debug.model.JDXDebugElement;
import org.zenframework.z8.pde.debug.model.JDXDebugTarget;
import org.zenframework.z8.pde.debug.model.JDXThread;
import org.zenframework.z8.pde.debug.model.type.JDXType;
import org.zenframework.z8.pde.debug.model.value.JDXValue;

import com.sun.jdi.Type;
import com.sun.jdi.Value;

public abstract class JDXVariable extends JDXDebugElement implements IVariable {
	private JDXValue m_value;
	private JDXThread m_thread;

	private int m_lastChangeIndex = -1;

	protected final static String m_jdxStringSignature = "Ljava/lang/String;";

	public JDXVariable(JDXDebugTarget target, JDXThread thread) {
		super(target);
		m_thread = thread;
	}

	public JDXThread getJDXThread() {
		return m_thread;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		if(adapter == IVariable.class) {
			return this;
		}
		return super.getAdapter(adapter);
	}

	protected final Value getCurrentValue() throws DebugException {
		try {
			return retrieveValue();
		} catch(RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDXMessages.JDXVariable_exception_retrieving, new Object[] { e.toString() }), e);
			return null;
		}
	}

	protected abstract Value retrieveValue() throws DebugException;

	@Override
	public IValue getValue() throws DebugException {
		Value currentValue = getCurrentValue();
		if(m_value == null) {
			m_value = JDXValue.createValue(getJDXDebugTarget(), m_thread, this, currentValue);
		} else {
			Value previousValue = m_value.getUnderlyingValue();
			if(currentValue == previousValue) {
				return m_value;
			}
			if(previousValue == null || currentValue == null) {
				m_value = JDXValue.createValue(getJDXDebugTarget(), m_thread, this, currentValue);
				setChangeCount(getJDXDebugTarget().getSuspendCount());
			} else if(!previousValue.equals(currentValue)) {
				m_value = JDXValue.createValue((JDXDebugTarget)getDebugTarget(), m_thread, this, currentValue);
				setChangeCount(getJDXDebugTarget().getSuspendCount());
			}
		}
		return m_value;
	}

	@Override
	public boolean supportsValueModification() {
		return false;
	}

	@Override
	public void setValue(String expression) throws DebugException {
		notSupported(JDXMessages.JDXVariable_does_not_support_value_modification);
	}

	@Override
	public void setValue(IValue value) throws DebugException {
		notSupported(JDXMessages.JDXVariable_does_not_support_value_modification);
	}

	@Override
	public boolean verifyValue(String expression) {
		return false;
	}

	@Override
	public boolean verifyValue(IValue value) {
		return false;
	}

	public boolean isLocal() {
		return false;
	}

	public JDXType getJDXType() throws DebugException {
		return JDXType.createType(getJDXDebugTarget(), getJDXThread(), this, getUnderlyingType());
	}

	protected abstract Type getUnderlyingType() throws DebugException;

	protected Value getLastKnownValue() {
		if(m_value == null) {
			return null;
		}
		return m_value.getUnderlyingValue();
	}

	protected void setChangeCount(int count) {
		m_lastChangeIndex = count;
	}

	protected int getChangeCount() {
		return m_lastChangeIndex;
	}

	@Override
	public boolean hasValueChanged() {
		return getChangeCount() == getJDXDebugTarget().getSuspendCount();
	}
}
