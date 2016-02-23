package org.tiernolan.pickcluster.types;

import java.io.IOException;
import java.math.BigInteger;

import org.tiernolan.pickcluster.types.endian.Endian;
import org.tiernolan.pickcluster.types.endian.EndianDataInputStream;

public class UInt128 extends UIntBase {
	
	private final static int SIZE = 16;
	
	public UInt128(BigInteger value) {
		super(value, SIZE);
	}
	
	public UInt128(EndianDataInputStream in) throws IOException {
		this(in, Endian.LITTLE);
	}
	
	public UInt128(EndianDataInputStream in, int endian) throws IOException {
		super(in, SIZE, endian);
	}

	public UInt128(byte[] buf) {
		this(buf, Endian.LITTLE);
	}
	
	public UInt128(byte[] buf, int endian) {
		super(buf, SIZE, endian);
	}

}
