package org.tiernolan.pickcluster.crypt;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.tiernolan.pickcluster.types.UInt256;
import org.tiernolan.pickcluster.types.encode.Convert;

public class DigestTest {

	@Test
	public void testSha256() {
		byte[] hello = new byte[5];
		hello[0] = 'h';
		hello[1] = 'e';
		hello[2] = 'l';
		hello[3] = 'l';
		hello[4] = 'o';
		
		UInt256 exp = new UInt256(Convert.hexToBytes("2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824"));
		
		UInt256 digest = Digest.SHA256(hello);
		
		assertEquals(exp, digest);
	}
	
	@Test
	public void testDoubleSha256() {
		byte[] hello = new byte[5];
		hello[0] = 'h';
		hello[1] = 'e';
		hello[2] = 'l';
		hello[3] = 'l';
		hello[4] = 'o';
		
		UInt256 exp = new UInt256(Convert.hexToBytes("9595c9df90075148eb06860365df33584b75bff782a510c6cd4883a419833d50"));

		UInt256 digest = Digest.doubleSHA256(hello);
		
		assertEquals(exp, digest);
	}
	
}
