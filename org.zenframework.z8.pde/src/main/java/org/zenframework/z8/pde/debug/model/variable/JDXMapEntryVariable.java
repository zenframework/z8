package org.zenframework.z8.pde.debug.model.variable;

import java.text.MessageFormat;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;

import org.zenframework.z8.pde.Plugin;
import org.zenframework.z8.pde.debug.JDXMessages;
import org.zenframework.z8.pde.debug.model.JDXDebugTarget;
import org.zenframework.z8.pde.debug.model.JDXThread;
import org.zenframework.z8.pde.debug.model.value.JDXMapValue;
import org.zenframework.z8.pde.debug.model.value.JDXValue;

import com.sun.jdi.ArrayType;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.Type;
import com.sun.jdi.Value;

public class JDXMapEntryVariable extends JDXModificationVariable {
	private int m_index;
	private JDXMapValue m_map;
	private String m_referenceTypeName = null;

	public JDXMapEntryVariable(JDXDebugTarget target, JDXThread thread, JDXMapValue map, int index) {
		super(target, thread);
		m_map = map;
		m_index = index;
	}

	@Override
	protected Value retrieveValue() {
		try {
			return m_map.getUnderlyingValue(getIndex());
		} catch(DebugException e) {
			Plugin.log(e);
		}
		return null;
	}

	@Override
	public String getName() {
		return "[" + getIndex() + "]";
	}

	@Override
	protected void setJDXValue(Value value) {
		assert (false);
		/*
		 * ObjectReference ar = getArrayReference();
		 * 
		 * if(ar == null) { requestFailed(JDXMessages.
		 * JDXArrayEntryVariable_value_modification_failed, null); //$NON-NLS-1$
		 * }
		 * 
		 * try { ar.setValue(getIndex(), value);
		 * fireChangeEvent(DebugEvent.CONTENT); } catch(ClassNotLoadedException
		 * e) { targetRequestFailed(MessageFormat.format(JDXMessages.
		 * JDXArrayEntryVariable_exception_modifying_variable_value, new
		 * Object[] { e.toString() }), e); } catch(InvalidTypeException e) {
		 * targetRequestFailed(MessageFormat.format(JDXMessages.
		 * JDXArrayEntryVariable_exception_modifying_variable_value, new
		 * Object[] { e.toString() }), e); } catch(RuntimeException e) {
		 * targetRequestFailed(MessageFormat.format(JDXMessages.
		 * JDXArrayEntryVariable_exception_modifying_variable_value, new
		 * Object[] { e.toString() }), e); }
		 */}

	public JDXMapValue getMap() {
		return m_map;
	}

	protected ObjectReference getMapReference() {
		return m_map.getUnderlyingObject();
	}

	protected int getIndex() {
		return m_index;
	}

	@Override
	public String getReferenceTypeName() throws DebugException {
		if(m_referenceTypeName == null) {
			m_referenceTypeName = stripBrackets(m_map.getReferenceTypeName());
		}
		return m_referenceTypeName;
	}

	protected String stripBrackets(String typeName) {
		int lastLeft = typeName.lastIndexOf("[");

		if(lastLeft < 0) {
			return typeName;
		}

		return typeName.substring(0, lastLeft);
	}

	@Override
	public void setValue(IValue v) throws DebugException {
		if(verifyValue(v)) {
			JDXValue value = (JDXValue)v;
			setJDXValue(value.getUnderlyingValue());
		}
	}

	@Override
	protected Type getUnderlyingType() throws DebugException {
		try {
			return ((ArrayType)getMapReference().type()).componentType();
		} catch(ClassNotLoadedException e) {
			targetRequestFailed(MessageFormat.format(JDXMessages.JDXArrayEntryVariable_exception_while_retrieving_type_of_array_entry, new Object[] { e.toString() }), e);
		} catch(RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDXMessages.JDXArrayEntryVariable_exception_while_retrieving_type_of_array_entry, new Object[] { e.toString() }), e);
		}
		return null;
	}
}
