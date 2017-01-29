package org.zenframework.z8.pde.debug.model.variable;

import org.eclipse.debug.core.DebugException;

import org.zenframework.z8.pde.debug.model.JDXDebugTarget;
import org.zenframework.z8.pde.debug.model.JDXThread;

import com.sun.jdi.Field;
import com.sun.jdi.ObjectReference;

public class JDXMapEntryFieldVariable extends JDXFieldVariable {
	static final public int KEY_FIELD = 0;
	static final public int VALUE_FIELD = 1;

	private int m_fieldType;
	private JDXMapEntryVariable m_variable;

	public JDXMapEntryFieldVariable(JDXDebugTarget target, JDXThread thread, Field field, int fieldType, JDXMapEntryVariable variable) {
		super(target, thread, field, (ObjectReference)variable.retrieveValue());

		m_fieldType = fieldType;
		m_variable = variable;
	}

	@Override
	public String getReferenceTypeName() throws DebugException {
		String typeName = m_variable.getMap().getReferenceTypeName();

		int index = typeName.lastIndexOf('[');

		if(m_fieldType == VALUE_FIELD) {
			return typeName.substring(0, index);
		} else if(m_fieldType == KEY_FIELD) {
			return typeName.substring(index + 1, typeName.length() - 1);
		}

		assert (false);
		return null;
	}

}
