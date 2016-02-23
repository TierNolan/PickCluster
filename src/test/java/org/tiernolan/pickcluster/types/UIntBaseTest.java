package org.tiernolan.pickcluster.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

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

public class UIntBaseTest {
	
	private final int TEST_LENGTH = 1000;
	
	@Test
	public void testBigIntegerConstructor() throws IOException {

		long[] data = new long[] {
				0x00000000L,
				0x00000001L,
				0x7FFFFFFFL,
				0x80000000L,
				0x80000001L,
				0xFFFFFFFEL,
				0xFFFFFFFFL
		};
		
		for (long l : data) {
			testFromBigInteger(BigInteger.valueOf(l));
		}
		
		Random r = new Random();
		
		for (int i = 0; i < TEST_LENGTH; i++) {
			BigInteger value = new BigInteger(32, r);
			testFromBigInteger(value);
		}
	}
	
	private void testFromBigInteger(BigInteger value) throws IOException {
		UIntBase uInt = new UIntBase(value, 4);
		
		byte[] exp = value.toByteArray();
		if (exp[0] == 0) {
			byte[] newExp = new byte[exp.length - 1];
			System.arraycopy(exp, 1, newExp, 0, newExp.length);
			exp = newExp;
		}

		assertTrue("Expected byte array length is greater than 4 for " + value, exp.length <= 4);
		if (exp.length < 4) {
			byte[] newExp = new byte[4];
			System.arraycopy(exp, 0, newExp, 4 - exp.length, exp.length);
			exp = newExp;
		}
		Endian.swapInPlace(exp);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		EndianDataOutputStream eos = new EndianDataOutputStream(baos);
		uInt.write(eos);
		eos.flush();
		byte[] bytes = baos.toByteArray();
		
		assertTrue("Byte array mismatch", Arrays.equals(exp, bytes));
		assertEquals("BigInteger mismatch", value, uInt.toBigInteger());
	}
	
	@Test
	public void testInputStreamConstructor() throws IOException {
		byte[][] data = new byte[][] {
				Convert.hexToBytes("00000000"),
				Convert.hexToBytes("00000001"),
				Convert.hexToBytes("7FFFFFFF"),
				Convert.hexToBytes("80000000"),
				Convert.hexToBytes("80000001"),
				Convert.hexToBytes("FFFFFFFE"),
				Convert.hexToBytes("FFFFFFFF")
		};
		
		for (byte[] bytes : data) {
			for (int endian = 0; endian < 2; endian++) {
				testFromByteArray(bytes, endian);
			}
		}
		
		Random r = new Random();
		
		byte[] buf = new byte[4];
		
		for (int i = 0; i < TEST_LENGTH; i++) {
			r.nextBytes(buf);
			int endian = r.nextBoolean() ? Endian.LITTLE : Endian.BIG;
			testFromByteArray(buf, endian);
		}
	}
	
	private void testFromByteArray(byte[] buf, int endian) throws IOException {
		byte[] bigEndianBuf = endian == Endian.LITTLE ? Endian.swap(buf) : buf;
		byte[] littleEndianBuf = endian == Endian.BIG ? Endian.swap(buf) : buf;
		
		EndianDataInputStream eis = new EndianDataInputStream(new ByteArrayInputStream(buf));
		UIntBase uInt = new UIntBase(eis, 4, endian);
		
		byte[] buf2 = new byte[buf.length + 1];
		System.arraycopy(bigEndianBuf, 0, buf2, 1, buf.length);
		
		BigInteger exp = new BigInteger(buf2);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		EndianDataOutputStream eos = new EndianDataOutputStream(baos);
		uInt.write(eos);
		eos.flush();
		byte[] bytes = baos.toByteArray();
		
		assertTrue("Byte array mismatch", Arrays.equals(littleEndianBuf, bytes));
		assertEquals("BigInteger mismatch", exp, uInt.toBigInteger());
	}
	
	@Test
	public void comparisonTest() {
		
		long[] data = new long[] {
				0x00000000L,
				0x00000001L,
				0x7FFFFFFFL,
				0x80000000L,
				0x80000001L,
				0xFFFFFFFEL,
				0xFFFFFFFFL
		};
		
		for (long lA : data) {
			for (long lB : data) {
				BigInteger a = BigInteger.valueOf(lA);
				BigInteger b = BigInteger.valueOf(lB);
				testComparisons(a, b, 4, 4);
				testComparisons(a, a, 4, 4);
				testComparisons(b, b, 4, 4);
				testComparisons(a, b, 4, 6);
				testComparisons(a, a, 4, 6);
				testComparisons(b, b, 4, 6);
			}
		}
		
		Random r = new Random();
		
		for (int i = 0; i < TEST_LENGTH; i++) {
			BigInteger a = new BigInteger(32, r);
			BigInteger b = new BigInteger(32, r);
			testComparisons(a, b, 4, 4);
			testComparisons(a, a, 4, 4);
		}
		
		for (int i = 0; i < TEST_LENGTH; i++) {
			BigInteger a = new BigInteger(32, r);
			BigInteger b = new BigInteger(48, r);
			testComparisons(a, b, 4, 6);
			testComparisons(a, a, 4, 6);
		}
		
		for (int i = 0; i < TEST_LENGTH; i++) {
			BigInteger a = new BigInteger(32, r);
			BigInteger b = new BigInteger(32, r);
			testComparisons(a, b, 4, 6);
			testComparisons(a, a, 4, 6);
			testComparisons(b, b, 4, 6);
		}
		
		UIntBase[] array = new UIntBase[100];
		for (int i = 0; i < array.length; i++) {
			BigInteger a = new BigInteger(24, r);
			a = a.shiftLeft(8).add(BigInteger.valueOf(i));
			array[i] = new UIntBase(a, 4);
		}
		
		Arrays.sort(array);
		
		for (int i = 0; i < 100; i++) {
			for (int j = 0; j < i; j++) {
				assertTrue(array[j].compareTo(array[i]) < 0);
				assertTrue(array[i].compareTo(array[j]) > 0);
			}
		}
		
	}
	
	private void testComparisons(BigInteger a, BigInteger b, int lenA, int lenB) {
		UIntBase uIntA = new UIntBase(a, lenA);
		UIntBase uIntB = new UIntBase(b, lenB);
		
		assertTrue(uIntA.equals(uIntA));
		assertTrue(uIntB.equals(uIntB));
		if (a.equals(b)) {
			assertTrue(uIntA.equals(uIntB));
			assertTrue(uIntB.equals(uIntA));
			assertEquals(uIntA.hashCode(), uIntB.hashCode());
			int compAB = uIntA.compareTo(uIntB);
			int compBA = uIntB.compareTo(uIntA);
			assertTrue(compAB == 0 && compBA == 0);
		} else {
			assertFalse(uIntA.equals(uIntB));
			assertFalse(uIntB.equals(uIntA));
			int compAB = uIntA.compareTo(uIntB);
			int compBA = uIntB.compareTo(uIntA);
			assertFalse(compAB == 0 && compBA != 0);
			assertFalse(compAB != 0 && compBA == 0);
			assertFalse(compAB > 0 && compBA > 0);
			assertFalse(compAB < 0 && compBA < 0);
		}
	}

}
