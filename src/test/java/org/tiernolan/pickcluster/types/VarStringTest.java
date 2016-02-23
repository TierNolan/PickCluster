package org.tiernolan.pickcluster.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.junit.Test;
import org.tiernolan.pickcluster.types.encode.Convert;
import org.tiernolan.pickcluster.types.endian.EndianDataInputStream;
import org.tiernolan.pickcluster.types.endian.EndianDataOutputStream;

public class VarStringTest {
	
	@Test
	public void testEncodeDecode() throws IOException {
		String[][] tests = new String[][] {
				{"", "00"},
				{"a", "0161"},
				{"ab", "026162"},
				{repeat('a', 0xFC), "FC" + repeat("61", 0xFC)},
				{repeat('a', 0xFD), "FDFD00" + repeat("61", 0xFD)},
				{repeat('a', 0xFE), "FDFE00" + repeat("61", 0xFE)},
				{repeat('a', 0xFF), "FDFF00" + repeat("61", 0xFF)},
				{repeat('a', 0x100), "FD0001" + repeat("61", 0x100)},
				{repeat('a', 0x101), "FD0101" + repeat("61", 0x101)},
				{repeat('a', 0xFFFF), "FDFFFF" + repeat("61", 0xFFFF)},
				{repeat('a', 0x10000), "FE00000100" + repeat("61", 0x10000)},
				{repeat('a', 0x12345), "FE45230100" + repeat("61", 0x12345)}
		};
		
		for (String[] test : tests) {
			test(test[0], test[1]);
		}
	}
	
	private String repeat(char c, int count) {
		char[] chars = new char[count];
		Arrays.fill(chars, c);
		return new String(chars);
	}
	
	private String repeat(String s, int count) {
		StringBuilder sb = new StringBuilder(s.length() * count);
		for (int i = 0; i < count; i++) {
			sb.append(s);
		}
		return sb.toString();
	}
	
	private void test(String value, String hexString) throws IOException {
		byte[] expected = Convert.hexToBytes(hexString);
		VarString varString = new VarString(value);
		assertEquals(value, varString.getValue());
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		EndianDataOutputStream eos = new EndianDataOutputStream(baos);
		varString.write(0, eos);
		eos.flush();
		
		byte[] encoded = baos.toByteArray();
		assertTrue("Mismatch when encoding " + value, Arrays.equals(expected, encoded));
		
		ByteArrayInputStream bais = new ByteArrayInputStream(expected);
		EndianDataInputStream eis = new EndianDataInputStream(bais);
		VarString varString2 = new VarString(eis, 0x10000000);
		assertEquals("Mismatch when decoding " + value, value, varString2.getValue());
	}

}
