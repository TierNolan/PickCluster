package org.tiernolan.pickcluster.types;

import java.io.IOException;
import java.math.BigInteger;

import org.tiernolan.pickcluster.types.endian.Endian;
import org.tiernolan.pickcluster.types.endian.EndianDataInputStream;

public class UInt160 extends UIntBase {
	
	private final static int SIZE = 20;
	
	public UInt160(BigInteger value) {
		super(value, SIZE);
	}
	
	public UInt160(EndianDataInputStream in) throws IOException {
		this(in, Endian.LITTLE);
	}
	
	public UInt160(EndianDataInputStream in, int endian) throws IOException {
		super(in, SIZE, endian);
	}

	public UInt160(byte[] buf) {
		this(buf, Endian.LITTLE);
	}
	
	public UInt160(byte[] buf, int endian) {
		super(buf, SIZE, endian);
	}
	
	public UInt160 read(int version, EndianDataInputStream in, Object ... extraParams) throws IOException {
		return new UInt160(in, (Integer) extraParams[0]);
	}

}
