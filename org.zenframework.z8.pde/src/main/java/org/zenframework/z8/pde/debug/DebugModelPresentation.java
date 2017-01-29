package org.zenframework.z8.pde.debug;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDisconnect;
import org.eclipse.debug.core.model.IIndexedValue;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.debug.core.model.ITerminate;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IValueDetailListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.part.FileEditorInput;

import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.workspace.Workspace;
import org.zenframework.z8.pde.Plugin;
import org.zenframework.z8.pde.debug.breakpoints.JDXBreakpoint;
import org.zenframework.z8.pde.debug.breakpoints.JDXClassPrepareBreakpoint;
import org.zenframework.z8.pde.debug.breakpoints.JDXExceptionBreakpoint;
import org.zenframework.z8.pde.debug.breakpoints.JDXLineBreakpoint;
import org.zenframework.z8.pde.debug.breakpoints.JDXMethodBreakpoint;
import org.zenframework.z8.pde.debug.breakpoints.JDXPatternBreakpoint;
import org.zenframework.z8.pde.debug.breakpoints.JDXStratumLineBreakpoint;
import org.zenframework.z8.pde.debug.breakpoints.JDXTargetPatternBreakpoint;
import org.zenframework.z8.pde.debug.breakpoints.JDXWatchpoint;
import org.zenframework.z8.pde.debug.model.JDXDebugModel;
import org.zenframework.z8.pde.debug.model.JDXDebugOptionsManager;
import org.zenframework.z8.pde.debug.model.JDXDebugTarget;
import org.zenframework.z8.pde.debug.model.JDXStackFrame;
import org.zenframework.z8.pde.debug.model.JDXThread;
import org.zenframework.z8.pde.debug.model.type.JDXReferenceType;
import org.zenframework.z8.pde.debug.model.type.JDXType;
import org.zenframework.z8.pde.debug.model.value.JDXArrayValue;
import org.zenframework.z8.pde.debug.model.value.JDXMapValue;
import org.zenframework.z8.pde.debug.model.value.JDXNullValue;
import org.zenframework.z8.pde.debug.model.value.JDXPrimitiveValue;
import org.zenframework.z8.pde.debug.model.value.JDXStringValue;
import org.zenframework.z8.pde.debug.model.value.JDXValue;
import org.zenframework.z8.pde.debug.model.variable.JDXFieldVariable;
import org.zenframework.z8.pde.debug.model.variable.JDXVariable;
import org.zenframework.z8.pde.debug.util.Signature;

public class DebugModelPresentation extends LabelProvider implements IDebugModelPresentation {
	private HashMap<String, Object> m_attributes = new HashMap<String, Object>();

	protected static final String fgStringName = "com.java.lang.String";

	public final static String SHOW_HEX_VALUES = "SHOW_HEX_VALUES";
	public final static String SHOW_CHAR_VALUES = "SHOW_CHAR_VALUES";
	public final static String SHOW_UNSIGNED_VALUES = "SHOW_UNSIGNED_VALUES";

	private static final String RUN_TO_LINE = JDXDebugModel.getModelIdentifier() + ".run_to_line"; //$NON-NLS-1$

	@Override
	public void setAttribute(String attribute, Object value) {
		m_attributes.put(attribute, value);
	}

	public Object getAttribute(String attribute) {
		return m_attributes.get(attribute);
	}

	public boolean showVariableTypeNames() {
		Object showTypeNames = getAttribute(IDebugModelPresentation.DISPLAY_VARIABLE_TYPE_NAMES);
		return showTypeNames == null ? false : showTypeNames.equals(Boolean.TRUE);
	}

	@Override
	public Image getImage(Object element) {
		return null;
	}

	@Override
	public void computeDetail(IValue value, IValueDetailListener listener) {
		String detail = "";
		try {
			detail = value.getValueString();
		} catch(DebugException e) {
		}
		listener.detailComputed(value, detail);
	}

