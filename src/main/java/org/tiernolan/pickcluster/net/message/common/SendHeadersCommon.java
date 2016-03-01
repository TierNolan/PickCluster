package org.tiernolan.pickcluster.net.message.common;

import java.io.IOException;

import org.tiernolan.pickcluster.net.message.Message;
import org.tiernolan.pickcluster.types.endian.EndianDataInputStream;
import org.tiernolan.pickcluster.types.endian.EndianDataOutputStream;

public class SendHeadersCommon extends Message {
	
	public SendHeadersCommon() {
		super("sendheaders");
	}
	
	public SendHeadersCommon(int version, EndianDataInputStream in) throws IOException {
		super("sendheaders");
	}

	@Override
	public void write(int version, EndianDataOutputStream out) throws IOException {
	}
	
	@Override
	public SendHeadersCommon read(int version, EndianDataInputStream in, Object ... extraParams) throws IOException {
		return new SendHeadersCommon(version, in);
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
