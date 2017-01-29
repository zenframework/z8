package org.zenframework.z8.pde.debug.model.value;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;

import org.zenframework.z8.pde.debug.JDXMessages;
import org.zenframework.z8.pde.debug.model.JDXDebugTarget;
import org.zenframework.z8.pde.debug.model.JDXThread;
import org.zenframework.z8.pde.debug.model.variable.JDXMapEntryFieldVariable;
import org.zenframework.z8.pde.debug.model.variable.JDXMapEntryVariable;
import org.zenframework.z8.pde.debug.model.variable.JDXVariable;

import com.sun.jdi.Field;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;

public class JDXMapEntryValue extends JDXObjectValue {
	public JDXMapEntryValue(JDXDebugTarget target, JDXThread thread, JDXVariable variable, ObjectReference object) {
		super(target, thread, variable, object);
	}

	@Override
	protected synchronized List<IVariable> getVariablesList() throws DebugException {
		List<IVariable> variables = new ArrayList<IVariable>();

		ObjectReference object = getUnderlyingObject();
		ReferenceType refType = object.referenceType();

		try {
			Field keyField = refType.fieldByName("key");
			Field valueField = refType.fieldByName("value");

			variables.add(new JDXMapEntryFieldVariable(getJDXDebugTarget(), getJDXThread(), keyField, JDXMapEntryFieldVariable.KEY_FIELD, (JDXMapEntryVariable)getJDXVariable()));
			variables.add(new JDXMapEntryFieldVariable(getJDXDebugTarget(), getJDXThread(), valueField, JDXMapEntryFieldVariable.VALUE_FIELD, (JDXMapEntryVariable)getJDXVariable()));
		} catch(ObjectCollectedException e) {
			return new ArrayList<IVariable>();
		} catch(RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDXMessages.JDXValue_exception_retrieving_fields, new Object[] { e.toString() }), e);
			return null;
		}

		return variables;
	}

	@Override
	public String getReferenceTypeName() {
		return "";
	}
}
