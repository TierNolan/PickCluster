package org.tiernolan.pickcluster.types;

import java.io.IOException;
import java.math.BigInteger;

import org.tiernolan.pickcluster.types.endian.EndianDataInputStream;
import org.tiernolan.pickcluster.types.endian.EndianDataOutputStream;
import org.tiernolan.pickcluster.util.StringCreator;

public class TargetBits implements NetType {
	
	public static final TargetBits EXAMPLE = new TargetBits(BigInteger.ZERO);
	
	private final int bits;
	private final BigInteger target;
	
	public TargetBits(BigInteger difficulty) {
		this.bits = targetToBits(difficulty);
		this.target = bitsToTarget(bits);
	}
	
	public TargetBits(EndianDataInputStream in) throws IOException {
		this.bits = in.readLEInt();
		this.target = bitsToTarget(bits);
	}
	
	public void write(int version, EndianDataOutputStream out) throws IOException {
		write(out);
	}
	
	public void write(EndianDataOutputStream out) throws IOException {
		out.writeLEInt(bits);
	}
	
	public TargetBits read(int version, EndianDataInputStream in, Object ... extraParams) throws IOException {
		return new TargetBits(in);
	}
	
	public int getBits() {
		return bits;
	}
	
	public BigInteger getTarget() {
		return target;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		} else if (!(o instanceof TargetBits)) {
			return false;
		} else {
			return bits == ((TargetBits) o).bits;
		}
	}
	
	@Override
	public int hashCode() {
		return bits;
	}

	public static int targetToBits(BigInteger target) {
		boolean neg = target.compareTo(BigInteger.ZERO) < 0;
		
		byte[] mant = target.abs().toByteArray();
		if (mant.length == 1 && mant[0] == 0) {
			mant = new byte[0];
		}
		int size = mant.length;
		if (size > 255) {
			throw new IllegalArgumentException("Target exceeds maximum length");
		}

		int encoded = 0;
		encoded = size << 24;
		if (mant.length > 0) {
			encoded |= (mant[0] & 0xFF) << 16;
		}
		if (mant.length > 1) {
			encoded |= (mant[1] & 0xFF) << 8;
		}
		if (mant.length > 2) {
			encoded |= (mant[2] & 0xFF);
		}
		
		if (neg) {
			return encoded | 0x00800000;
		} else {
			return encoded;
		}
	}
	
	public static BigInteger bitsToTarget(int bits) {
		
		int size = (bits >> 24) & 0xFF;
		
		boolean neg = (bits & 0x00800000) != 0;
		
		bits = bits & 0xFF7FFFFF;
		
		byte[] mant = new byte[3];
		
		if (size >= 1) {
			mant[0] = (byte) (bits >> 16);
		}
		
		if (size >= 2) {
			mant[1] = (byte) (bits >> 8);
		}
		
		if (size >= 3) {
			mant[2] = (byte) (bits);
		}
		
		BigInteger m = new BigInteger(mant);
		m = m.shiftLeft((size - 3) * 8);
		
		if (neg) {
			return m.negate();
		} else {
			return m;
		}
	}
	
	@Override
	public String toString() {
		return new StringCreator()
			.add("bits", "0x" + Integer.toHexString(bits))
			.add("target", target)
			.toString();
	}

	@Override
	public int estimateSize() {
		return 4;
	}

}
