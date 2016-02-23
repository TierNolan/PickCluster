package org.tiernolan.pickcluster.net.message;

import java.io.IOException;

import org.tiernolan.pickcluster.types.endian.EndianDataInputStream;


public interface MessageConstructor<T extends Message> {

	public T getMessage(int version, EndianDataInputStream in) throws IOException;
	
}
