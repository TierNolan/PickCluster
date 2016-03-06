package org.tiernolan.pickcluster.net.chainparams;

import java.util.List;

import org.tiernolan.pickcluster.net.message.MessageProtocol;
import org.tiernolan.pickcluster.types.SocketAddressType;
import org.tiernolan.pickcluster.types.reference.Header;

public interface ChainParameters {
	
	public String getNetworkName();
	
	public long getSubsidy(int height);
	
	public int getMaxMessageLength();
	public int getBigEndianMessagePrefix();
	public int getDefaultPort();
	public <T extends Header<T>> T getGenesis();
	public MessageProtocol getMessageProtocol();
	public List<SocketAddressType> getFixedSeeds();
}