	@Override
	public String getText(Object item) {
		try {
			if(item instanceof JDXVariable) {
				return getVariableText((JDXVariable)item);
			} else if(item instanceof JDXStackFrame) {
				return getStackFrameText((JDXStackFrame)item);
			} else if(item instanceof IMarker) {
				IBreakpoint breakpoint = getBreakpoint((IMarker)item);
				if(breakpoint != null) {
					return getBreakpointText(breakpoint);
				}
				return null;
			} else if(item instanceof IBreakpoint) {
				return getBreakpointText((IBreakpoint)item);
			} else {
				StringBuffer label = new StringBuffer();
				if(item instanceof JDXThread) {
					label.append(getThreadText((JDXThread)item));
				} else if(item instanceof JDXDebugTarget) {
					label.append(getDebugTargetText((JDXDebugTarget)item));
				} else if(item instanceof JDXValue) {
					label.append(getValueText((JDXValue)item));
				}
				if(item instanceof ITerminate) {
					if(((ITerminate)item).isTerminated()) {
						label.insert(0, JDXMessages.JDXModelPresentation__terminated__2);
						return label.toString();
					}
				}
				if(item instanceof IDisconnect) {
					if(((IDisconnect)item).isDisconnected()) {
						label.insert(0, JDXMessages.JDXModelPresentation__disconnected__4);
						return label.toString();
					}
				}
				if(label.length() > 0) {
					return label.toString();
				}
			}
		} catch(CoreException e) {
			return JDXMessages.JDXModelPresentation__not_responding__6;
		}
		return null;
	}

	protected String getVariableText(JDXVariable var) {
		String varLabel = JDXMessages.JDXModelPresentation_unknown_name__1;
		try {
			varLabel = var.getName();
		} catch(DebugException exception) {
		}

		JDXValue jdxValue = null;

		try {
			jdxValue = (JDXValue)var.getValue();
		} catch(DebugException e1) {
		}

		int spaceIndex = varLabel.lastIndexOf(' ');
		StringBuffer buff = new StringBuffer();
		String typeName = JDXMessages.JDXModelPresentation_unknown_type__2;

		try {
			typeName = var.getReferenceTypeName();
			if(spaceIndex == -1) {
				typeName = getTypeName(typeName);
			}
		} catch(DebugException exception) {
		}

		if(showVariableTypeNames()) {
			buff.append(typeName);
			buff.append(' ');
		}

		if(spaceIndex != -1) {
			varLabel = varLabel.substring(spaceIndex + 1);
		}
		buff.append(varLabel);

		if(var instanceof JDXFieldVariable) {
			JDXFieldVariable field = (JDXFieldVariable)var;
			if(isDuplicateName(field)) {
				try {
					String decl = field.getDeclaringType().getName();
					buff.append(MessageFormat.format(" ({0})", new Object[] { getTypeName(decl) }));
				} catch(DebugException e) {
				}
			}
		}

		String valueString = JDXMessages.JDXModelPresentation_unknown_value__3;

		if(jdxValue != null) {
			try {
				valueString = getValueText(jdxValue);
			} catch(DebugException exception) {
			}
		}

		if(valueString.length() != 0) {
			buff.append("= ");
			buff.append(valueString);
		}

		return buff.toString();
	}

	protected boolean isDuplicateName(JDXFieldVariable variable) {
		JDXReferenceType javaType = variable.getReceivingType();
		try {
			String[] names = javaType.getAllFieldNames();
			boolean found = false;
			for(int i = 0; i < names.length; i++) {
				if(variable.getName().equals(names[i])) {
					if(found) {
						return true;
					}
					found = true;
				}
			}
			return false;
		} catch(DebugException e) {
		}
		return false;
	}

	protected String getStackFrameText(JDXStackFrame stackFrame) throws DebugException {
		JDXStackFrame frame = (JDXStackFrame)stackFrame.getAdapter(JDXStackFrame.class);

		if(frame != null) {
			StringBuffer label = new StringBuffer();
			String dec = JDXMessages.JDXModelPresentation_unknown_declaring_type__4;

			try {
				dec = frame.getDeclaringTypeName();
			} catch(DebugException exception) {
			}

			boolean javaStratum = true;

			try {
				javaStratum = frame.getReferenceType().getDefaultStratum().equals("Java");
			} catch(DebugException e) {
			}

			if(javaStratum) {
				String rec = JDXMessages.JDXModelPresentation_unknown_receiving_type__5;

				try {
					rec = frame.getReceivingTypeName();
				} catch(DebugException exception) {
				}

				label.append(getTypeName(rec));

				if(!dec.equals(rec)) {
					label.append('(');
					label.append(getTypeName(dec));
					label.append(')');
				}

				label.append('.');

				try {
					label.append(frame.getMethodName());
				} catch(DebugException exception) {
					label.append(JDXMessages.JDXModelPresentation_unknown_method_name__6);
				}
				try {
					List<String> args = frame.getArgumentTypeNames();

					if(args.isEmpty()) {
						label.append("()");
					} else {
						label.append('(');
						Iterator<String> iter = args.iterator();

						while(iter.hasNext()) {
							label.append(getTypeName(iter.next()));
							if(iter.hasNext()) {
								label.append(", ");
							} else if(frame.isVarArgs()) {
								label.replace(label.length() - 2, label.length(), "...");
							}
						}
						label.append(')');
					}
				} catch(DebugException exception) {
					label.append(JDXMessages.JDXModelPresentation__unknown_arguements___7);
				}
			} else {
				label.append(frame.getSourceName());
			}
			try {
				int lineNumber = frame.getLineNumber();
				label.append(' ');
				label.append(JDXMessages.JDXModelPresentation_line__76);
				label.append(' ');
				if(lineNumber >= 0) {
					label.append(lineNumber);
				} else {
					label.append(JDXMessages.JDXModelPresentation_not_available);
				}
			} catch(DebugException exception) {
				label.append(JDXMessages.JDXModelPresentation__unknown_line_number__8);
			}
			if(!frame.wereLocalsAvailable()) {
				label.append(' ');
				label.append(JDXMessages.JDXModelPresentation_local_variables_unavailable);
			}
			return label.toString();
		}
		return null;
	}

