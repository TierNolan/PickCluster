package org.tiernolan.pickcluster.util;

public class ThreadUtils {
	
	public static void joinUninterruptibly(Thread t) {
		boolean interrupted = false;
		while (t.isAlive()) {
			try {
				t.join();
			} catch (InterruptedException e) {
				interrupted = true;
			}
		}
		if (interrupted) {
			Thread.currentThread().interrupt();
		}
	}

}
