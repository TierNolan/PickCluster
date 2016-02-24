package org.tiernolan.pickcluster.net.message.common;

import java.io.IOException;

import org.tiernolan.pickcluster.net.message.Message;
import org.tiernolan.pickcluster.types.UInt96;
import org.tiernolan.pickcluster.types.encode.Convert;
import org.tiernolan.pickcluster.types.endian.EndianDataInputStream;
import org.tiernolan.pickcluster.types.endian.EndianDataOutputStream;


public class UnknownMessage extends Message {
	
	private final byte[] data;
	
	public UnknownMessage(UInt96 command, byte[] data) {
		super(command);
		this.data = new byte[data.length];
		System.arraycopy(data, 0, this.data, 0, data.length);
	}
	
	public byte[] getData() {
		byte[] data = new byte[this.data.length];
		System.arraycopy(this.data, 0, data, 0, data.length);
		return data;
	}

	@Override
	public void write(int version, EndianDataOutputStream out) throws IOException {
		out.write(data);
	}
	
	public UnknownMessage read(int version, EndianDataInputStream in, Object ... extraParams) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getDataString() {
		return Convert.bytesToHex(data);
	}

	@Override
	public int estimateDataSize() {
		return data.length;
	}
	
}