	protected IBreakpoint getBreakpoint(IMarker marker) {
		return DebugPlugin.getDefault().getBreakpointManager().getBreakpoint(marker);
	}

	protected String getBreakpointText(IBreakpoint breakpoint) {
		try {
			if(breakpoint instanceof JDXExceptionBreakpoint) {
				return getExceptionBreakpointText((JDXExceptionBreakpoint)breakpoint);
			} else if(breakpoint instanceof JDXWatchpoint) {
				return getWatchpointText((JDXWatchpoint)breakpoint);
			} else if(breakpoint instanceof JDXMethodBreakpoint) {
				return getMethodBreakpointText((JDXMethodBreakpoint)breakpoint);
			} else if(breakpoint instanceof JDXPatternBreakpoint) {
				return getJDXPatternBreakpointText((JDXPatternBreakpoint)breakpoint);
			} else if(breakpoint instanceof JDXTargetPatternBreakpoint) {
				return getJDXTargetPatternBreakpointText((JDXTargetPatternBreakpoint)breakpoint);
			} else if(breakpoint instanceof JDXStratumLineBreakpoint) {
				return getJDXStratumLineBreakpointText((JDXStratumLineBreakpoint)breakpoint);
			} else if(breakpoint instanceof JDXLineBreakpoint) {
				return getLineBreakpointText((JDXLineBreakpoint)breakpoint);
			} else if(breakpoint instanceof JDXClassPrepareBreakpoint) {
				return getClassPrepareBreakpointText((JDXClassPrepareBreakpoint)breakpoint);
			}
			return "";
		} catch(CoreException e) {
			IMarker marker = breakpoint.getMarker();

			if(marker == null || !marker.exists()) {
				return JDXMessages.JDXModelPresentation_6;
			}

			Plugin.log(e);

			return JDXMessages.JDXModelPresentation_4;
		}
	}

	protected String getTypeName(String qualifiedTypeName) {
		String qualifiedNestedTypeName = null;

		int index = qualifiedTypeName.indexOf("$");

		if(index != -1) {
			qualifiedNestedTypeName = qualifiedTypeName.substring(index + 1);
			qualifiedNestedTypeName = qualifiedNestedTypeName.replace('$', '.');
			qualifiedTypeName = qualifiedTypeName.substring(0, index);
		}

		IType classType = Workspace.getInstance().lookupType(qualifiedTypeName, qualifiedNestedTypeName);

		if(classType != null) {
			qualifiedTypeName = classType.getNestedUserName();
		}

		return qualifiedTypeName;
	}

	private String getJDXStratumLineBreakpointText(JDXStratumLineBreakpoint breakpoint) throws CoreException {
		String sourceName = breakpoint.getSourceName();

		if(sourceName == null) {
			sourceName = "";
			IMarker marker = breakpoint.getMarker();

			if(marker != null) {
				IResource resource = marker.getResource();
				if(resource.getType() == IResource.FILE) {
					sourceName = resource.getName();
				}
			}
		}

		StringBuffer label = new StringBuffer(sourceName);
		appendLineNumber(breakpoint, label);
		appendHitCount(breakpoint, label);
		appendSuspendPolicy(breakpoint, label);
		appendThreadFilter(breakpoint, label);

		return label.toString();
	}

