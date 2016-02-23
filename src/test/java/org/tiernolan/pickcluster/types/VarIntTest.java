package org.tiernolan.pickcluster.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;

import org.junit.Test;
import org.tiernolan.pickcluster.types.encode.Convert;
import org.tiernolan.pickcluster.types.endian.Endian;
import org.tiernolan.pickcluster.types.endian.EndianDataInputStream;
import org.tiernolan.pickcluster.types.endian.EndianDataOutputStream;

public class VarIntTest {
	
	private final int TEST_LENGTH = 1000;
	
	@Test
	public void testEncodeDecode() throws IOException {

		Object[][] tests = new Object[][] {
				{0x00000000L, "00"},
				{0x00000001L, "01"},
				{0x00000056L, "56"},
				{0x000000FCL, "FC"},
				{0x000000FDL, "FDFD00"},
				{0x000000FEL, "FDFE00"},
				{0x000000FFL, "FDFF00"},
				{0x00000100L, "FD0001"},
				{0x00000101L, "FD0101"},
				{0x0000475AL, "FD5A47"},
				{0x0000FFFFL, "FDFFFF"},
				{0x00010000L, "FE00000100"},
				{0x00010001L, "FE01000100"},
				{0x75785724L, "FE24577875"},
				{0xFFFFFFFFL, "FEFFFFFFFF"},
				{0x0000000100000000L, "FF0000000001000000"},
				{0x0000000100000001L, "FF0100000001000000"},
				{0x0123456789ABCDEFL, "FFEFCDAB8967452301"},
				{0x7FFFFFFFFFFFFFFFL, "FFFFFFFFFFFFFFFF7F"}				
		};
		
		for (Object[] test : tests) {
			test((Long) test[0], (String) test[1]);
		}
		
		Random r = new Random();
		
		for (int i = 0; i < TEST_LENGTH; i++) {
			int type = r.nextInt(4);
			
			if (type == 0) {
				long v = r.nextInt(0xFD);
				test(v, String.format("%02x", v));
			} else if (type == 1) {
				long v = r.nextInt() & 0xFFFF;
				if (v < 0xFD) {
					continue;
				}
				test(v, String.format("FD%04x", Endian.swap((short) v)));
			} else if (type == 2) {
				long v = r.nextInt() & 0xFFFFFFFFL;
				if (v <= 0xFFFF) {
					continue;
				}
				test(v, String.format("FE%08x", Endian.swap((int) v)));
			} else if (type == 3) {
				long v = r.nextLong() & 0x7FFFFFFFFFFFFFFFL;
				if (v <= 0xFFFFFFFFL) {
					continue;
				}
				test(v, String.format("FF%016x", Endian.swap(v)));
			}
		}
	}
	
	private void test(long value, String hexString) throws IOException {
		byte[] expected = Convert.hexToBytes(hexString);
		VarInt varInt = new VarInt(value);
		assertEquals(value, varInt.getValue());
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		EndianDataOutputStream eos = new EndianDataOutputStream(baos);
		varInt.write(0, eos);
		eos.flush();
		
		byte[] encoded = baos.toByteArray();
		assertTrue("Mismatch when encoding " + value, Arrays.equals(expected, encoded));
		
		ByteArrayInputStream bais = new ByteArrayInputStream(expected);
		EndianDataInputStream eis = new EndianDataInputStream(bais);
		VarInt varInt2 = new VarInt(eis);
		assertEquals("Mismatch when decoding " + value, value, varInt2.getValue());
	}

}
