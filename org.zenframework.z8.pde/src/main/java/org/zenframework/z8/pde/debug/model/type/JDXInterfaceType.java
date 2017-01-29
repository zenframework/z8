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

public class JDXInterfaceType extends JDXReferenceType {
	public JDXInterfaceType(JDXDebugTarget target, JDXThread thread, JDXVariable variable, InterfaceType type) {
		super(target, thread, variable, type);
	}

	public JDXType[] getImplementors() throws DebugException {
		try {
			List<ClassType> implementorList = ((InterfaceType)getUnderlyingType()).implementors();
			List<JDXType> jdxClassTypeList = new ArrayList<JDXType>(implementorList.size());
			Iterator<ClassType> iterator = implementorList.iterator();
			while(iterator.hasNext()) {
				ClassType classType = iterator.next();
				if(classType != null) {
					jdxClassTypeList.add(JDXType.createType(getDebugTarget(), getJDXThread(), getJDXVariable(), classType));
				}
			}
			return jdxClassTypeList.toArray(new JDXType[jdxClassTypeList.size()]);
		} catch(RuntimeException re) {
			getDebugTarget().targetRequestFailed(MessageFormat.format(JDXMessages.JDXClassType_exception_while_retrieving_superclass, new Object[] { re.toString() }), re);
		}
		return new JDXType[0];
	}

	public JDXType[] getSubInterfaces() throws DebugException {
		try {
			List<InterfaceType> subList = ((InterfaceType)getUnderlyingType()).subinterfaces();
			List<JDXType> jdxInterfaceTypeList = new ArrayList<JDXType>(subList.size());
			Iterator<InterfaceType> iterator = subList.iterator();
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

	public JDXType[] getSuperInterfaces() throws DebugException {
		try {
			List<InterfaceType> superList = ((InterfaceType)getUnderlyingType()).superinterfaces();
			List<JDXType> jdxInterfaceTypeList = new ArrayList<JDXType>(superList.size());
			Iterator<InterfaceType> iterator = superList.iterator();
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
}
