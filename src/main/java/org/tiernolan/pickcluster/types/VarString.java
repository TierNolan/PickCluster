package org.tiernolan.pickcluster.types;

import java.io.IOException;

import org.tiernolan.pickcluster.net.chainparams.BadBehaviourIOException;
import org.tiernolan.pickcluster.types.endian.EndianDataInputStream;
import org.tiernolan.pickcluster.types.endian.EndianDataOutputStream;

public class VarString implements NetType {
	
	private final String string;
	private final VarInt varIntLength;
	
	public VarString(String string) {
		this.string = string;
		this.varIntLength = new VarInt(string.length());
		for (int i = 0; i < string.length(); i++) {
			if ((string.charAt(i) & 0xFF00) != 0) {
				throw new IllegalArgumentException("String contains non-ASCII characters");
			}
		}
	}
	
	public VarString(EndianDataInputStream in, int maxLength) throws IOException {
		this.varIntLength = new VarInt(in);
		long length = varIntLength.getValue();
		if (length > maxLength) {
			throw new BadBehaviourIOException("Maximum length for string exceeded", 100);
		}
		char[] chars = new char[(int) length];
		for (int i = 0; i < chars.length; i++) {
			chars[i] = (char) (in.readByte() & 0xFF);
		}
		this.string = new String(chars);
	}
	
	public void write(int version, EndianDataOutputStream out) throws IOException {
		write(out);
	}
	
	public void write(EndianDataOutputStream out) throws IOException {
		this.varIntLength.write(out);
		for (int i = 0; i < string.length(); i++) {
			out.writeByte(string.charAt(i));
		}
	}
	
	public String getValue() {
		return string;
	}
	
	public String toString() {
		return string.replace("\0", "\\0");
	}

	@Override
	public int estimateSize() {
		return string.length() + varIntLength.estimateSize();
	}
}
