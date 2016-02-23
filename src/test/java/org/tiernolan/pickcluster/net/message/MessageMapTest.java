package org.tiernolan.pickcluster.net.message;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.junit.Test;
import org.tiernolan.pickcluster.net.MessageInputStream;
import org.tiernolan.pickcluster.net.MessageOutputStream;
import org.tiernolan.pickcluster.net.chainparams.bitcoin.BitcoinChainParams;
import org.tiernolan.pickcluster.net.chainparams.bitcoin.message.Version;
import org.tiernolan.pickcluster.net.types.MessageInputStreamTest;
import org.tiernolan.pickcluster.types.endian.EndianDataInputStream;
import org.tiernolan.pickcluster.types.endian.EndianDataOutputStream;

public class MessageMapTest {

	@Test
	public void basicTest() throws IOException {
		byte[] encodedVersionMessage = MessageInputStreamTest.VERSION_MESSAGE;
		ByteArrayInputStream bais = new ByteArrayInputStream(encodedVersionMessage);
		EndianDataInputStream eis = new EndianDataInputStream(bais);
		
		MessageInputStream mis = new MessageInputStream(eis, BitcoinChainParams.BITCOIN_MAIN);
		
		Message version = mis.getMessage();
		
		assertEquals("Unexpected command name", "version", version.getCommandString());
		assertEquals("Unexpected message class type", Version.class, version.getClass());
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		EndianDataOutputStream eos = new EndianDataOutputStream(baos);
		
		MessageOutputStream mos = new MessageOutputStream(eos, BitcoinChainParams.BITCOIN_MAIN);
		mos.setVersion(((Version) version).getVersion());
		mos.writeMessage(version);
		mos.close();
		
		byte[] buf = baos.toByteArray();
		assertTrue(Arrays.equals(buf, encodedVersionMessage));
	}
	
}
