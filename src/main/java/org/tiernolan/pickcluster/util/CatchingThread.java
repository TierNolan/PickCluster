package org.tiernolan.pickcluster.util;

public abstract class CatchingThread extends Thread {

	protected CatchingThread() {
	}
	
	protected CatchingThread(String name) {
		super(name);
	}
	
	public final void run() {
		try {
			this.secondaryRun();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	protected abstract void secondaryRun() throws Throwable;
	
}
