package org.tiernolan.pickcluster.types;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;

import org.junit.Test;
import org.tiernolan.pickcluster.types.encode.Convert;
import org.tiernolan.pickcluster.types.endian.Endian;
import org.tiernolan.pickcluster.types.endian.EndianDataInputStream;
import org.tiernolan.pickcluster.types.endian.EndianDataOutputStream;

public class NetTypeArrayTest {

	private final static boolean FULL_TEST = false;
	
	@Test
	public void testEncodeDecode() throws IOException {
		Object[][] tests = new Object[][] {
				{repeat(0, 0), "00"},
				{repeat(1, 1), "0101000000000000000000000000000000"},
				{repeat(0x0123456789ABCDEFL, 2), "02EFCDAB89674523010000000000000000F0CDAB89674523010000000000000000"},
				{repeat(0x0123456789ABCDEFL, 0xFC), "FC" + repeatString(0x0123456789ABCDEFL, 0xFC)},
				{repeat(0x0123456789ABCDEFL, 0xFD), "FDFD00" + repeatString(0x0123456789ABCDEFL, 0xFD)},
				{repeat(0x0123456789ABCDEFL, 0xFE), "FDFE00" + repeatString(0x0123456789ABCDEFL, 0xFE)},
				{repeat(0x0123456789ABCDEFL, 0xFF), "FDFF00" + repeatString(0x0123456789ABCDEFL, 0xFF)},
				{repeat(0x0123456789ABCDEFL, 0x100), "FD0001" + repeatString(0x0123456789ABCDEFL, 0x100)},
				{repeat(0x0123456789ABCDEFL, 0x101), "FD0101" + repeatString(0x0123456789ABCDEFL, 0x101)},
				// Only run when FULL_TEST is set to true
				{repeat(0x0123456789ABCDEFL, 0xFFFF), "FDFFFF" + repeatString(0x0123456789ABCDEFL, 0xFFFF)},
				{repeat(0x0123456789ABCDEFL, 0x10000), "FE00000100" + repeatString(0x0123456789ABCDEFL, 0x10000)},
				{repeat(0x0123456789ABCDEFL, 0x12345), "FE45230100" + repeatString(0x0123456789ABCDEFL, 0x12345)}
			};

		for (Object[] test : tests) {
			test((UInt128[]) test[0], (String) test[1]);
		}
	}
	
	private UInt128[] repeat(long start, int count) {
		if (count >= 0xFFFF && (!FULL_TEST)) {
			return null;
		}
		UInt128[] array = new UInt128[count];
		for (int i = 0; i < count; i++) {
			array[i] = new UInt128(BigInteger.valueOf(i + start));
		}
		return array;
	}
	
	private String repeatString(long start, int count) {
		if (count >= 0xFFFF && (!FULL_TEST)) {
			return null;
		}
		StringBuilder sb = new StringBuilder(32 * count);
		for (int i = 0; i < count; i++) {
			long value = start + i;
			value = Endian.swap(value);
			sb.append(String.format("%016x%016x", value, 0L));
		}
		return sb.toString();
	}
	
	private void test(UInt128[] value, String hexString) throws IOException {
		if (value == null) {
			return;
		}
		byte[] expected = Convert.hexToBytes(hexString);
		NetTypeArray<UInt128> netTypeArray = new NetTypeArray<UInt128>(value, UInt128.class);
		assertTrue(Arrays.equals(value, netTypeArray.getValue()));
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		EndianDataOutputStream eos = new EndianDataOutputStream(baos);
		netTypeArray.write(0, eos);
		eos.flush();
		
		byte[] encoded = baos.toByteArray();
		assertTrue("Mismatch when encoding " + Arrays.toString(value) + ", exp " + Convert.bytesToHex(expected) + ", got " + Convert.bytesToHex(encoded), Arrays.equals(expected, encoded));
		
		ByteArrayInputStream bais = new ByteArrayInputStream(expected);
		EndianDataInputStream eis = new EndianDataInputStream(bais);
		NetTypeArray<UInt128> netTypeArray2 = new NetTypeArray<UInt128>(0, eis, 0x20000, UInt128.class, new UInt128(BigInteger.ZERO), Endian.LITTLE);
		assertTrue("Mismatch when decoding " + Arrays.toString(value), Arrays.equals(value, netTypeArray2.getValue()));
	}

}
