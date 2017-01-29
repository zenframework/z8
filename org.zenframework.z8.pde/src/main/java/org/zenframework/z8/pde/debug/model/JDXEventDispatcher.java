package org.zenframework.z8.pde.debug.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;

import org.zenframework.z8.pde.Plugin;
import org.zenframework.z8.pde.debug.JDXMessages;

import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventIterator;
import com.sun.jdi.event.EventQueue;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.VMDeathEvent;
import com.sun.jdi.event.VMDisconnectEvent;
import com.sun.jdi.event.VMStartEvent;
import com.sun.jdi.request.EventRequest;

public class JDXEventDispatcher implements Runnable {
	private JDXDebugTarget m_target;

	private boolean m_shutdown;
	private HashMap<EventRequest, JDXEventListener> m_eventHandlers;
	private List<DebugEvent> m_debugEvents = new ArrayList<DebugEvent>(5);

	public JDXEventDispatcher(JDXDebugTarget target) {
		m_eventHandlers = new HashMap<EventRequest, JDXEventListener>(10);
		m_target = target;
		m_shutdown = false;
	}

	protected void dispatch(EventSet eventSet) {
		if(isShutdown()) {
			return;
		}
		EventIterator iter = eventSet.eventIterator();
		boolean vote = false;
		boolean resume = true;
		int voters = 0;
		Event winningEvent = null;
		JDXEventListener winner = null;
		while(iter.hasNext()) {
			if(isShutdown()) {
				return;
			}
			Event event = iter.nextEvent();
			if(event == null) {
				continue;
			}
			JDXEventListener listener = m_eventHandlers.get(event.request());

			if(listener != null) {
				vote = true;
				resume = listener.handleEvent(event, m_target) && resume;
				voters++;
				if(!resume && winner == null) {
					winner = listener;
					winningEvent = event;
				}
				continue;
			}
			// Dispatch VM start/end events
			if(event instanceof VMDeathEvent) {
				m_target.handleVMDeath((VMDeathEvent)event);
				shutdown(); // stop listening for events
			} else if(event instanceof VMDisconnectEvent) {
				m_target.handleVMDisconnect((VMDisconnectEvent)event);
				shutdown(); // stop listening for events
			} else if(event instanceof VMStartEvent) {
				m_target.handleVMStart((VMStartEvent)event);
			} else {
				// Unhandled Event
			}
		}

		if(winner != null && voters > 1) {
			winner.wonSuspendVote(winningEvent, m_target);
		}
		fireEvents();
		if(vote && resume) {
			try {
				eventSet.resume();
			} catch(VMDisconnectedException e) {
			} catch(RuntimeException e) {
				try {
					m_target.targetRequestFailed(JDXMessages.EventDispatcher_0, e);
				} catch(DebugException de) {
					Plugin.log(de);
				}
			}
		}
	}

	@Override
	public void run() {
		VirtualMachine vm = m_target.getVM();
		if(vm != null) {
			EventQueue q = vm.eventQueue();
			EventSet eventSet = null;
			while(!isShutdown()) {
				try {
					try {
						eventSet = q.remove(1000);
					} catch(VMDisconnectedException e) {
						break;
					}
					if(!isShutdown() && eventSet != null) {
						dispatch(eventSet);
					}
				} catch(InterruptedException e) {
					break;
				}
			}
		}
	}

	public void shutdown() {
		m_shutdown = true;
	}

	protected boolean isShutdown() {
		return m_shutdown;
	}

	public void addEventListener(JDXEventListener listener, EventRequest request) {
		m_eventHandlers.put(request, listener);
	}

	public void removeEventListener(JDXEventListener listener, EventRequest request) {
		m_eventHandlers.remove(request);
	}

	public void queue(DebugEvent event) {
		m_debugEvents.add(event);
	}

	protected void fireEvents() {
		DebugPlugin plugin = DebugPlugin.getDefault();
		if(plugin != null) {
			DebugEvent[] events = m_debugEvents.toArray(new DebugEvent[m_debugEvents.size()]);
			m_debugEvents.clear();
			plugin.fireDebugEventSet(events);
		}
	}
}
