package org.tiernolan.pickcluster.net.chainparams;

import org.tiernolan.pickcluster.net.message.MessageProtocol;
import org.tiernolan.pickcluster.types.UInt256;

public interface ChainParameters {
	
	public String getNetworkName();
	
	public long getSubsidy(int height);
	
	public int getMaxMessageLength();
	public TargetCalculator getTargetCalculator();
	public int getBigEndianMessagePrefix();
	public int getDefaultPort();
	public UInt256 getGenesisHash();
	public MessageProtocol getMessageProtocol();
}