	protected String getExceptionBreakpointText(JDXExceptionBreakpoint breakpoint) throws CoreException {
		StringBuffer buffer = new StringBuffer();
		String typeName = breakpoint.getTypeName();
		buffer.append(getTypeName(typeName));
		appendHitCount(breakpoint, buffer);
		appendSuspendPolicy(breakpoint, buffer);
		appendThreadFilter(breakpoint, buffer);
		if(breakpoint.getExclusionFilters().length > 0 || breakpoint.getInclusionFilters().length > 0) {
			buffer.append(JDXMessages.JDXModelPresentation___scoped__1);
		}
		appendInstanceFilter(breakpoint, buffer);
		String state = null;
		boolean c = breakpoint.isCaught();
		boolean u = breakpoint.isUncaught();
		if(c && u) {
			state = JDXMessages.JDXModelPresentation_caught_and_uncaught_60;
		} else if(c) {
			state = JDXMessages.JDXModelPresentation_caught_61;
		} else if(u) {
			state = JDXMessages.JDXModelPresentation_uncaught_62;
		}
		String label = null;
		if(state == null) {
			label = buffer.toString();
		} else {
			String format = JDXMessages.JDXModelPresentation__1____0__63;
			label = MessageFormat.format(format, new Object[] { state, buffer });
		}
		return label;
	}

	protected String getLineBreakpointText(JDXLineBreakpoint breakpoint) throws CoreException {
		String typeName = breakpoint.getTypeName();
		StringBuffer label = new StringBuffer();
		label.append(getTypeName(typeName));
		appendLineNumber(breakpoint, label);
		appendHitCount(breakpoint, label);
		appendSuspendPolicy(breakpoint, label);
		appendThreadFilter(breakpoint, label);
		appendInstanceFilter(breakpoint, label);
		return label.toString();
	}

	protected String getClassPrepareBreakpointText(JDXClassPrepareBreakpoint breakpoint) throws CoreException {
		String typeName = breakpoint.getTypeName();
		StringBuffer label = new StringBuffer();
		label.append(getTypeName(typeName));
		appendHitCount(breakpoint, label);
		appendSuspendPolicy(breakpoint, label);
		return label.toString();
	}

	protected StringBuffer appendLineNumber(JDXLineBreakpoint breakpoint, StringBuffer label) throws CoreException {
		int lineNumber = breakpoint.getLineNumber();
		if(lineNumber > 0) {
			label.append(" [");
			label.append(JDXMessages.JDXModelPresentation_line__65);
			label.append(' ');
			label.append(lineNumber);
			label.append(']');
		}
		return label;
	}

	protected StringBuffer appendHitCount(JDXBreakpoint breakpoint, StringBuffer label) throws CoreException {
		int hitCount = breakpoint.getHitCount();
		if(hitCount > 0) {
			label.append(" [");
			label.append(JDXMessages.JDXModelPresentation_hit_count__67);
			label.append(' ');
			label.append(hitCount);
			label.append(']');
		}
		return label;
	}

	protected void appendSuspendPolicy(JDXBreakpoint breakpoint, StringBuffer buffer) throws CoreException {
		if(breakpoint.getSuspendPolicy() == JDXBreakpoint.SUSPEND_VM) {
			buffer.append(' ');
			buffer.append(JDXMessages.JDXModelPresentation_Suspend_VM);
		}
	}

	protected void appendThreadFilter(JDXBreakpoint breakpoint, StringBuffer buffer) {
		if(breakpoint.getThreadFilters().length != 0) {
			buffer.append(' ');
			buffer.append(JDXMessages.JDXModelPresentation_thread_filtered);
		}
	}

	protected void appendInstanceFilter(JDXBreakpoint breakpoint, StringBuffer buffer) throws CoreException {
		JDXValue[] instances = breakpoint.getInstanceFilters();
		for(int i = 0; i < instances.length; i++) {
			String instanceText = instances[i].getValueString();
			if(instanceText != null) {
				buffer.append(' ');
				buffer.append(MessageFormat.format(JDXMessages.JDXModelPresentation_instance_1, new Object[] { instanceText }));
			}
		}
	}

	protected String getJDXPatternBreakpointText(JDXPatternBreakpoint breakpoint) throws CoreException {
		IResource resource = breakpoint.getMarker().getResource();
		StringBuffer label = new StringBuffer(resource.getName());
		appendLineNumber(breakpoint, label);
		appendHitCount(breakpoint, label);
		appendSuspendPolicy(breakpoint, label);
		appendThreadFilter(breakpoint, label);
		return label.toString();
	}

	protected String getJDXTargetPatternBreakpointText(JDXTargetPatternBreakpoint breakpoint) throws CoreException {
		StringBuffer label = new StringBuffer(breakpoint.getSourceName());
		appendLineNumber(breakpoint, label);
		appendHitCount(breakpoint, label);
		appendSuspendPolicy(breakpoint, label);
		appendThreadFilter(breakpoint, label);
		return label.toString();
	}

