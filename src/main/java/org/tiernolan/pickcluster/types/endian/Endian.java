package org.tiernolan.pickcluster.types.endian;

public class Endian {
	
	public final static int LITTLE = 0;
	public final static int BIG = 1;
	
	public static byte swap(byte b) {
		return b;
	}
	
	public static short swap(short s) {
		return (short) (((s & 0xFFFF) >> 8) | (s << 8));
	}
	
	public static int swap(int x) {
		return (x >>> 24) | ((x & 0x00FF0000) >>> 8) | ((x & 0x0000FF00) << 8) | (x << 24);
	}
	
	public static long swap(long x) {
		return (((long) swap((int) x)) << 32) | (swap((int) (x >>> 32)) & 0xFFFFFFFFL);
	}
	
	public static byte[] swap(byte[] buf) {
		return swap(buf, 0, buf.length);
	}
	
	public static byte[] swap(byte[] buf, int off, int len) {
		byte[] newBuf = new byte[len];
		System.arraycopy(buf, 0, newBuf, off, len);
		swapInPlace(newBuf);
		return newBuf;
	}

	public static void swapInPlace(byte[] buf) {
		swapInPlace(buf, 0, buf.length);
	}
	
	public static void swapInPlace(byte[] buf, int off, int len) {
		int mid = buf.length >> 1;
		int end = buf.length - 1;
		for (int i = 0; i < mid; i++) {
			byte temp = buf[i];
			buf[i] = buf[end];
			buf[end] = temp;
			end--;
		}
	}

}
