package org.zenframework.z8.pde.debug.model;

import com.sun.jdi.event.Event;

public interface JDXEventListener {
	public boolean handleEvent(Event event, JDXDebugTarget target);

	public void wonSuspendVote(Event event, JDXDebugTarget target);
}
