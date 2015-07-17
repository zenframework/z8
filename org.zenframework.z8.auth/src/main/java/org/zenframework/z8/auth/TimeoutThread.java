package org.zenframework.z8.auth;

class TimeoutThread extends Thread
{
	public static String threadTimeoutGroupName = "Z8 Timeout Group";

	private SessionManager sessionManager;

	private static class TimeoutThreadGroup extends ThreadGroup
	{
		private TimeoutThreadGroup() {
			super(threadTimeoutGroupName);
		}
	}

	private static ThreadGroup threadGroup;
	static {
		threadGroup = new TimeoutThreadGroup();
	}

	TimeoutThread() {
		super(threadGroup, "Z8 Check Sessions Timeout");
	}

	public void start(SessionManager sessions) {
		sessionManager = sessions;
		start();
	}

	@Override
    public void run() {
		while (!interrupted()) {
			sessionManager.checkTimeout();

			try {
				Thread.sleep(30000);
			} catch (InterruptedException e) {
				break;
			}
		}
	}
}
