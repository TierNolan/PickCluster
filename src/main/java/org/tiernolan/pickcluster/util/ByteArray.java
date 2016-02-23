package org.tiernolan.pickcluster.util;

import java.io.DataInputStream;
import java.io.IOException;

public class ByteArray {
	
	public static byte[] rightJustify(byte[] bytes, int length) {
		
		if (bytes.length == length) {
			return bytes;
		}
		
		byte[] temp = new byte[length];
		
		int j = length - 1;
		for (int i = bytes.length - 1; i >= 0 && j >= 0;) {
			temp[j--] = bytes[i--];
		}
		
		return temp;
		
	}
	
	public static byte[] readBuf(DataInputStream in, int len) throws IOException {
		byte[] buf = new byte[len];
		in.readFully(buf);
		return buf;
	}

}
