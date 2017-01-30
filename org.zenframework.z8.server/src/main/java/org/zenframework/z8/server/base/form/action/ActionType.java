package org.zenframework.z8.server.base.form.action;

public enum ActionType {
	Default(names.Default),
	Danger(names.Danger),
	Primary(names.Primary),
	Success(names.Success);

	private class names {
		static protected final String Default = "default";
		static protected final String Primary = "primary";
		static protected final String Success = "success";
		static protected final String Danger = "danger";
	}

	private String fName = null;

	ActionType(String name) {
		fName = name;
	}

	@Override
	public String toString() {
		return fName;
	}

	static public ActionType fromString(String string) {
		if(names.Default.equals(string))
			return ActionType.Default;
		else if(names.Primary.equals(string))
			return ActionType.Primary;
		else if(names.Success.equals(string))
			return ActionType.Success;
		else if(names.Danger.equals(string))
			return ActionType.Danger;
		else
			throw new RuntimeException("Unknown action type: '" + string + "'");
	}
}