	protected String getWatchpointText(JDXWatchpoint watchpoint) throws CoreException {
		String typeName = watchpoint.getTypeName();
		StringBuffer label = new StringBuffer();
		label.append(getTypeName(typeName));
		appendHitCount(watchpoint, label);
		appendSuspendPolicy(watchpoint, label);
		appendThreadFilter(watchpoint, label);
		boolean access = watchpoint.isAccess();
		boolean modification = watchpoint.isModification();
		if(access && modification) {
			label.append(JDXMessages.JDXModelPresentation_access_and_modification_70);
		} else if(access) {
			label.append(JDXMessages.JDXModelPresentation_access_71);
		} else if(modification) {
			label.append(JDXMessages.JDXModelPresentation_modification_72);
		}
		label.append(" - ");
		label.append(watchpoint.getFieldName());
		return label.toString();
	}

	protected String getMethodBreakpointText(JDXMethodBreakpoint methodBreakpoint) throws CoreException {
		String typeName = methodBreakpoint.getTypeName();
		StringBuffer label = new StringBuffer();
		label.append(getTypeName(typeName));
		appendHitCount(methodBreakpoint, label);
		appendSuspendPolicy(methodBreakpoint, label);
		appendThreadFilter(methodBreakpoint, label);
		boolean entry = methodBreakpoint.isEntry();
		boolean exit = methodBreakpoint.isExit();
		if(entry && exit) {
			label.append(JDXMessages.JDXModelPresentation_entry_and_exit);
		} else if(entry) {
			label.append(JDXMessages.JDXModelPresentation_entry);
		} else if(exit) {
			label.append(JDXMessages.JDXModelPresentation_exit);
		}

		String methodSig = methodBreakpoint.getMethodSignature();
		String methodName = methodBreakpoint.getMethodName();
		if(methodSig != null) {
			label.append(" - ");
			label.append(Signature.toString(methodSig, methodName, null, false, false));
		} else if(methodName != null) {
			label.append(" - ");
			label.append(methodName);
		}
		return label.toString();
	}

	public static String getFormattedString(String key, String arg) {
		return getFormattedString(key, new String[] { arg });
	}

	public static String getFormattedString(String string, String[] args) {
		return MessageFormat.format(string, (Object[])args);
	}

