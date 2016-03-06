package org.tiernolan.pickcluster.util;

public class TimeUtils {
	
	public final static long MINUTE_IN_SECONDS = 60;
	public final static long HOUR_IN_SECONDS = MINUTE_IN_SECONDS * 60;
	public final static long DAY_IN_SECONDS = HOUR_IN_SECONDS * 24;
	public final static long WEEK_IN_SECONDS = DAY_IN_SECONDS * 7;
	
	private static long forcedTime = -1;
	
	public static void forceTimeSeconds(long time) {
		forceTimeMillis(time * 1000L);
	}
	
	public static void forceTimeMillis(long time) {
		forcedTime = time;
	}
	
	public static long getNowTimestamp() {
		return getCurrentTimeMillis() / 1000;
	}
	
	public static long getCurrentTimeMillis() {
		if (forcedTime == -1) {
			return System.currentTimeMillis();
		} else {
			return forcedTime;
		}
	}

}
