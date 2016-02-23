package org.tiernolan.pickcluster.types;

import java.io.IOException;

import org.tiernolan.pickcluster.types.endian.EndianDataOutputStream;

public interface NetType {
	
	public void write(int version, EndianDataOutputStream out) throws IOException;
	
	public int estimateSize();

}
