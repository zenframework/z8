package org.zenframework.z8.server.engine;

public class TimeoutChecker extends Thread {
	
	private RmiServer server;
	
	public TimeoutChecker(RmiServer server, String name) {
		super(name);
		
		this.server = server;
	}
	
	@Override
	public void run() {
		while(true) {
			try {
				server.check();
	
				if(Thread.interrupted())
					return;
			
				Thread.sleep(30000);
			} catch(InterruptedException e) {
				return;
			}
		}
	}

	public void destroy() {
		interrupt();
	}
}
