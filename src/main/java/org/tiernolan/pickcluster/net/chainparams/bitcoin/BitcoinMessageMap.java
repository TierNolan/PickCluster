package org.tiernolan.pickcluster.net.chainparams.bitcoin;

import java.io.IOException;

import org.tiernolan.pickcluster.net.chainparams.bitcoin.message.Ping;
import org.tiernolan.pickcluster.net.chainparams.bitcoin.message.Pong;
import org.tiernolan.pickcluster.net.chainparams.bitcoin.message.VerAck;
import org.tiernolan.pickcluster.net.chainparams.bitcoin.message.Version;
import org.tiernolan.pickcluster.net.message.MessageMap;
import org.tiernolan.pickcluster.types.endian.EndianDataInputStream;

public class BitcoinMessageMap extends MessageMap {
	
	public BitcoinMessageMap(BitcoinMessageMap map) {
		super(map);
	}
	
	public BitcoinMessageMap() {
		this.addAll();
		this.done();
	}
	
	private void addAll() {
		this.add("version", new BitcoinMessageConstructor<Version>() {
			@Override
			public Version getMessage(int version, EndianDataInputStream in) throws IOException {
				return new Version(version, in);
			}});
		this.add("verack", new BitcoinMessageConstructor<VerAck>() {
			@Override
			public VerAck getMessage(int version, EndianDataInputStream in) throws IOException {
				return new VerAck(version, in);
			}});
		this.add("ping", new BitcoinMessageConstructor<Ping>() {
			@Override
			public Ping getMessage(int version, EndianDataInputStream in) throws IOException {
				return new Ping(version, in);
			}});
		this.add("pong", new BitcoinMessageConstructor<Pong>() {
			@Override
			public Pong getMessage(int version, EndianDataInputStream in) throws IOException {
				return new Pong(version, in);
			}});
	}
	
	@Override
	public BitcoinMessageMap copyConstructorsOnly() {
		return new BitcoinMessageMap(this);
	}
	
}
