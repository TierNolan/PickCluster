package org.tiernolan.pickcluster.net.chainparams;

import org.tiernolan.pickcluster.net.message.MessageProtocol;
import org.tiernolan.pickcluster.types.reference.Header;

public interface ChainParameters {
	
	public String getNetworkName();
	
	public long getSubsidy(int height);
	
	public int getMaxMessageLength();
	public int getBigEndianMessagePrefix();
	public int getDefaultPort();
	public Header<?> getGenesis();
	public MessageProtocol getMessageProtocol();
}
