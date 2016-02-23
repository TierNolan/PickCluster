package org.tiernolan.pickcluster.crypt;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;

public class CryptRandom {
	
	private final static ThreadLocal<SecureRandom> localRandom = new ThreadLocal<SecureRandom>() {
		@Override
		public SecureRandom initialValue() {
			SecureRandom random;
			try {
				random = SecureRandom.getInstance("SHA1PRNG", "SUN");
			} catch (NoSuchAlgorithmException | NoSuchProviderException e) {
				random = new SecureRandom();
			}
			random.nextBytes(new byte[32]);
			return random;
		}
	};
	
	public static boolean nextBoolean() {
		return localRandom.get().nextBoolean();
	}

	public static int nextInt() {
		return localRandom.get().nextInt();
	}

	public static int nextInt(int n) {
		return localRandom.get().nextInt(n);
	}

	public static long nextLong() {
		return localRandom.get().nextLong();
	}
	
	public static float nextFloat() {
		return localRandom.get().nextFloat();
	}

	public static double nextDouble() {
		return localRandom.get().nextDouble();
	}
	
	public static void nextBytes(byte[] bytes) {
		localRandom.get().nextBytes(bytes);
	}

}
