package org.tiernolan.pickcluster.net.message.common;

import java.io.IOException;

import org.tiernolan.pickcluster.net.message.Message;
import org.tiernolan.pickcluster.types.endian.EndianDataInputStream;
import org.tiernolan.pickcluster.types.endian.EndianDataOutputStream;

public class GetAddrCommon extends Message {
	
	public GetAddrCommon() {
		super("getaddr");
	}
	
	public GetAddrCommon(int version, EndianDataInputStream in) throws IOException {
		super("getaddr");
	}

	@Override
	public void write(int version, EndianDataOutputStream out) throws IOException {
	}
	
	@Override
	public GetAddrCommon read(int version, EndianDataInputStream in, Object ... extraParams) throws IOException {
		return new GetAddrCommon(version, in);
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
