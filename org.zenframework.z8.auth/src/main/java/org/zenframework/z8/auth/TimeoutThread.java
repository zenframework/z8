package org.zenframework.z8.auth;

class TimeoutThread extends Thread {
	private SessionManager sessionManager;

	TimeoutThread() {
		super("Z8 Session Timeout");
	}

	public void start(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
		start();
	}

	@Override
	public void run() {
		while(!interrupted()) {
			sessionManager.checkTimeout();

			try {
				Thread.sleep(30000);
			} catch(InterruptedException e) {
				break;
			}
		}
	}
}
