package org.tiernolan.pickcluster.types;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;

import org.tiernolan.pickcluster.types.encode.Convert;
import org.tiernolan.pickcluster.types.endian.Endian;
import org.tiernolan.pickcluster.types.endian.EndianDataInputStream;
import org.tiernolan.pickcluster.types.endian.EndianDataOutputStream;
import org.tiernolan.pickcluster.util.ByteArray;

public class UIntBase implements Comparable<UIntBase>, NetType {
	
	private final byte[] buf;
	private final BigInteger value;
	private final int hash;
	
	public UIntBase(BigInteger value, int len) {
		this(valueToBuf(value, len), Endian.BIG);
	}
	
	public UIntBase(EndianDataInputStream in, int len) throws IOException {
		this(in, len, Endian.LITTLE);
	}
	
	public UIntBase(EndianDataInputStream in, int len, int endian) throws IOException {
		this(ByteArray.readBuf(in, len), endian);
	}
	
	protected UIntBase(byte[] buf, int len, int endian) {
		this(buf, endian);
		if (buf.length != len) {
			throw new IllegalArgumentException("Buffer length is not " + len);
		}
	}

	private UIntBase(byte[] buf, int endian) {
		this.buf = buf;
		if (endian == Endian.BIG) {
			Endian.swapInPlace(this.buf);
		}
		byte[] buf2 = new byte[this.buf.length + 1];
		for (int i = 1; i < buf2.length; i++) {
			buf2[i] = buf[buf.length - i];
		}
		this.value = new BigInteger(buf2);
		int h = 1;
		int i = buf.length - 1;
		while (i >= 0 && buf[i] == 0)
			i--;
		
		for (; i >= 0; i--) {
			h += (h << 5) + buf[i];
		}
		this.hash = h;
	}
	
	public byte[] getBytes() {
		byte[] data = new byte[this.buf.length];
		System.arraycopy(this.buf, 0, data, 0, data.length);
		return data;
	}
	
	public BigInteger toBigInteger() {
		return this.value;
	}
	
	protected int getLength() {
		return buf.length;
	}
	
	public UIntBase read(int version, EndianDataInputStream in, Object ... extraParams) throws IOException {
		return new UIntBase(in, buf.length, (Integer) extraParams[0]);
	}
	
	public void write(int version, EndianDataOutputStream out) throws IOException {
		write(out);
	}
	
	public void write(EndianDataOutputStream out) throws IOException {
		out.write(this.buf);
	}
	
	@Override
	public int hashCode() {
		return this.hash;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o instanceof UIntBase) {
			UIntBase other = (UIntBase) o;
			if (this.hash != other.hash) {
				return false;
			}
			if (this.buf.length != other.buf.length) {
				if (this.buf.length > other.buf.length) {
					for (int i = other.buf.length; i < this.buf.length; i++) {
						if (this.buf[i] != 0) {
							return false;
						}
					}
				} else {
					for (int i = this.buf.length; i < other.buf.length; i++) {
						if (other.buf[i] != 0) {
							return false;
						}
					}
				}
				int common = Math.min(this.buf.length, other.buf.length);
				for (int i = common - 1; i >= 0; i--) {
					if (this.buf[i] != other.buf[i]) {
						return false;
					}
				}
				return true;
			}
			return Arrays.equals(this.buf, other.buf);
		}
		return false;
	}

	@Override
	public int compareTo(UIntBase o) {
		if (this.buf.length != o.buf.length) {
			if (this.buf.length > o.buf.length) {
				for (int i = o.buf.length; i < this.buf.length; i++) {
					if (this.buf[i] != 0) {
						return 1;
					}
				}
			} else {
				for (int i = this.buf.length; i < o.buf.length; i++) {
					if (o.buf[i] != 0) {
						return -1;
					}
				}
			}
		}
		int common = Math.min(this.buf.length, o.buf.length);
		for (int i = common - 1; i >= 0; i--) {
			int x = this.buf[i] & 0xFF;
			int y = o.buf[i] & 0xFF;
			int c = x - y;
			if (c != 0) {
				return c;
			}
		}
		return 0;
	}
	
	@Override
	public String toString() {
		return this.value.toString();
	}
	
	public String toHexString() {
		return toHexString(false);
	}
	
	public String toHexString(boolean upper) {
		return Convert.bytesToHex(this.buf, upper);
	}
	
	private static byte[] valueToBuf(BigInteger value, int len) {
		if (value.compareTo(BigInteger.ZERO) < 0) {
			throw new IllegalArgumentException("Value is negative " + value);
		}
		byte[] buf = new byte[len];
		byte[] encoded = value.toByteArray();
		if (encoded.length <= buf.length) {
			System.arraycopy(encoded, 0, buf, buf.length - encoded.length, encoded.length);
		} else if (encoded.length == buf.length + 1 && encoded[0] == 0) {
			System.arraycopy(encoded, 1, buf, 0, buf.length);
		} else {
			throw new IllegalArgumentException("Value of " + value + " cannot be encoded in " + len + " bytes");
		}
		return buf;
	}

	@Override
	public int estimateSize() {
		return buf.length;
	}

}
