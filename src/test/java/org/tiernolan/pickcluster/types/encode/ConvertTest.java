package org.tiernolan.pickcluster.types.encode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.tiernolan.pickcluster.types.UInt96;

public class ConvertTest {

	@Test
	public void hexCharToIntTest() {
		Object[][] tests = new Object[][] {
				{'a', 10},
				{'b', 11},
				{'f', 15},
				{'A', 10},
				{'C', 12},
				{'F', 15},
				{'0', 0},
				{'5', 5},
				{'9', 9},
				{'x', -1},
				{'?', -1}
		};
		
		for (Object[] test : tests) {
			char c = (Character) test[0];
			int exp = (Integer) test[1];
			int converted = Convert.hexCharToInt(c);
			assertEquals("Character (" + c + ") was not converted correctly", exp, converted);
		}
	}
	
	@Test
	public void intToHexCharTest() {
		Object[][] tests = new Object[][] {
				{'a', 'A', 10},
				{'b', 'B', 11},
				{'f', 'F', 15},
				{'0', '0', 0},
				{'5', '5', 5},
				{'9', '9', 9}
		};
		
		for (Object[] test : tests) {
			char lowerExp = (Character) test[0];
			char upperExp = (Character) test[1];
			int i = (Integer) test[2];
			char converted = Convert.intToHexChar(i);
			char convertedLower = Convert.intToHexChar(i, false);
			char convertedUpper = Convert.intToHexChar(i, true);
			assertEquals("Integer (" + i + ") was not converted correctly", lowerExp, converted);
			assertEquals("Integer (" + i + ") was not converted correctly to uppercase", upperExp, convertedUpper);
			assertEquals("Integer (" + i + ") was not converted correctly to lowercase", lowerExp, convertedLower);
		}
	}
	
	@Test
	public void hexToBytesTest() {
		List<String> data = new ArrayList<String>();
		List<byte[]> exp = new ArrayList<byte[]>();
		
		data.add("");
		exp.add(new byte[] {});

		data.add("000102030405060708090A0B0C0D0E0F10");
		exp.add(new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16});
		
		data.add("00102030405060708090A0B0C0D0E0F0");
		exp.add(new byte[] {0, 16, 32, 48, 64, 80, 96, 112, -128, -112, -96, -80, -64, -48, -32, -16});

		data.add("01112131415161718191A1B1C1D1E1F1");
		exp.add(new byte[] {1, 17, 33, 49, 65, 81, 97, 113, -127, -111, -95, -79, -63, -47, -31, -15});
		
		data.add("00112233445566778899AABBCCDDEEFF");
		exp.add(new byte[] {0, 17, 34, 51, 68, 85, 102, 119, -120, -103, -86, -69, -52, -35, -18, -1});
		
		for (int i = 0; i < data.size(); i++) {
			String hex = data.get(i);
			byte[] e = exp.get(i);
			byte[] converted = Convert.hexToBytes(hex);
			assertTrue("Hex string " + hex + " was not converted correctly", Arrays.equals(e, converted));
		}
	}
	
	@Test
	public void bytesToHexTest() {
		List<String> exp = new ArrayList<String>();
		List<byte[]> data = new ArrayList<byte[]>();
		
		exp.add("");
		data.add(new byte[] {});

		exp.add("000102030405060708090A0B0C0D0E0F10");
		data.add(new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16});
		
		exp.add("00102030405060708090A0B0C0D0E0F0");
		data.add(new byte[] {0, 16, 32, 48, 64, 80, 96, 112, -128, -112, -96, -80, -64, -48, -32, -16});

		exp.add("01112131415161718191A1B1C1D1E1F1");
		data.add(new byte[] {1, 17, 33, 49, 65, 81, 97, 113, -127, -111, -95, -79, -63, -47, -31, -15});
		
		exp.add("00112233445566778899AABBCCDDEEFF");
		data.add(new byte[] {0, 17, 34, 51, 68, 85, 102, 119, -120, -103, -86, -69, -52, -35, -18, -1});
		
		for (int i = 0; i < data.size(); i++) {
			String hex = exp.get(i);
			String hexUpper = hex.toUpperCase();
			String hexLower = hex.toLowerCase();
			
			byte[] bytes = data.get(i);

			String converted = Convert.bytesToHex(bytes);
			String convertedLower = Convert.bytesToHex(bytes, false);
			String convertedUpper = Convert.bytesToHex(bytes, true);

			assertEquals("Hex string " + Arrays.toString(bytes) + " was not converted correctly", hexLower, converted);
			assertEquals("Hex string " + Arrays.toString(bytes) + " was not converted to upper-case correctly", hexUpper, convertedUpper);
			assertEquals("Hex string " + Arrays.toString(bytes) + " was not converted to lower-case correctly", hexLower, convertedLower);
		}
	}
	
	@Test
	public void UInt96ToCommandStringTest() {
		String[][] tests = new String[][] {
				{"",             "000000000000000000000000"},
				{"n",            "6E0000000000000000000000"},
				{"no",           "6E6F00000000000000000000"},
				{"nop",          "6E6F70000000000000000000"},
				{"version",      "76657273696f6e0000000000"},
				{"abcdefABCDEF", "616263646566414243444546"},
				{"uvwxyzUVWXYZ", "75767778797A55565758595A"}
		};
		
		for (String[] test : tests) {
			String commandString = test[0];
			UInt96 command = new UInt96(Convert.hexToBytes(test[1]));
			assertEquals(commandString, Convert.UInt96ToCommandString(command));
			assertEquals(command, Convert.commandStringToUInt96(commandString));
		}
	}
}