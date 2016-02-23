package org.tiernolan.pickcluster.types.endian;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Random;

import org.junit.Test;

public class EndianDataInputStreamTest {
	
	private final static int TEST_LENGTH = 1000;
	
	@Test
	public void test() throws IOException {
		
		PipedInputStream pis = new PipedInputStream();
		PipedOutputStream pos = new PipedOutputStream(pis);
		
		EndianDataInputStream eis = new EndianDataInputStream(pis);
		DataOutputStream dos = new DataOutputStream(pos);
		
		Random r = new Random();
		
		for (int i = 0; i < TEST_LENGTH; i++) {
			int type = r.nextInt(4);
			boolean bigEndian = r.nextBoolean();
			
			if (type == 0) {
				byte val = (byte) r.nextInt();
				dos.writeByte(val);
				if (bigEndian) {
					assertEquals("Readback error for byte " + val, val, eis.readBEByte());
				} else {
					assertEquals("Readback error for byte " + val, val, eis.readLEByte());
				}
			} else if (type == 1) {
				short val = (short) r.nextInt();
				dos.writeShort(bigEndian ? val : Endian.swap(val));
				if (bigEndian) {
					assertEquals("Readback error for short " + val, val, eis.readBEShort());
				} else {
					assertEquals("Readback error for short " + val, val, eis.readLEShort());
				}
			} else if (type == 2) {
				int val = r.nextInt();
				dos.writeInt(bigEndian ? val : Endian.swap(val));
				if (bigEndian) {
					assertEquals("Readback error for int " + val, val, eis.readBEInt());
				} else {
					assertEquals("Readback error for int " + val, val, eis.readLEInt());
				}
			} else if (type == 3) {
				long val = r.nextLong();
				dos.writeLong(bigEndian ? val : Endian.swap(val));
				if (bigEndian) {
					assertEquals("Readback error for long " + val, val, eis.readBELong());
				} else {
					assertEquals("Readback error for long " + val, val, eis.readLELong());
				}
			} else {
				assertTrue("Invalid type value " + type, false);
			}
		}
		
	}

}
