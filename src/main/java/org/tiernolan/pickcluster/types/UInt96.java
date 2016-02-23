package org.tiernolan.pickcluster.types;

import java.io.IOException;
import java.math.BigInteger;

import org.tiernolan.pickcluster.types.endian.Endian;
import org.tiernolan.pickcluster.types.endian.EndianDataInputStream;

public class UInt96 extends UIntBase {
	
	private final static int SIZE = 12;
	
	public UInt96(BigInteger value) {
		super(value, SIZE);
	}
	
	public UInt96(EndianDataInputStream in) throws IOException {
		this(in, Endian.LITTLE);
	}
	
	public UInt96(EndianDataInputStream in, int endian) throws IOException {
		super(in, SIZE, endian);
	}

	public UInt96(byte[] buf) {
		this(buf, Endian.LITTLE);
	}
	
	public UInt96(byte[] buf, int endian) {
		super(buf, SIZE, endian);
	}

}