	protected String getThreadText(JDXThread thread) throws CoreException {
		if(thread.isTerminated()) {
			if(thread.isSystemThread()) {
				return getFormattedString(JDXMessages.JDXModelPresentation_System_Thread____0____Terminated__7, thread.getName());
			}
			return getFormattedString(JDXMessages.JDXModelPresentation_Thread____0____Terminated__8, thread.getName());
		}

		if(thread.isStepping()) {
			if(thread.isSystemThread()) {
				return getFormattedString(JDXMessages.JDXModelPresentation_System_Thread___0____Stepping__9, thread.getName());
			}
			return getFormattedString(JDXMessages.JDXModelPresentation_Thread___0____Stepping__10, thread.getName());
		}

		if(!thread.isSuspended() || (thread.isSuspendedQuiet())) {
			if(thread.isSystemThread()) {
				return getFormattedString(JDXMessages.JDXModelPresentation_System_Thread___0____Running__11, thread.getName());
			}
			return getFormattedString(JDXMessages.JDXModelPresentation_Thread___0____Running__12, thread.getName());
		}

		IBreakpoint[] breakpoints = thread.getBreakpoints();

		if(breakpoints.length > 0) {
			JDXBreakpoint breakpoint = (JDXBreakpoint)breakpoints[0];
			for(int i = 0, numBreakpoints = breakpoints.length; i < numBreakpoints; i++) {
				if(isProblemBreakpoint(breakpoints[i])) {
					breakpoint = (JDXBreakpoint)breakpoints[i];
					break;
				}
			}

			String typeName = getMarkerTypeName(breakpoint);

			if(breakpoint instanceof JDXExceptionBreakpoint) {
				String exName = ((JDXExceptionBreakpoint)breakpoint).getExceptionTypeName();
				if(exName == null) {
					exName = typeName;
				}

				int index = exName.lastIndexOf('.');
				exName = exName.substring(index + 1);

				if(thread.isSystemThread()) {
					return getFormattedString(JDXMessages.JDXModelPresentation_System_Thread___0____Suspended__exception__1____13, new String[] { thread.getName(), exName });
				}
				return getFormattedString(JDXMessages.JDXModelPresentation_Thread___0____Suspended__exception__1____14, new String[] { thread.getName(), exName });
			}

			if(breakpoint instanceof JDXWatchpoint) {
				JDXWatchpoint wp = (JDXWatchpoint)breakpoint;
				String fieldName = wp.getFieldName();
				if(wp.isAccessSuspend(thread.getDebugTarget())) {
					if(thread.isSystemThread()) {
						return getFormattedString(JDXMessages.JDXModelPresentation_System_Thread___0____Suspended__access_of_field__1__in__2____16, new String[] { thread.getName(), fieldName, typeName });
					}
					return getFormattedString(JDXMessages.JDXModelPresentation_Thread___0____Suspended__access_of_field__1__in__2____17, new String[] { thread.getName(), fieldName, typeName });
				}
				// modification
				if(thread.isSystemThread()) {
					return getFormattedString(JDXMessages.JDXModelPresentation_System_Thread___0____Suspended__modification_of_field__1__in__2____18, new String[] { thread.getName(), fieldName, typeName });
				}
				return getFormattedString(JDXMessages.JDXModelPresentation_Thread___0____Suspended__modification_of_field__1__in__2____19, new String[] { thread.getName(), fieldName, typeName });
			}

			if(breakpoint instanceof JDXMethodBreakpoint) {
				JDXMethodBreakpoint me = (JDXMethodBreakpoint)breakpoint;
				String methodName = me.getMethodName();
				if(me.isEntrySuspend(thread.getDebugTarget())) {
					if(thread.isSystemThread()) {
						return getFormattedString(JDXMessages.JDXModelPresentation_System_Thread___0____Suspended__entry_into_method__1__in__2____21, new String[] { thread.getName(), methodName, typeName });
					}
					return getFormattedString(JDXMessages.JDXModelPresentation_Thread___0____Suspended__entry_into_method__1__in__2____22, new String[] { thread.getName(), methodName, typeName });
				}
				if(thread.isSystemThread()) {
					return getFormattedString(JDXMessages.JDXModelPresentation_System_Thread___0____Suspended__exit_of_method__1__in__2____21, new String[] { thread.getName(), methodName, typeName });
				}
				return getFormattedString(JDXMessages.JDXModelPresentation_Thread___0____Suspended__exit_of_method__1__in__2____22, new String[] { thread.getName(), methodName, typeName });
			}
			if(breakpoint instanceof JDXLineBreakpoint) {
				JDXLineBreakpoint jlbp = (JDXLineBreakpoint)breakpoint;
				int lineNumber = jlbp.getLineNumber();
				if(lineNumber > -1) {
					if(thread.isSystemThread()) {
						if(isRunToLineBreakpoint(jlbp)) {
							return getFormattedString(JDXMessages.JDXModelPresentation_System_Thread___0____Suspended__run_to_line__1__in__2____23, new String[] { thread.getName(), String.valueOf(lineNumber), typeName });
						}
						return getFormattedString(JDXMessages.JDXModelPresentation_System_Thread___0____Suspended__breakpoint_at_line__1__in__2____24, new String[] { thread.getName(), String.valueOf(lineNumber), typeName });
					}
					if(isRunToLineBreakpoint(jlbp)) {
						return getFormattedString(JDXMessages.JDXModelPresentation_Thread___0____Suspended__run_to_line__1__in__2____25, new String[] { thread.getName(), String.valueOf(lineNumber), typeName });
					}
					return getFormattedString(JDXMessages.JDXModelPresentation_Thread___0____Suspended__breakpoint_at_line__1__in__2____26, new String[] { thread.getName(), String.valueOf(lineNumber), typeName });
				}
			}
			if(breakpoint instanceof JDXClassPrepareBreakpoint) {
				return getFormattedString(JDXMessages.JDXModelPresentation_115, new String[] { thread.getName(), getTypeName(breakpoint.getTypeName()) });
			}
		}
		if(thread.isSystemThread()) {
			return getFormattedString(JDXMessages.JDXModelPresentation_System_Thread___0____Suspended__27, thread.getName());
		}
		return getFormattedString(JDXMessages.JDXModelPresentation_Thread___0____Suspended__28, thread.getName());
	}

	public static boolean isRunToLineBreakpoint(JDXLineBreakpoint breakpoint) {
		return breakpoint.getMarker().getAttribute(RUN_TO_LINE, false);
	}

