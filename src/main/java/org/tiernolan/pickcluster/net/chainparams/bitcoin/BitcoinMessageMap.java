package org.tiernolan.pickcluster.net.chainparams.bitcoin;

import java.io.IOException;

import org.tiernolan.pickcluster.net.chainparams.bitcoin.message.BitcoinGetHeaders;
import org.tiernolan.pickcluster.net.chainparams.bitcoin.message.BitcoinHeaders;
import org.tiernolan.pickcluster.net.message.MessageConstructor;
import org.tiernolan.pickcluster.net.message.MessageMap;
import org.tiernolan.pickcluster.types.endian.EndianDataInputStream;

public class BitcoinMessageMap extends MessageMap {
	
	public BitcoinMessageMap(BitcoinMessageMap map) {
		super(map);
	}
	
	public BitcoinMessageMap() {
	}
	
	@Override
	protected void addAllConstructors() {
		this.add("headers", new MessageConstructor<BitcoinHeaders>() {
			@Override
			public BitcoinHeaders getMessage(int version, EndianDataInputStream in) throws IOException {
				return new BitcoinHeaders(version, in);
			}});
		this.add("getheaders", new MessageConstructor<BitcoinGetHeaders>() {
			@Override
			public BitcoinGetHeaders getMessage(int version, EndianDataInputStream in) throws IOException {
				return new BitcoinGetHeaders(version, in);
			}});
	}
	
	@Override
	public BitcoinMessageMap copyConstructorsOnly() {
		return new BitcoinMessageMap(this);
	}
	
}
