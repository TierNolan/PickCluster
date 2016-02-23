package org.tiernolan.pickcluster.types;

import java.io.IOException;

import org.tiernolan.pickcluster.net.chainparams.BadBehaviourIOException;
import org.tiernolan.pickcluster.types.endian.EndianDataInputStream;
import org.tiernolan.pickcluster.types.endian.EndianDataOutputStream;

public class VarInt implements NetType {
	
	private final long value;
	
	public VarInt(long value) {
		if (value < 0) {
			throw new IllegalArgumentException("Value is negative");
		}
		this.value = value;
	}
	
	public VarInt(EndianDataInputStream in) throws IOException {
		byte msb = in.readLEByte();
		if (msb == (byte) 0xFF) {
			value = in.readLELong();
			if (value < 0) {
				throw new BadBehaviourIOException("Integer exceeds maximum range for long", 100);
			}
		} else if (msb == (byte) 0xFE) {
			value = in.readLEInt() & 0xFFFFFFFFL;
		} else if (msb == (byte) 0xFD) {
			value = in.readLEShort() & 0xFFFFL;
		} else {
			value = msb & 0xFF;
		}
	}
	
	public void write(int version, EndianDataOutputStream out) throws IOException {
		write(out);
	}
	
	public VarInt read(int version, EndianDataInputStream in, Object ... extraParams) throws IOException {
		return new VarInt(in);
	}
	
	public void write(EndianDataOutputStream out) throws IOException {
		if (value < 0xFD) {
			out.write((byte) value);
		} else if (value <= 0xFFFF) {
			out.write((byte) 0xFD);
			out.writeLEShort((short) value);
		} else if (value <= 0xFFFFFFFFL) {
			out.write((byte) 0xFE);
			out.writeLEInt((int) value);
		} else {
			out.write((byte) 0xFF);
			out.writeLELong(value);
		}
	}
	
	public long getValue() {
		return value;
	}
	
	public String toString() {
		return Long.toString(value);
	}

	@Override
	public int estimateSize() {
		return 9;
	}
}
