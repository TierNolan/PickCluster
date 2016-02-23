package org.tiernolan.pickcluster.types.endian;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Random;

import org.junit.Test;

public class EndianDataOutputStreamTest {
	
	private final static int TEST_LENGTH = 1000;
	
	@Test
	public void test() throws IOException {
		
		PipedInputStream pis = new PipedInputStream();
		PipedOutputStream pos = new PipedOutputStream(pis);
		
		DataInputStream dis = new DataInputStream(pis);
		EndianDataOutputStream eos = new EndianDataOutputStream(pos);
		
		Random r = new Random();
		
		for (int i = 0; i < TEST_LENGTH; i++) {
			int type = r.nextInt(4);
			boolean bigEndian = r.nextBoolean();
			
			if (type == 0) {
				byte val = (byte) r.nextInt();
				if (bigEndian) {
					eos.writeBEByte(val);
				} else {
					eos.writeLEByte(val);
				}
				byte readback = dis.readByte();
				assertEquals("Readback error for byte " + val, val, bigEndian ? readback : Endian.swap(readback));
			} else if (type == 1) {
				short val = (short) r.nextInt();
				if (bigEndian) {
					eos.writeBEShort(val);
				} else {
					eos.writeLEShort(val);
				}
				short readback = dis.readShort();
				assertEquals("Readback error for short " + val, val, bigEndian ? readback : Endian.swap(readback));
			} else if (type == 2) {
				int val = r.nextInt();
				if (bigEndian) {
					eos.writeBEInt(val);
				} else {
					eos.writeLEInt(val);
				}
				int readback = dis.readInt();
				assertEquals("Readback error for int " + val, val, bigEndian ? readback : Endian.swap(readback));
			} else if (type == 3) {
				long val = r.nextLong();
				if (bigEndian) {
					eos.writeBELong(val);
				} else {
					eos.writeLELong(val);
				}
				long readback = dis.readLong();
				assertEquals("Readback error for long " + val, val, bigEndian ? readback : Endian.swap(readback));
			} else {
				assertTrue("Invalid type value " + type, false);
			}
		}
		
	}

}
