package org.tiernolan.pickcluster.net.types;


import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;
import org.tiernolan.pickcluster.types.TargetBits;

public class TargetBitsTest {

	private static boolean RUN_FULL_TEST = false;
	
	private static Object[] vectors = new Object[] {
		0,          "0",          0,
		0x00123456, "0",          0,
		0x01800000, "0",          0,
		0x00800000, "0",          0,
		0x01123456, "12",         0x01120000,
		null,       "80",         0x02008000,
		0x01fedcba, "-7E",        0x01fe0000,
		0x02123456, "1234",       0x02123400,
		0x03123456, "123456",     0x03123456,
		0x04123456, "12345600",   0x04123456,
		0x04923456, "-12345600",  0x04923456,
		0x05009234, "92340000",   0x05009234,
		0x20123456, "1234560000000000000000000000000000000000000000000000000000000000", 0x20123456,
		0xff123456, "123456000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000", 0xff123456
	};
	
	@Test
	public void testVectors() {
		
		for (int i = 0; i < vectors.length; i += 3) {
			Integer x = (Integer) vectors[i];
			BigInteger expected = new BigInteger((String) vectors[i + 1], 16);

			if (x != null) {
				BigInteger decoded = TargetBits.bitsToTarget(x);
				assertEquals(Integer.toHexString(x) + " did not decode correctly", expected, decoded);
			}
			
			int encoded = TargetBits.targetToBits(expected);
			assertEquals(expected + " did not encode correctly", (Integer) vectors[i + 2], (Integer) encoded);
		}
		
	}
	
	@Test
	public void fullEncodeDecodeTest() throws InterruptedException {
		
		if (!RUN_FULL_TEST) {
			return;
		}
		
		final AtomicReference<Throwable> failed = new AtomicReference<Throwable>();

		final ConcurrentLinkedQueue<Runnable> decodeTaskQueue = new ConcurrentLinkedQueue<Runnable>();
		
		int step = 0x01000000;
		
		for (long i = 0; i < 0x100000000L; i += step) {
			final long taskId = i / step;
			final long start = i;
			final long end = Math.min(0xFFFFFFFFL, i + step);
			Runnable r = new Runnable() {
				public void run() {
					System.out.println("Starting task " + taskId + " at position " + Integer.toHexString((int) start));
					for (long i = start; i < end; i ++) {
						encodeDecode((int) i);
					}
					System.out.println("Ending task " + taskId);
				}
			};
			decodeTaskQueue.add(r);
		}

		Thread[] threads = new Thread[3];
		for (int i = 0; i < threads.length; i++) {
			final int threadId = i;
			threads[i] = new Thread(new Runnable(){
				@Override
				public void run() {
					System.out.println("Starting thread " + threadId);
					try {
						Runnable r = null;
						while (failed.get() == null && (r = decodeTaskQueue.poll()) != null) {
							r.run();
						}
					} catch (Throwable t) {
						System.out.println(t + " was thrown in thread " + threadId);
						failed.set(t);
					} finally {
						System.out.println("Exiting thread " + threadId);
					}
				}
			});
		}
		
		for (int i = 0; i < threads.length; i++) {
			threads[i].start();
		}
		
		for (int i = 0; i < threads.length; i++) {
			threads[i].join();
		}
		
		if (failed.get() != null) {
			throw new RuntimeException(failed.get());
		}
			
	}
	
	@Test
	public void encodeDecodeTest() {
		Random r = new Random();
		
		for (int i = 0; i < 10000; i++) {
			encodeDecode(r.nextInt());
		}
	}
	
	private static void encodeDecode(final int x) {
	
		BigInteger decoded = TargetBits.bitsToTarget((int) x);

		int size = (x >> 24) & 0xFF;
		
		int exp = x;

		if (size == 0 || (exp & 0xFFFFFF) == 0) {
			exp = 0;
		} else if (size == 1) {
			exp = exp & 0xFFFF0000;
		} else if (size == 2) {
			exp = exp & 0xFFFFFF00;
		}
		
		boolean neg = (x & 0x00800000) != 0;

		exp = exp & 0xFF7FFFFF;
		
		while (size > 0 & ((exp & 0xFF8000) == 0 || (exp & 0xFF8000) == 0xFF8000)) {
			size--;
			int lsb = exp & 0xFFFF;
			if (lsb == 0) {
				size = 0;
				exp = 0;
			} else {
				exp = (size << 24) | (lsb << 8);
			}
		}
		
		if (size == 0 || (exp & 0xFFFFFF) == 0) {
			exp = 0;
		} else if (size == 1) {
			exp = exp & 0xFFFF0000;
		} else if (size == 2) {
			exp = exp & 0xFFFFFF00;
		}
		
		if (neg && exp != 0) {
			exp |= 0x00800000;
		}
		
		int bits = TargetBits.targetToBits(decoded);
		if (bits != exp) {
			assertEquals(x + " decoded to " + decoded + " but did not re-encode to " + x + " as expected", exp, bits);
		}
	}

}
