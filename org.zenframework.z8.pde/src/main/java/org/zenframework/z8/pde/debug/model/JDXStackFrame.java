package org.zenframework.z8.pde.debug.model;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;

import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.workspace.Workspace;
import org.zenframework.z8.pde.debug.JDXMessages;
import org.zenframework.z8.pde.debug.model.type.JDXClassType;
import org.zenframework.z8.pde.debug.model.type.JDXReferenceType;
import org.zenframework.z8.pde.debug.model.type.JDXType;
import org.zenframework.z8.pde.debug.model.value.JDXValue;
import org.zenframework.z8.pde.debug.model.variable.JDXFieldVariable;
import org.zenframework.z8.pde.debug.model.variable.JDXLocalVariable;
import org.zenframework.z8.pde.debug.model.variable.JDXThisVariable;
import org.zenframework.z8.pde.debug.util.Signature;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassType;
import com.sun.jdi.Field;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.NativeMethodException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.Type;

public class JDXStackFrame extends JDXDebugElement implements IStackFrame {
	public static final int ERR_INVALID_STACK_FRAME = 130;

	private int m_depth = -2;

	private StackFrame m_stackFrame;

	private JDXThread m_thread;

	private List<IVariable> m_variables;
	private ObjectReference m_thisObject;
	private String m_receivingTypeName;
	private boolean m_refreshVariables = true;
	private boolean m_localsAvailable = true;
	private Location m_location;

	public JDXStackFrame(JDXThread thread, StackFrame frame, int depth) {
		super(thread.getJDXDebugTarget());
		setThread(thread);
		bind(frame, depth);
	}

	protected JDXStackFrame bind(StackFrame frame, int depth) {
		synchronized(m_thread) {
			if(m_depth == -2) {
				// first initialization
				m_stackFrame = frame;
				m_depth = depth;
				m_location = frame.location();
				return this;
			} else if(depth == -1) {
				// mark as invalid
				m_depth = -1;
				m_stackFrame = null;
				return null;
			} else if(m_depth == depth) {
				Location location = frame.location();
				if(location.method().equals(m_location.method())) {
					m_stackFrame = frame;
					m_location = location;
					clearCachedData();
					return this;
				}
			}
			// invalidate this franme
			bind(null, -1);
			// return a new frame
			return new JDXStackFrame(m_thread, frame, depth);
		}
	}

	@Override
	public IThread getThread() {
		return m_thread;
	}

	public JDXThread getJDXThread() {
		return m_thread;
	}

	@Override
	public boolean canResume() {
		return getThread().canResume();
	}

	@Override
	public boolean canSuspend() {
		return getThread().canSuspend();
	}

	@Override
	public boolean canStepInto() {
		try {
			return exists() && isTopStackFrame() && getThread().canStepInto();
		} catch(DebugException e) {
			logError(e);
			return false;
		}
	}

	@Override
	public boolean canStepOver() {
		return exists() && getThread().canStepOver();
	}

	@Override
	public boolean canStepReturn() {
		try {
			if(!exists() || !getThread().canStepReturn()) {
				return false;
			}

			List<IStackFrame> frames = getJDXThread().computeStackFrames();

			if(frames != null && !frames.isEmpty()) {
				return !this.equals(frames.get(frames.size() - 1));
			}
		} catch(DebugException e) {
			logError(e);
		}
		return false;
	}

	public Method getUnderlyingMethod() {
		synchronized(m_thread) {
			return m_location.method();
		}
	}

	@Override
	public IVariable[] getVariables() throws DebugException {
		List<IVariable> list = getVariables0();
		return list.toArray(new IVariable[list.size()]);
	}

