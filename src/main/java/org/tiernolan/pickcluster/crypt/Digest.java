package org.tiernolan.pickcluster.crypt;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.tiernolan.pickcluster.types.UInt256;

public class Digest {
	
	private static ThreadLocal<MessageDigest> SHA256DigestLocal = new ThreadLocal<MessageDigest>() {
		@Override
		public MessageDigest initialValue() {
			try {
				return MessageDigest.getInstance("SHA-256");
			} catch (NoSuchAlgorithmException e) {
				throw new IllegalStateException(e);
			}
		}
	};		
		
	public static UInt256 SHA256(byte[] buf) {
		return SHA256(buf, 0, buf.length);
	}
	
	public static UInt256 SHA256(byte[] buf, int off, int len) {
		MessageDigest digestEngine = SHA256DigestLocal.get();
		digestEngine.reset();
		digestEngine.update(buf, off, len);
		byte[] digest = digestEngine.digest();
		
		return new UInt256(digest);
	}

	public static UInt256 doubleSHA256(byte[] buf) {
		return doubleSHA256(buf, 0, buf.length);
	}
	
	public static UInt256 doubleSHA256(byte[] buf, int off, int len) {
		MessageDigest digestEngine = SHA256DigestLocal.get();
		digestEngine.reset();
		digestEngine.update(buf, off, len);
		byte[] digest1 = digestEngine.digest();
		digestEngine.update(digest1);
		byte[] digest2 = digestEngine.digest();
		
		return new UInt256(digest2);
	}

}
