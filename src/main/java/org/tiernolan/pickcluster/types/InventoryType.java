package org.tiernolan.pickcluster.types;

import java.io.IOException;

import org.tiernolan.pickcluster.types.endian.EndianDataInputStream;
import org.tiernolan.pickcluster.types.endian.EndianDataOutputStream;
import org.tiernolan.pickcluster.util.StringCreator;

public class InventoryType implements NetType {
	
	public static final InventoryType EXAMPLE = new InventoryType(0, UInt256.EXAMPLE);
	
	private final int type;
	private final UInt256 hash;
	
	public static final int ERROR_TYPE = 0;
	public static final int MSG_TX = 1;
	public static final int MSG_BLOCK = 2;
	public static final int MSG_FILTERED_BLOCK = 3;
	
	private static final String[] typeNames = new String[] {"ERROR_TYPE", "MSG_TX", "MSG_BLOCK", "MSG_FILTERED_BLOCK"};
	
	public InventoryType(int type, UInt256 hash) {
		this.type = type;
		this.hash = hash;
	}
	
	public InventoryType(EndianDataInputStream in) throws IOException {
		this.type = in.readLEInt();
		this.hash = new UInt256(in);
	}
	
	public void write(int version, EndianDataOutputStream out) throws IOException {
		write(out);
	}
	
	public void write(EndianDataOutputStream out) throws IOException {
		out.writeLEInt(type);
		hash.write(out);
	}
	
	public InventoryType read(int version, EndianDataInputStream in, Object ... extraParams) throws IOException {
		return new InventoryType(in);
	}
	
	public int getType() {
		return type;
	}
	
	public UInt256 getHash() {
		return hash;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		} 
		if (o instanceof InventoryType) {
			InventoryType other = (InventoryType) o;
			
			return other.type == this.type && hash.equals(other.hash);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return type + hash.hashCode();
	}
	
	@Override
	public String toString() {
		StringCreator sc = new StringCreator();
		return sc
			.add("type", typeToString(type) + "(" + type + "}")
			.add("hash", hash.toHexString())
			.toString();
	}
	
	@Override
	public int estimateSize() {
		return 36;
	}
	
	private static String typeToString(int type) {
		if (type < 0 || type > 3) {
			return typeNames[0];
		} else {
			return typeNames[type];
		}
	}
}