	public static boolean isProblemBreakpoint(IBreakpoint breakpoint) {
		return breakpoint == JDXDebugOptionsManager.getDefault().getSuspendOnUncaughtExceptionBreakpoint();
	}

	protected String getMarkerTypeName(JDXBreakpoint breakpoint) throws CoreException {
		String typeName = null;
		if(breakpoint instanceof JDXPatternBreakpoint) {
			typeName = breakpoint.getMarker().getResource().getName();
		} else {
			typeName = breakpoint.getTypeName();
		}

		int index = typeName.lastIndexOf('.');
		if(index != -1) {
			typeName = typeName.substring(index + 1);
		}
		return typeName;
	}

	protected String getDebugTargetText(JDXDebugTarget debugTarget) throws DebugException {
		String labelString = debugTarget.getName();
		if(debugTarget.isSuspended()) {
			labelString += JDXMessages.JDXModelPresentation_target_suspended;
		}
		return labelString;
	}

	protected String getValueText(JDXValue value) throws DebugException {
		String refTypeName = value.getReferenceTypeName();
		String valueString = value.getValueString();

		boolean isObject = !(value instanceof JDXPrimitiveValue) && !(value instanceof JDXNullValue);
		boolean isString = value instanceof JDXStringValue;

		JDXType type = value.getJDXType();

		String signature = null;

		if(type != null) {
			signature = type.getSignature();
		}
		if("V".equals(signature)) {
			valueString = JDXMessages.JDXModelPresentation__No_explicit_return_value__30;
		}

		boolean isArray = value instanceof JDXArrayValue || value instanceof JDXMapValue;

		StringBuffer buffer = new StringBuffer();

		if(isString || valueString != null && valueString.length() > 0) {
			if(isString) {
				buffer.append('"');
			}
			buffer.append(valueString);
			if(isString) {
				buffer.append('"');
			}
		}

		if(isObject && (refTypeName.length() > 0)) {
			String qualTypeName = getTypeName(refTypeName);

			if(isArray) {
				qualTypeName = adjustTypeNameForArrayIndex(qualTypeName, ((IIndexedValue)value).getSize());
			}

			buffer.append(' ');

			if(valueString != null && valueString.length() > 0) {
				buffer.append('(');
			}

			buffer.append(qualTypeName);

			if(valueString != null && valueString.length() > 0) {
				buffer.append(')');
			}
		}

		/*
		 * if(isShowUnsignedValues()) { buffer = appendUnsignedText(value,
		 * buffer); }
		 * 
		 * if(isShowHexValues()) { buffer = appendHexText(value, buffer); }
		 * 
		 * if(isShowCharValues()) { buffer = appendCharText(value, buffer); }
		 */
		return buffer.toString();
	}

	protected boolean isShowHexValues() {
		Boolean show = (Boolean)m_attributes.get(SHOW_HEX_VALUES);
		show = show == null ? Boolean.FALSE : show;
		return show.booleanValue();
	}

	protected boolean isShowCharValues() {
		Boolean show = (Boolean)m_attributes.get(SHOW_CHAR_VALUES);
		show = show == null ? Boolean.FALSE : show;
		return show.booleanValue();
	}

	protected boolean isShowUnsignedValues() {
		Boolean show = (Boolean)m_attributes.get(SHOW_UNSIGNED_VALUES);
		show = show == null ? Boolean.FALSE : show;
		return show.booleanValue();
	}

	protected StringBuffer appendHexText(JDXValue value, StringBuffer buffer) throws DebugException {
		String hexText = getValueHexText(value);
		if(hexText != null) {
			buffer.append(" [");
			buffer.append(hexText);
			buffer.append("]");
		}
		return buffer;
	}

	protected StringBuffer appendCharText(JDXValue value, StringBuffer buffer) throws DebugException {
		String charText = getValueCharText(value);
		if(charText != null) {
			buffer.append(" [");
			buffer.append(charText);
			buffer.append("]");
		}
		return buffer;
	}

