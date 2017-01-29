package org.zenframework.z8.pde.debug.model.type;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.debug.core.DebugException;

import org.zenframework.z8.pde.debug.JDXMessages;
import org.zenframework.z8.pde.debug.model.JDXDebugTarget;
import org.zenframework.z8.pde.debug.model.JDXThread;
import org.zenframework.z8.pde.debug.model.variable.JDXVariable;

import com.sun.jdi.ClassType;
import com.sun.jdi.InterfaceType;

public class JDXClassType extends JDXReferenceType {
	public JDXClassType(JDXDebugTarget target, JDXThread thread, JDXVariable variable, ClassType type) {
		super(target, thread, variable, type);
	}

	public JDXType getSuperclass() throws DebugException {
		try {
			ClassType superclazz = ((ClassType)getUnderlyingType()).superclass();
			if(superclazz != null) {
				return JDXType.createType(getDebugTarget(), getJDXThread(), getJDXVariable(), superclazz);
			}
		} catch(RuntimeException e) {
			getDebugTarget().targetRequestFailed(MessageFormat.format(JDXMessages.JDXClassType_exception_while_retrieving_superclass, new Object[] { e.toString() }), e);
		}
		return null;
	}

	public JDXType[] getAllInterfaces() throws DebugException {
		try {
			List<InterfaceType> interfaceList = ((ClassType)getUnderlyingType()).allInterfaces();
			List<JDXType> jdxInterfaceTypeList = new ArrayList<JDXType>(interfaceList.size());
			Iterator<InterfaceType> iterator = interfaceList.iterator();
			while(iterator.hasNext()) {
				InterfaceType interfaceType = iterator.next();
				if(interfaceType != null) {
					jdxInterfaceTypeList.add(JDXType.createType(getDebugTarget(), getJDXThread(), getJDXVariable(), interfaceType));
				}
			}
			JDXType[] jdxInterfaceTypeArray = new JDXType[jdxInterfaceTypeList.size()];
			jdxInterfaceTypeArray = jdxInterfaceTypeList.toArray(jdxInterfaceTypeArray);
			return jdxInterfaceTypeArray;
		} catch(RuntimeException re) {
			getDebugTarget().targetRequestFailed(MessageFormat.format(JDXMessages.JDXClassType_exception_while_retrieving_superclass, new Object[] { re.toString() }), re);
		}
		return new JDXType[0];
	}

	public JDXType[] getInterfaces() throws DebugException {
		try {
			List<InterfaceType> interfaceList = ((ClassType)getUnderlyingType()).interfaces();
			List<JDXType> jdxInterfaceTypeList = new ArrayList<JDXType>(interfaceList.size());
			Iterator<InterfaceType> iterator = interfaceList.iterator();
			while(iterator.hasNext()) {
				InterfaceType interfaceType = iterator.next();
				if(interfaceType != null) {
					jdxInterfaceTypeList.add(JDXType.createType(getDebugTarget(), getJDXThread(), getJDXVariable(), interfaceType));
				}
			}
			return jdxInterfaceTypeList.toArray(new JDXType[jdxInterfaceTypeList.size()]);
		} catch(RuntimeException re) {
			getDebugTarget().targetRequestFailed(MessageFormat.format(JDXMessages.JDXClassType_exception_while_retrieving_superclass, new Object[] { re.toString() }), re);
		}
		return new JDXType[0];
	}

	public boolean isEnum() {
		return ((ClassType)getReferenceType()).isEnum();
	}
}
