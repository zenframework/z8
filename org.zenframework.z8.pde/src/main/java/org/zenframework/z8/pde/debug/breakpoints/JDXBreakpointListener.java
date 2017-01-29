package org.zenframework.z8.pde.debug.breakpoints;

import org.zenframework.z8.pde.debug.model.JDXDebugTarget;
import org.zenframework.z8.pde.debug.model.JDXThread;
import org.zenframework.z8.pde.debug.model.type.JDXType;

public interface JDXBreakpointListener {
	public static int SUSPEND = 0x0001;
	public static int DONT_SUSPEND = 0x0002;
	public static int INSTALL = 0x0001;
	public static int DONT_INSTALL = 0x0002;
	public static int DONT_CARE = 0x0004;

	public void addingBreakpoint(JDXDebugTarget target, JDXBreakpoint breakpoint);

	public int installingBreakpoint(JDXDebugTarget target, JDXBreakpoint breakpoint, JDXType type);

	public void breakpointInstalled(JDXDebugTarget target, JDXBreakpoint breakpoint);

	public int breakpointHit(JDXThread thread, JDXBreakpoint breakpoint);

	public void breakpointRemoved(JDXDebugTarget target, JDXBreakpoint breakpoint);
}