	protected List<IVariable> getVariables0() throws DebugException {
		synchronized(m_thread) {
			if(m_variables == null) {
				Method method = getUnderlyingMethod();
				m_variables = new ArrayList<IVariable>();

				if(method.isStatic()) {
					// add statics
					List<Field> allFields = null;

					ReferenceType declaringType = method.declaringType();

					try {
						allFields = declaringType.allFields();
					} catch(RuntimeException e) {
						targetRequestFailed(MessageFormat.format(JDXMessages.JDXStackFrame_exception_retrieving_fields, new Object[] { e.toString() }), e);
						return new ArrayList<IVariable>();
					}
					if(allFields != null) {
						Iterator<Field> fields = allFields.iterator();

						while(fields.hasNext()) {
							Field field = fields.next();

							if(field.isStatic()) {
								IType classType = getType();

								if(classType != null && classType.findMember(field.name()) != null) {
									m_variables.add(new JDXFieldVariable(getJDXDebugTarget(), m_thread, field, declaringType));
								}
							}
						}

						Collections.sort(m_variables, new Comparator<IVariable>() {
							@Override
							public int compare(IVariable a, IVariable b) {
								try {
									return a.getName().compareToIgnoreCase(b.getName());
								} catch(DebugException de) {
									logError(de);
									return -1;
								}
							}
						});
					}
				} else {
					ObjectReference t = getUnderlyingThisObject();

					if(t != null) {
						m_variables.add(new JDXThisVariable(getJDXDebugTarget(), getJDXThread(), t));
					}
				}

				Iterator<LocalVariable> variables = getUnderlyingVisibleVariables().iterator();

				while(variables.hasNext()) {
					LocalVariable var = variables.next();
					m_variables.add(new JDXLocalVariable(this, getJDXThread(), var));
				}
			} else if(m_refreshVariables) {
				updateVariables();
			}

			m_refreshVariables = false;
			return m_variables;
		}
	}

	@Override
	public String getName() throws DebugException {
		return getMethodName();
	}

