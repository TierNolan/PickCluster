package org.tiernolan.pickcluster.net.message.common;

import java.io.IOException;

import org.tiernolan.pickcluster.net.message.Message;
import org.tiernolan.pickcluster.net.message.reference.VerackMessage;
import org.tiernolan.pickcluster.types.endian.EndianDataInputStream;
import org.tiernolan.pickcluster.types.endian.EndianDataOutputStream;

public class VerAckCommon extends Message implements VerackMessage {
	
	public VerAckCommon() {
		super("verack");
	}
	
	public VerAckCommon(int version, EndianDataInputStream in) throws IOException {
		super("verack");
	}

	@Override
	public void write(int version, EndianDataOutputStream out) throws IOException {
	}
	
	public VerAckCommon read(int version, EndianDataInputStream in, Object ... extraParams) throws IOException {
		return new VerAckCommon(version, in);
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
