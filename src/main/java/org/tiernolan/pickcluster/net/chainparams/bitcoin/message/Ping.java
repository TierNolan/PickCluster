package org.tiernolan.pickcluster.net.chainparams.bitcoin.message;

import java.io.IOException;

import org.tiernolan.pickcluster.net.chainparams.bitcoin.BitcoinMessage;
import org.tiernolan.pickcluster.net.message.PingMessage;
import org.tiernolan.pickcluster.types.endian.EndianDataInputStream;
import org.tiernolan.pickcluster.types.endian.EndianDataOutputStream;
import org.tiernolan.pickcluster.util.StringCreator;

public class Ping extends BitcoinMessage implements PingMessage {
	
	private final long nonce;
	
	public Ping(long nonce) {
		super("ping");
		this.nonce = nonce;
	}
	
	public Ping(int version, EndianDataInputStream in) throws IOException {
		super("ping");
		this.nonce = in.readLELong();
	}

	@Override
	public void write(int version, EndianDataOutputStream out) throws IOException {
		out.writeLELong(nonce);
	}
	
	public long getNonce() {
		return this.nonce;
	}
	
	@Override
	public String getDataString() {
		StringCreator sc = new StringCreator();
		sc.add("nonce", this.nonce);
		return sc.toString();
		
	}

	@Override
	public int estimateDataSize() {
		return 8;
	}

	@Override
	public long getPingNonce() {
		return nonce;
	}

}
