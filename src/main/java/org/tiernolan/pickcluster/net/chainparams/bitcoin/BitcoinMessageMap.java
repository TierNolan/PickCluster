package org.tiernolan.pickcluster.net.chainparams.bitcoin;

import org.tiernolan.pickcluster.net.message.MessageMap;

public class BitcoinMessageMap extends MessageMap {
	
	public BitcoinMessageMap(BitcoinMessageMap map) {
		super(map);
	}
	
	public BitcoinMessageMap() {
	}
	
	@Override
	protected void addAllConstructors() {
	}
	
	@Override
	public BitcoinMessageMap copyConstructorsOnly() {
		return new BitcoinMessageMap(this);
	}
	
}
