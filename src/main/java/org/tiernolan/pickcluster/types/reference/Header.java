package org.tiernolan.pickcluster.types.reference;

import java.math.BigInteger;

import org.tiernolan.pickcluster.net.blockchain.HeaderInfo;
import org.tiernolan.pickcluster.net.chainparams.ChainParameters;
import org.tiernolan.pickcluster.types.NetType;
import org.tiernolan.pickcluster.types.UInt256;

public interface Header<T extends Header<T>> extends NetType {
	
	public UInt256 getPreviousHash();
	public UInt256 getHash();
	public boolean verifyPOW();
	public boolean verifyConnect(HeaderInfo<T> thisInfo, int thisHeight, ChainParameters params);
	public BigInteger getDifficulty();

}