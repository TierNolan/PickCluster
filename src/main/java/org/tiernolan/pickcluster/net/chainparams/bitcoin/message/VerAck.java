package org.tiernolan.pickcluster.net.chainparams.bitcoin.message;

import java.io.IOException;

import org.tiernolan.pickcluster.net.chainparams.bitcoin.BitcoinMessage;
import org.tiernolan.pickcluster.net.message.VerackMessage;
import org.tiernolan.pickcluster.types.endian.EndianDataInputStream;
import org.tiernolan.pickcluster.types.endian.EndianDataOutputStream;

public class VerAck extends BitcoinMessage implements VerackMessage {
	
	public VerAck() {
		super("verack");
	}
	
	public VerAck(int version, EndianDataInputStream in) throws IOException {
		super("verack");
	}

	@Override
	public void write(int version, EndianDataOutputStream out) throws IOException {
	}

	@Override
	public String getDataString() {
		return "";
		
	}

	@Override
	public int estimateDataSize() {
		return 0;
	}

}
