package org.tiernolan.pickcluster.types;

import java.io.IOException;
import java.math.BigInteger;

import org.tiernolan.pickcluster.types.endian.EndianDataInputStream;
import org.tiernolan.pickcluster.types.endian.EndianDataOutputStream;

public class NetTypeBigInteger implements NetType {

	private final BigInteger value;
	
	public NetTypeBigInteger(long value) {
		this(BigInteger.valueOf(value));
	}
	
	public NetTypeBigInteger(BigInteger value) {
		this.value = value;
	}
	
	public NetTypeBigInteger(EndianDataInputStream in, int maxBytes) throws IOException {
		VarInt length = new VarInt(in);
		if (length.getValue() > maxBytes) {
			throw new IOException("Length of " + length.getValue() + " exceeds max value, " + maxBytes);
		}
		byte[] buf = new byte[(int) length.getValue()];
		in.readFully(buf);
		this.value = new BigInteger(buf);
	}

	@Override
	public NetType read(int version, EndianDataInputStream in, Object... extraParams) throws IOException {
		return new NetTypeBigInteger(in, (Integer) extraParams[0]);
	}

	@Override
	public void write(int version, EndianDataOutputStream out) throws IOException {
		byte[] buf = value.toByteArray();
		VarInt length = new VarInt(buf.length);
		length.write(out);
		out.write(buf);
	}

	@Override
	public int estimateSize() {
		return value.toByteArray().length + 9;
	}
	
	public BigInteger getValue() {
		return this.value;
	}

}
