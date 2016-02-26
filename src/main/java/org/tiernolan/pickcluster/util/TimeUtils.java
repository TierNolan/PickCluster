package org.tiernolan.pickcluster.util;

public class TimeUtils {
	
	private static long forcedTime = -1;
	
	public static void forceTime(long time) {
		forcedTime = time;
	}
	
	public static long getCurrentTimeMillis() {
		if (forcedTime == -1) {
			return System.currentTimeMillis();
		} else {
			return forcedTime;
		}
	}

}
