package org.tiernolan.pickcluster.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;

import org.junit.Test;
import org.tiernolan.pickcluster.types.encode.Convert;
import org.tiernolan.pickcluster.types.endian.EndianDataInputStream;
import org.tiernolan.pickcluster.types.endian.EndianDataOutputStream;

public class NetTypeBigIntegerTest {
	
	@Test
	public void testBigIntegerConstructor() throws IOException {
		Object[][] data = new Object[][] {
				{0, BigInteger.valueOf(0), "0100"},
				{1, BigInteger.valueOf(1), "0101"},
				{2, BigInteger.valueOf(2), "0102"},
				{-1, BigInteger.valueOf(-1), "01FF"},
				{0x12345, BigInteger.valueOf(0x12345), "03012345"},
				{-0x10000, BigInteger.valueOf(-0x10000), "03FF0000"},
		};

		for (Object[] test : data) {
			int i = (Integer) test[0];
			BigInteger big = (BigInteger) test[1];
			byte[] expEncoded = Convert.hexToBytes((String) test[2]);
			
			NetTypeBigInteger fromInt = new NetTypeBigInteger(i);
			assertEquals(fromInt.getValue(), big);
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			EndianDataOutputStream eos = new EndianDataOutputStream(baos);
			fromInt.write(0, eos);
			eos.flush();
			System.out.println("Got " + Convert.bytesToHex(baos.toByteArray()));
			System.out.println("Exp " + Convert.bytesToHex(expEncoded));
			assertTrue(Arrays.equals(baos.toByteArray(), expEncoded));
			
			ByteArrayInputStream bais = new ByteArrayInputStream(expEncoded);
			EndianDataInputStream eis = new EndianDataInputStream(bais);
			NetTypeBigInteger fromBytes = new NetTypeBigInteger(eis, 100);
			assertEquals(fromBytes.getValue(), big);
		}
	}

}
