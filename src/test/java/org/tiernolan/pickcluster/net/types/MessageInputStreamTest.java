package org.tiernolan.pickcluster.net.types;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.Test;
import org.tiernolan.pickcluster.net.MessageInputStream;
import org.tiernolan.pickcluster.net.chainparams.bitcoin.BitcoinChainParams;
import org.tiernolan.pickcluster.net.message.Message;
import org.tiernolan.pickcluster.types.encode.Convert;
import org.tiernolan.pickcluster.types.endian.EndianDataInputStream;

public class MessageInputStreamTest {

	public final static byte[] VERSION_MESSAGE = Convert.hexToBytes(
			"f9beb4d976657273696f6e0000000000" +
			"64000000358d493262ea000001000000" +
			"0000000011b2d0500000000001000000" +
			"0000000000000000000000000000ffff" +
			"00000000000000000000000000000000" +
			"0000000000000000ffff000000000000" +
			"3b2eb35d8ce617650f2f5361746f7368" +
			"693a302e372e322fc03e0300"
			);
	
	@Test
	public void basicMessageTest() throws IOException {
		ByteArrayInputStream bais = new ByteArrayInputStream(VERSION_MESSAGE);
		EndianDataInputStream eis = new EndianDataInputStream(bais);
		
		MessageInputStream mis = new MessageInputStream(eis, BitcoinChainParams.BITCOIN_MAIN);
		
		Message version = mis.getMessage();
		
		assertEquals("Unexpected command name", "version", version.getCommandString());
	}
	
}
