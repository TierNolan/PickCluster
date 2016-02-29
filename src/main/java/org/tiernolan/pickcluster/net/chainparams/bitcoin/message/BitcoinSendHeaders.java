package org.tiernolan.pickcluster.net.chainparams.bitcoin.message;

import java.io.IOException;

import org.tiernolan.pickcluster.net.chainparams.bitcoin.types.BitcoinHeader;
import org.tiernolan.pickcluster.net.message.Message;
import org.tiernolan.pickcluster.types.endian.EndianDataInputStream;
import org.tiernolan.pickcluster.types.endian.EndianDataOutputStream;

public class BitcoinSendHeaders extends Message {
	
	public BitcoinSendHeaders() {
		super("sendheaders");
	}
	
	public BitcoinSendHeaders(int version, EndianDataInputStream in) throws IOException {
		super("sendheaders");
	}

	@Override
	public void write(int version, EndianDataOutputStream out) throws IOException {
	}
	
	@Override
	public BitcoinSendHeaders read(int version, EndianDataInputStream in, Object ... extraParams) throws IOException {
		return new BitcoinSendHeaders(version, in);
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
