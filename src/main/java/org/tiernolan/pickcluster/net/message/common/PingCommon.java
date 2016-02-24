package org.tiernolan.pickcluster.net.message.common;

import java.io.IOException;

import org.tiernolan.pickcluster.net.message.Message;
import org.tiernolan.pickcluster.net.message.reference.PingMessage;
import org.tiernolan.pickcluster.types.endian.EndianDataInputStream;
import org.tiernolan.pickcluster.types.endian.EndianDataOutputStream;
import org.tiernolan.pickcluster.util.StringCreator;

public class PingCommon extends Message implements PingMessage {
	
	private final long nonce;
	
	public PingCommon(long nonce) {
		super("ping");
		this.nonce = nonce;
	}
	
	public PingCommon(int version, EndianDataInputStream in) throws IOException {
		super("ping");
		this.nonce = in.readLELong();
	}

	@Override
	public void write(int version, EndianDataOutputStream out) throws IOException {
		out.writeLELong(nonce);
	}
	
	public PingCommon read(int version, EndianDataInputStream in, Object ... extraParams) throws IOException {
		return new PingCommon(version, in);
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
