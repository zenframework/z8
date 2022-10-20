package org.zenframework.z8.server.base.security;

import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.string;

public class SecurityEvent extends OBJECT {

	public static class CLASS<T extends SecurityEvent> extends OBJECT.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(SecurityEvent.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new SecurityEvent(container);
		}
	}

	public SecurityEvent(IObject container) {
		super(container);
	}

	private SecurityObject.CLASS<? extends SecurityObject> object;
	private string action;
	private bool success = bool.True;
	private string message = new string();

	public void setResult(boolean success, String message) {
		this.success = new bool(success);
		this.message = new string(message);
	}

	public SecurityObject.CLASS<? extends SecurityObject> z8_getObject() {
		return object;
	}

	public String getAction() {
		return action.get();
	}

	public string z8_getAction() {
		return action;
	}

	public boolean isSuccess() {
		return success.get();
	}

	public bool z8_isSuccess() {
		return success;
	}

	public String getMessage() {
		return message.get();
	}

	public string z8_getMessage() {
		return message;
	}

	public static SecurityEvent.CLASS<SecurityEvent> event(SecurityObject.CLASS<? extends SecurityObject> object, string action) {
		SecurityEvent.CLASS<SecurityEvent> event = new SecurityEvent.CLASS<SecurityEvent>(null);
		event.get().object = object;
		event.get().action = action;
		return event;
	}

}