	public List<String> getArgumentTypeNames() throws DebugException {
		try {
			Method underlyingMethod = getUnderlyingMethod();
			String genericSignature = underlyingMethod.genericSignature();

			if(genericSignature == null) {
				return underlyingMethod.argumentTypeNames();
			}

			String[] parameterTypes = Signature.getParameterTypes(genericSignature);
			List<String> argumentTypeNames = new ArrayList<String>();

			for(int i = 0; i < parameterTypes.length; i++) {
				argumentTypeNames.add(Signature.toString(parameterTypes[i]).replace('/', '.'));
			}

			return argumentTypeNames;
		} catch(RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDXMessages.JDXStackFrame_exception_retrieving_argument_type_names, new Object[] { e.toString() }), e);
			return null;
		}
	}

	@Override
	public int getLineNumber() throws DebugException {
		synchronized(m_thread) {
			try {
				IType classType = getType();

				if(classType == null) {
					return -1;
				}

				int t = m_location.lineNumber() - 1;
				int l = classType.getCompilationUnit().getSourceLineNumber(t);

				return l != -1 ? l + 1 : -1;
			} catch(RuntimeException e) {
				if(getThread().isSuspended()) {
					targetRequestFailed(MessageFormat.format(JDXMessages.JDXStackFrame_exception_retrieving_line_number, new Object[] { e.toString() }), e);
				}
			}
		}
		return -1;
	}

	@Override
	public boolean isStepping() {
		return getThread().isStepping();
	}

	@Override
	public boolean isSuspended() {
		return getThread().isSuspended();
	}

	@Override
	public void resume() throws DebugException {
		getThread().resume();
	}

	@Override
	public void stepInto() throws DebugException {
		if(!canStepInto()) {
			return;
		}
		getThread().stepInto();
	}

	@Override
	public void stepOver() throws DebugException {
		if(!canStepOver()) {
			return;
		}

		if(isTopStackFrame()) {
			getThread().stepOver();
		} else {
			getJDXThread().stepToFrame(this);
		}
	}

	@Override
	public void stepReturn() throws DebugException {
		if(!canStepReturn()) {
			return;
		}
		if(isTopStackFrame()) {
			getThread().stepReturn();
		} else {
			List<IStackFrame> frames = getJDXThread().computeStackFrames();

			int index = frames.indexOf(this);

			if(index >= 0 && index < frames.size() - 1) {
				IStackFrame nextFrame = frames.get(index + 1);
				getJDXThread().stepToFrame(nextFrame);
			}
		}
	}

	@Override
	public void suspend() throws DebugException {
		getThread().suspend();
	}

	protected void updateVariables() throws DebugException {
		if(m_variables == null) {
			return;
		}
		Method method = getUnderlyingMethod();
		int index = 0;
		if(!method.isStatic()) {
			// update "this"
			ObjectReference thisObject;
			try {
				thisObject = getUnderlyingThisObject();
			} catch(DebugException exception) {
				if(!getThread().isSuspended()) {
					thisObject = null;
				} else {
					throw exception;
				}
			}
			JDXThisVariable oldThisObject = null;
			if(!m_variables.isEmpty() && m_variables.get(0) instanceof JDXThisVariable) {
				oldThisObject = (JDXThisVariable)m_variables.get(0);
			}
			if(thisObject == null && oldThisObject != null) {
				// removal of 'this'
				m_variables.remove(0);
				index = 0;
			} else {
				if(oldThisObject == null && thisObject != null) {
					oldThisObject = new JDXThisVariable((JDXDebugTarget)getDebugTarget(), getJDXThread(), thisObject);
					m_variables.add(0, oldThisObject);
					index = 1;
				} else {
					if(oldThisObject != null) {
						// 'this' still exists, replace with new 'this' if a
						// different receiver
						if(!oldThisObject.retrieveValue().equals(thisObject)) {
							m_variables.remove(0);
							m_variables.add(0, new JDXThisVariable((JDXDebugTarget)getDebugTarget(), getJDXThread(), thisObject));
						}
						index = 1;
					}
				}
			}
		}

		List<LocalVariable> locals = getUnderlyingVisibleVariables();

		int localIndex = -1;

		while(index < m_variables.size()) {
			Object var = m_variables.get(index);

			if(var instanceof JDXLocalVariable) {
				JDXLocalVariable local = (JDXLocalVariable)m_variables.get(index);
				localIndex = locals.indexOf(local.getLocal());
				if(localIndex >= 0) {
					// update variable with new underling JDX LocalVariable
					local.setLocal((LocalVariable)locals.get(localIndex));
					locals.remove(localIndex);
					index++;
				} else {
					// remove variable
					m_variables.remove(index);
				}
			} else {
				index++;
			}
		}

		Iterator<LocalVariable> newOnes = locals.iterator();

		while(newOnes.hasNext()) {
			JDXLocalVariable local = new JDXLocalVariable(this, getJDXThread(), newOnes.next());
			m_variables.add(local);
		}
	}

	public IVariable findVariable(String varName) throws DebugException {
		IVariable[] variables = getVariables();
		IVariable thisVariable = null;

		for(int i = 0; i < variables.length; i++) {
			IVariable var = variables[i];

			if(var.getName().equals(varName)) {
				return var;
			}
			if(var instanceof JDXThisVariable) {
				thisVariable = var;
			}
		}
		if(thisVariable != null) {
			IVariable[] thisChildren = thisVariable.getValue().getVariables();
			for(int i = 0; i < thisChildren.length; i++) {
				IVariable var = thisChildren[i];
				if(var.getName().equals(varName)) {
					return var;
				}
			}
		}
		return null;
	}

	protected List<LocalVariable> getUnderlyingVisibleVariables() throws DebugException {
		synchronized(m_thread) {
			List<LocalVariable> variables = new ArrayList<LocalVariable>();

			try {
				variables = getUnderlyingStackFrame().visibleVariables();
			} catch(AbsentInformationException e) {
				setLocalsAvailable(false);
			} catch(NativeMethodException e) {
				setLocalsAvailable(false);
			} catch(RuntimeException e) {
				targetRequestFailed(MessageFormat.format(JDXMessages.JDXStackFrame_exception_retrieving_visible_variables_2, new Object[] { e.toString() }), e);
			}

			List<LocalVariable> visibleVariables = new ArrayList<LocalVariable>();

			for(int i = 0; i < variables.size(); i++) {
				LocalVariable local = variables.get(i);

				if(!local.name().startsWith("__")) {
					visibleVariables.add(local);
				}
			}

			return visibleVariables;
		}
	}

	protected ObjectReference getUnderlyingThisObject() throws DebugException {
		synchronized(m_thread) {
			if((m_stackFrame == null || m_thisObject == null)) {
				try {
					m_thisObject = getUnderlyingStackFrame().thisObject();
				} catch(RuntimeException e) {
					targetRequestFailed(MessageFormat.format(JDXMessages.JDXStackFrame_exception_retrieving_this, new Object[] { e.toString() }), e);
					return null;
				}
			}
			return m_thisObject;
		}
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		if(adapter == IStackFrame.class) {
			return this;
		}
		return super.getAdapter(adapter);
	}

	public String getSignature() throws DebugException {
		try {
			return getUnderlyingMethod().signature();
		} catch(RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDXMessages.JDXStackFrame_exception_retrieving_method_signature, new Object[] { e.toString() }), e);
			return null;
		}
	}

	public String getDeclaringTypeName() throws DebugException {
		synchronized(m_thread) {
			try {
				return JDXReferenceType.getGenericName(getUnderlyingMethod().declaringType());
			} catch(RuntimeException e) {
				if(getThread().isSuspended()) {
					targetRequestFailed(MessageFormat.format(JDXMessages.JDXStackFrame_exception_retrieving_declaring_type, new Object[] { e.toString() }), e);
				}
				return JDXMessages.JDXStackFrame__unknown_declaring_type__1;
			}
		}
	}

	public String getReceivingTypeName() throws DebugException {
		if(m_stackFrame == null || m_receivingTypeName == null) {
			try {
				ObjectReference thisObject = getUnderlyingThisObject();
				if(thisObject == null) {
					m_receivingTypeName = getDeclaringTypeName();
				} else {
					m_receivingTypeName = JDXReferenceType.getGenericName(thisObject.referenceType());
				}
			} catch(RuntimeException e) {
				if(getThread().isSuspended()) {
					targetRequestFailed(MessageFormat.format(JDXMessages.JDXStackFrame_exception_retrieving_receiving_type, new Object[] { e.toString() }), e);
				}
				return JDXMessages.JDXStackFrame__unknown_receiving_type__2;
			}
		}
		return m_receivingTypeName;
	}

	public String getMethodName() throws DebugException {
		try {
			return getUnderlyingMethod().name();
		} catch(RuntimeException e) {
			if(getThread().isSuspended()) {
				targetRequestFailed(MessageFormat.format(JDXMessages.JDXStackFrame_exception_retrieving_method_name, new Object[] { e.toString() }), e);
			}
			return JDXMessages.JDXStackFrame__unknown_method__1;
		}
	}

	public IType getType() throws DebugException {
		return getType(getSourceName());
	}

	public static IType getType(String sourcePath) throws DebugException {
		if(sourcePath != null) {
			String qualifiedName = new Path(sourcePath).removeFileExtension().toString().replace(IPath.SEPARATOR, '.');
			return Workspace.getInstance().lookupType(qualifiedName);
		}

		return null;
	}

	public String getSourceName() throws DebugException {
		synchronized(m_thread) {
			try {
				return m_location.sourcePath();
			} catch(AbsentInformationException e) {
				return null;
			} catch(NativeMethodException e) {
				return null;
			} catch(RuntimeException e) {
				targetRequestFailed(MessageFormat.format(JDXMessages.JDXStackFrame_exception_retrieving_source_name, new Object[] { e.toString() }), e);
			}
		}
		return null;
	}

	protected boolean isTopStackFrame() throws DebugException {
		IStackFrame tos = getThread().getTopStackFrame();
		return tos != null && tos.equals(this);
	}

	protected boolean exists() {
		synchronized(m_thread) {
			return m_depth != -1;
		}
	}

	@Override
	public boolean canTerminate() {
		return exists() && getThread().canTerminate() || getDebugTarget().canTerminate();
	}

	@Override
	public boolean isTerminated() {
		return getThread().isTerminated();
	}

	@Override
	public void terminate() throws DebugException {
		if(getThread().canTerminate()) {
			getThread().terminate();
		} else {
			getDebugTarget().terminate();
		}
	}

	public StackFrame getUnderlyingStackFrame() throws DebugException {
		synchronized(m_thread) {
			if(m_stackFrame == null) {
				if(m_depth == -1) {
					throw new DebugException(new Status(IStatus.ERROR, JDXDebugModel.getModelIdentifier(), ERR_INVALID_STACK_FRAME, JDXMessages.JDXStackFrame_25, null));
				}
				if(m_thread.isSuspended()) {
					// re-index stack frames - See Bug 47198
					m_thread.computeStackFrames();
					if(m_depth == -1) {
						// If depth is -1, then this is an invalid frame
						throw new DebugException(new Status(IStatus.ERROR, JDXDebugModel.getModelIdentifier(), ERR_INVALID_STACK_FRAME, JDXMessages.JDXStackFrame_25, null));
					}
				} else {
					throw new DebugException(new Status(IStatus.ERROR, JDXDebugModel.getModelIdentifier(), JDXThread.ERR_THREAD_NOT_SUSPENDED, JDXMessages.JDXStackFrame_25, null));
				}
			}
			return m_stackFrame;
		}
	}

	protected void setUnderlyingStackFrame(StackFrame frame) {
		synchronized(m_thread) {
			m_stackFrame = frame;
			if(frame == null) {
				m_refreshVariables = true;
			}
		}
	}

	protected void setThread(JDXThread thread) {
		m_thread = thread;
	}

	protected void setVariables(List<IVariable> variables) {
		m_variables = variables;
	}

	public IVariable[] getLocalVariables() throws DebugException {
		List<LocalVariable> list = getUnderlyingVisibleVariables();

		IVariable[] locals = new IVariable[list.size()];

		for(int i = 0; i < list.size(); i++) {
			locals[i] = new JDXLocalVariable(this, getJDXThread(), list.get(i));
		}

		return locals;
	}

	public IVariable getThis() throws DebugException {
		ObjectReference thisObject = getUnderlyingThisObject();
		if(thisObject != null) {
			return (IVariable)JDXValue.createValue(getJDXDebugTarget(), m_thread, null, thisObject);
		}

		return null;
	}

	@Override
	public IRegisterGroup[] getRegisterGroups() {
		return new IRegisterGroup[0];
	}

	public JDXClassType getDeclaringType() throws DebugException {
		Method method = getUnderlyingMethod();
		try {
			Type type = method.declaringType();
			if(type instanceof ClassType) {
				return (JDXClassType)JDXType.createType(getJDXDebugTarget(), m_thread, null, type);
			}
			targetRequestFailed(JDXMessages.JDXStackFrame_0, null);
		} catch(RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDXMessages.JDXStackFrame_exception_retreiving_declaring_type, new Object[] { e.toString() }), e);
		}
		return null;
	}

	public JDXReferenceType getReferenceType() throws DebugException {
		Method method = getUnderlyingMethod();
		try {
			Type type = method.declaringType();
			return (JDXReferenceType)JDXType.createType(getJDXDebugTarget(), m_thread, null, type);
		} catch(RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDXMessages.JDXStackFrame_exception_retreiving_declaring_type, new Object[] { e.toString() }), e);
		}
		return null;
	}

	@Override
	public int getCharEnd() {
		return -1;
	}

	@Override
	public int getCharStart() {
		return -1;
	}

	private void clearCachedData() {
		m_thisObject = null;
		m_receivingTypeName = null;
	}

	public boolean wereLocalsAvailable() {
		return m_localsAvailable;
	}

	private void setLocalsAvailable(boolean available) {
		if(available != m_localsAvailable) {
			m_localsAvailable = available;
			fireChangeEvent(DebugEvent.STATE);
		}
	}

	@Override
	public boolean hasRegisterGroups() {
		return false;
	}

	@Override
	public boolean hasVariables() throws DebugException {
		return getVariables0().size() > 0;
	}

	public String getSourcePath(String stratum) throws DebugException {
		synchronized(m_thread) {
			try {
				return m_location.sourcePath(stratum);
			} catch(AbsentInformationException e) {
			} catch(RuntimeException e) {
				targetRequestFailed(MessageFormat.format(JDXMessages.JDXStackFrame_exception_retrieving_source_path, new Object[] { e.toString() }), e);
			}
		}
		return null;
	}

	public String getSourcePath() throws DebugException {
		synchronized(m_thread) {
			try {
				return m_location.sourcePath();
			} catch(AbsentInformationException e) {
			} catch(RuntimeException e) {
				targetRequestFailed(MessageFormat.format(JDXMessages.JDXStackFrame_exception_retrieving_source_path, new Object[] { e.toString() }), e);
			}
		}
		return null;
	}

	public int getLineNumber(String stratum) throws DebugException {
		synchronized(m_thread) {
			try {
				return m_location.lineNumber(stratum);
			} catch(RuntimeException e) {
				if(getThread().isSuspended()) {
					targetRequestFailed(MessageFormat.format(JDXMessages.JDXStackFrame_exception_retrieving_line_number, new Object[] { e.toString() }), e);
				}
			}
		}
		return -1;
	}

	public String getSourceName(String stratum) throws DebugException {
		synchronized(m_thread) {
			try {
				return m_location.sourceName(stratum);
			} catch(AbsentInformationException e) {
			} catch(NativeMethodException e) {
			} catch(RuntimeException e) {
				targetRequestFailed(MessageFormat.format(JDXMessages.JDXStackFrame_exception_retrieving_source_name, new Object[] { e.toString() }), e);
			}
		}
		return null;
	}

	public boolean isVarArgs() {
		return getUnderlyingMethod().isVarArgs();
	}
}
