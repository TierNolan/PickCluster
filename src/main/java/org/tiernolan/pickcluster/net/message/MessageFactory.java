package org.tiernolan.pickcluster.net.message;

import org.tiernolan.pickcluster.types.endian.EndianDataInputStream;

public interface MessageFactory<T extends Message> {
	
	public T getMessage(int version, EndianDataInputStream in);
	
}
