package org.tiernolan.pickcluster.util;

import static org.junit.Assert.assertTrue;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Arrays;
import java.util.Random;

import org.junit.Test;
import org.tiernolan.pickcluster.types.encode.Convert;

public class ByteArrayTest {
	
	private final static int TEST_LENGTH = 1000;
	
	@Test
	public void testRightJustify() {
		
		Object[][] data = new Object[][] {
				{Convert.hexToBytes(""), 4, Convert.hexToBytes("00000000")},
				{Convert.hexToBytes("123456"), 4, Convert.hexToBytes("00123456")},
				{Convert.hexToBytes("12345678"), 4, Convert.hexToBytes("12345678")},
				{Convert.hexToBytes("12345678"), 6, Convert.hexToBytes("000012345678")},
				{Convert.hexToBytes("12345678"), 3, Convert.hexToBytes("345678")},
				{Convert.hexToBytes("123456789A"), 4, Convert.hexToBytes("3456789A")},
				{Convert.hexToBytes("12345678"), 0, Convert.hexToBytes("")},
		};
		
		for (Object[] d : data) {
			byte[] input = (byte[]) d[0];
			int len = (Integer) d[1];
			byte[] exp = (byte[]) d[2];
			assertTrue("Unexpected result when testing " + Convert.bytesToHex(input), Arrays.equals(exp, ByteArray.rightJustify(input, len)));
			rightJustifyTest(input, len);
		}
		
		Random r = new Random();
		
		for (int i = 0; i < TEST_LENGTH; i++) {
			byte[] buf = new byte[r.nextInt(12)];
			r.nextBytes(buf);
			int len = r.nextInt(12);
			rightJustifyTest(buf, len);
		}
		
	}

	private void rightJustifyTest(byte[] input, int len) {
		byte[] exp = new byte[len];
		int j = input.length - 1;
		for (int i = exp.length - 1; j >= 0 && i >= 0; i--) {
			exp[i] = input[j];
			j--;
		}
		assertTrue(Arrays.equals(exp, ByteArray.rightJustify(input, len)));
	}
	
	@Test
	public void testReadBuf() throws IOException {
		
		PipedOutputStream pos = new PipedOutputStream();
		PipedInputStream pis = new PipedInputStream(pos);
		
		byte[][] bufs = new byte[50][];
		
		Random r = new Random();
		
		for (int i = 0; i < 50; i++) {
			bufs[i] = new byte[r.nextInt(20)];
			r.nextBytes(bufs[i]);
			pos.write(bufs[i]);
		}
		
		DataInputStream dis = new DataInputStream(pis);

		for (byte[] exp : bufs) {
			byte[] buf = ByteArray.readBuf(dis, exp.length);
			assertTrue(Arrays.equals(exp, buf));
		}
		
	}
}
