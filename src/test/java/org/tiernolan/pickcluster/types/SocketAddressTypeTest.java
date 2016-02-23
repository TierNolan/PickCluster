package org.tiernolan.pickcluster.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Random;

import org.junit.Test;
import org.tiernolan.pickcluster.types.endian.EndianDataInputStream;
import org.tiernolan.pickcluster.types.endian.EndianDataOutputStream;

public class SocketAddressTypeTest {
	
	private final int TEST_LENGTH = 100;
	
	@Test
	public void testIP4() throws IOException {
		
		PipedOutputStream pos = new PipedOutputStream();
		PipedInputStream pis = new PipedInputStream(pos, TEST_LENGTH * 18);
		
		EndianDataOutputStream eos = new EndianDataOutputStream(pos);
		EndianDataInputStream eis = new EndianDataInputStream(pis);
		
		byte[][] ips = new byte[TEST_LENGTH][];
		InetAddress[] addrs = new InetAddress[TEST_LENGTH];
		int[] ports = new int[TEST_LENGTH];
		
		Random r = new Random();
		
		for (int i = 0; i < TEST_LENGTH; i++) {
			ips[i] = new byte[4];
			r.nextBytes(ips[i]);
			addrs[i] = InetAddress.getByAddress(ips[i]);
			ports[i] = r.nextInt() & 0xFFFF;
			eos.write(new byte[10]);
			eos.writeShort(-1);
			eos.write(ips[i]);
			eos.writeBEShort((short) ports[i]);
		}
		
		byte[] expectedArray = new byte[18];
		expectedArray[10] = -1;
		expectedArray[11] = -1;
		
		for (int i = 0; i < TEST_LENGTH; i++) {
			SocketAddressType s = new SocketAddressType(eis);
			assertEquals(addrs[i], s.getAddress());
			assertEquals(ports[i], s.getPort());
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			EndianDataOutputStream eos2 = new EndianDataOutputStream(baos);
			
			s.write(eos2);
			eos2.flush();
			byte[] bytes = baos.toByteArray();
			expectedArray[16] = (byte) (ports[i] >> 8);			
			expectedArray[17] = (byte) (ports[i]);
			
			System.arraycopy(ips[i], 0, expectedArray, 12, 4);
			
			assertTrue(Arrays.equals(expectedArray, bytes));
		}
		
	}
	
	@Test
	public void testIP6() throws IOException {
		
		PipedOutputStream pos = new PipedOutputStream();
		PipedInputStream pis = new PipedInputStream(pos, TEST_LENGTH * 18);
		
		EndianDataOutputStream eos = new EndianDataOutputStream(pos);
		EndianDataInputStream eis = new EndianDataInputStream(pis);
		
		byte[][] ips = new byte[TEST_LENGTH][];
		InetAddress[] addrs = new InetAddress[TEST_LENGTH];
		int[] ports = new int[TEST_LENGTH];
		
		Random r = new Random();
		
		for (int i = 0; i < TEST_LENGTH; i++) {
			ips[i] = new byte[16];
			r.nextBytes(ips[i]);
			addrs[i] = InetAddress.getByAddress(ips[i]);
			ports[i] = r.nextInt() & 0xFFFF;
			eos.write(ips[i]);
			eos.writeBEShort((short) ports[i]);
		}
		
		byte[] expectedArray = new byte[18];
		
		for (int i = 0; i < TEST_LENGTH; i++) {
			SocketAddressType s = new SocketAddressType(eis);
			assertEquals(addrs[i], s.getAddress());
			assertEquals(ports[i], s.getPort());
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			EndianDataOutputStream eos2 = new EndianDataOutputStream(baos);
			
			s.write(eos2);
			eos2.flush();
			byte[] bytes = baos.toByteArray();
			expectedArray[16] = (byte) (ports[i] >> 8);			
			expectedArray[17] = (byte) (ports[i]);
			
			System.arraycopy(ips[i], 0, expectedArray, 0, 16);
			
			assertTrue(Arrays.equals(expectedArray, bytes));
		}
		
	}

}
