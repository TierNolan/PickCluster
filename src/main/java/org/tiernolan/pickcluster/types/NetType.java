package org.tiernolan.pickcluster.types;

import java.io.IOException;

import org.tiernolan.pickcluster.types.endian.EndianDataInputStream;
import org.tiernolan.pickcluster.types.endian.EndianDataOutputStream;

public interface NetType {
	
	public NetType read(int version, EndianDataInputStream in, Object ... extraParams) throws IOException;
	public void write(int version, EndianDataOutputStream out) throws IOException;
	
	public int estimateSize();

}
