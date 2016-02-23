package org.tiernolan.pickcluster.types;

import java.io.IOException;
import java.math.BigInteger;

import org.tiernolan.pickcluster.types.endian.Endian;
import org.tiernolan.pickcluster.types.endian.EndianDataInputStream;

public class UInt256 extends UIntBase {
	
	private final static int SIZE = 32;
	
	public UInt256(BigInteger value) {
		super(value, SIZE);
	}
	
	public UInt256(EndianDataInputStream in) throws IOException {
		this(in, Endian.LITTLE);
	}
	
	public UInt256(EndianDataInputStream in, int endian) throws IOException {
		super(in, SIZE, endian);
	}

	public UInt256(byte[] buf) {
		this(buf, Endian.LITTLE);
	}
	
	public UInt256(byte[] buf, int endian) {
		super(buf, SIZE, endian);
	}

}
