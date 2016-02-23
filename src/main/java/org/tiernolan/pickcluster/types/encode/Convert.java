package org.tiernolan.pickcluster.types.encode;

import org.tiernolan.pickcluster.types.UInt96;

public class Convert {
	
	private final static int[] hexCharToInt;
	private final static char[] hexIntToCharLower;
	private final static char[] hexIntToCharUpper;
	
	static {
		hexCharToInt = new int[256];
		for (int i = 0; i < 256; i++) {
			hexCharToInt[i] = -1;
		}
		
		int i = 0;
		for (char c = '0'; c <= '9'; c++) {
			hexCharToInt[c & 0xFF] = i++;
		}
		
		i = 10;
		for (char c = 'A'; c <= 'F'; c++) {
			hexCharToInt[c & 0xFF] = i++;
		}
		
		i = 10;
		for (char c = 'a'; c <= 'f'; c++) {
			hexCharToInt[c & 0xFF] = i++;
		}
		
		hexIntToCharLower = new char[16];
		hexIntToCharUpper = new char[16];
		
		for (i = 0; i < 10; i++) {
			hexIntToCharLower[i] = (char) ('0' + i);
			hexIntToCharUpper[i] = (char) ('0' + i);
		}
		
		for (i = 10; i < 16; i++) {
			hexIntToCharLower[i] = (char) ('a' + i - 10);
			hexIntToCharUpper[i] = (char) ('A' + i - 10);
		}
	}
	
	public static int hexCharToInt(char c) {
		if ((c & 0xFF00) != 0) {
			return -1;
		}
		return hexCharToInt[c & 0xFF];
	}
	
	public static char intToHexChar(int x) {
		return intToHexChar(x, false);
	}
	public static char intToHexChar(int x, boolean upper) {
		return (upper ? hexIntToCharUpper : hexIntToCharLower)[x & 0xF];
	}
	
	public static byte[] hexToBytes(String hex) {
		if (!isEven(hex.length())) {
			throw new IllegalArgumentException("Encode Error: hex string length odd");
		}
		byte[] buf = new byte[hex.length() / 2];
		for (int i = 0; i < hex.length(); i += 2) {
			char a = hex.charAt(i);
			char b = hex.charAt(i + 1);
			int msn = hexCharToInt(a);
			int lsn = hexCharToInt(b);
			if (msn == -1 || lsn == -1) {
				throw new IllegalArgumentException("Encode Error: hex string contains non-hex character");
			}
			buf[i >> 1] = (byte) ((msn << 4) | lsn);
		}
		return buf;
	}
	
	public static String bytesToHex(byte[] buf) {
		return bytesToHex(buf, false);
	}
	
	public static String bytesToHex(byte[] buf, boolean upper) {
		char[] chars = new char[buf.length << 1];
		int b = 0;
		for (int i = 0; i < chars.length; i += 2) {
			chars[i] = intToHexChar(buf[b] >> 4, upper);
			chars[i + 1] = intToHexChar(buf[b], upper);
			b++;
		}
		return new String(chars);
	}
	
	private static boolean isEven(int x) {
		return (x & 1) == 0;
	}
	
	public static String UInt96ToCommandString(UInt96 command) {
		char[] chars = new char[12];
		int nonZero = 0;
		byte[] buf = command.getBytes();
		for (int i = 0; i < 12; i++) {
			if (buf[i] != 0) {
				nonZero = i + 1;
			}
			chars[i] = (char) (buf[i] & 0xFF);
		}
		return new String(chars, 0, nonZero);
	}
	
	public static UInt96 commandStringToUInt96(String command) {
		byte[] buf = new byte[12];
		for (int i = 0; i < command.length(); i++) {
			int c = command.charAt(i);
			if ((c & 0xFF00) != 0) {
				throw new IllegalArgumentException("Command string contains non-ASCII characters");
			}
			buf[i] = (byte) (c & 0xFF);
		}
		return new UInt96(buf);
	}

}