	protected String getValueCharText(JDXValue value) throws DebugException {
		String sig = getPrimitiveValueTypeSignature(value);
		if(sig == null) {
			return null;
		}
		String valueString = value.getValueString();
		long longValue;
		try {
			longValue = Long.parseLong(valueString);
		} catch(NumberFormatException e) {
			return null;
		}
		switch(sig.charAt(0)) {
		case 'B': // byte
			longValue = longValue & 0xFF; // Only lower 8 bits
			break;
		case 'I': // int
			longValue = longValue & 0xFFFFFFFF; // Only lower 32 bits
			if(longValue > 0xFFFF || longValue < 0) {
				return null;
			}
			break;
		case 'S': // short
			longValue = longValue & 0xFFFF; // Only lower 16 bits
			break;
		case 'J':
			if(longValue > 0xFFFF || longValue < 0) {
				// Out of character range
				return null;
			}
			break;
		default:
			return null;
		}
		char charValue = (char)longValue;
		StringBuffer charText = new StringBuffer();
		if(Character.getType(charValue) == Character.CONTROL) {
			Character ctrl = new Character((char)(charValue + 64));
			charText.append('^'); // $NON-NLS-1$
			charText.append(ctrl);
			switch(charValue) { // common use
			case 0:
				charText.append(" (NUL)"); //$NON-NLS-1$
				break;
			case 8:
				charText.append(" (BS)"); //$NON-NLS-1$
				break;
			case 9:
				charText.append(" (TAB)"); //$NON-NLS-1$
				break;
			case 10:
				charText.append(" (LF)"); //$NON-NLS-1$
				break;
			case 13:
				charText.append(" (CR)"); //$NON-NLS-1$
				break;
			case 21:
				charText.append(" (NL)"); //$NON-NLS-1$
				break;
			case 27:
				charText.append(" (ESC)"); //$NON-NLS-1$
				break;
			case 127:
				charText.append(" (DEL)"); //$NON-NLS-1$
				break;
			}
		} else {
			charText.append(new Character(charValue));
		}
		return charText.toString();
	}

	protected String getValueUnsignedText(JDXValue value) throws DebugException {
		String sig = getPrimitiveValueTypeSignature(value);
		if(sig == null) {
			return null;
		}
		switch(sig.charAt(0)) {
		case 'B': // byte
			int byteVal;
			try {
				byteVal = Integer.parseInt(value.getValueString());
			} catch(NumberFormatException e) {
				return null;
			}
			if(byteVal < 0) {
				byteVal = byteVal & 0xFF;
				return Integer.toString(byteVal);
			}
		default:
			return null;
		}
	}

	protected String getValueHexText(JDXValue value) throws DebugException {
		String sig = getPrimitiveValueTypeSignature(value);
		if(sig == null) {
			return null;
		}
		StringBuffer buff = new StringBuffer();
		long longValue;
		char sigValue = sig.charAt(0);
		try {
			if(sigValue == 'C') {
				longValue = value.getValueString().charAt(0);
			} else {
				longValue = Long.parseLong(value.getValueString());
			}
		} catch(NumberFormatException e) {
			return null;
		}
		switch(sigValue) {
		case 'B':
			buff.append("0x");
			// keep only the relevant bits for byte
			longValue &= 0xFF;
			buff.append(Long.toHexString(longValue));
			break;
		case 'I':
			buff.append("0x");
			// keep only the relevant bits for int
			longValue &= 0xFFFFFFFFl;
			buff.append(Long.toHexString(longValue));
			break;
		case 'S':
			buff.append("0x");
			// keep only the relevant bits for short
			longValue = longValue & 0xFFFF;
			buff.append(Long.toHexString(longValue));
			break;
		case 'J':
			buff.append("0x");
			buff.append(Long.toHexString(longValue));
			break;
		case 'C':
			buff.append("\\u");
			String hexString = Long.toHexString(longValue);
			int length = hexString.length();
			while(length < 4) {
				buff.append('0');
				length++;
			}
			buff.append(hexString);
			break;
		default:
			return null;
		}
		return buff.toString();
	}

	protected String getPrimitiveValueTypeSignature(JDXValue value) throws DebugException {
		JDXType type = value.getJDXType();
		if(type != null) {
			String sig = type.getSignature();
			if(sig != null && sig.length() == 1) {
				return sig;
			}
		}
		return null;
	}

	protected String adjustTypeNameForArrayIndex(String typeName, int arrayIndex) {
		int lastBracket = typeName.lastIndexOf("[");

		if(lastBracket != -1) {
			return typeName.substring(0, lastBracket) + "[" + arrayIndex + "]";
		}

		return typeName;
	}

	@Override
	public IEditorInput getEditorInput(Object element) {
		if(element instanceof IFile) {
			return new FileEditorInput((IFile)element);
		}
		if(element instanceof ILineBreakpoint) {
			return new FileEditorInput((IFile)((ILineBreakpoint)element).getMarker().getResource());
		}
		return null;
	}

	@Override
	public String getEditorId(IEditorInput input, Object element) {
		if(element instanceof IFile || element instanceof ILineBreakpoint) {
			return "org.zenframework.z8.pde.Z8Editor";
		}
		return null;
	}
}
