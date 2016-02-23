package org.tiernolan.pickcluster.net.message.common;

import java.io.EOFException;
import java.io.IOException;

import org.tiernolan.pickcluster.net.chainparams.bitcoin.BitcoinMessage;
import org.tiernolan.pickcluster.net.message.VersionMessage;
import org.tiernolan.pickcluster.types.NetAddr;
import org.tiernolan.pickcluster.types.VarString;
import org.tiernolan.pickcluster.types.endian.EndianDataInputStream;
import org.tiernolan.pickcluster.types.endian.EndianDataOutputStream;
import org.tiernolan.pickcluster.util.StringCreator;

public class VersionCommon extends BitcoinMessage implements VersionMessage {
	
	private final static int MAX_SUBVERSION_LENGTH = 256;
	
	private final int version;
	private final long services;
	private final long timestap;
	private final NetAddr addrTo;
	private final NetAddr addrFrom;
	private final long nonce;
	private final VarString userAgent;
	private final int chainHeight;
	private final boolean relay;
	
	public VersionCommon(int version, long services, long timestamp, NetAddr addrTo, NetAddr addrFrom, long nonce, VarString userAgent, int chainHeight, boolean relay) {
		super("version");
		this.version = version;
		this.services = services;
		this.timestap = timestamp;
		this.addrTo = addrTo;
		this.addrFrom = addrFrom;
		this.nonce = nonce;
		this.userAgent = userAgent;
		this.chainHeight = chainHeight;
		this.relay = relay;
	}
	
	public VersionCommon(int version, EndianDataInputStream in) throws IOException {
		super("version");
		this.version = in.readLEInt();
		this.services = in.readLELong();
		this.timestap = in.readLELong();
		this.addrTo = new NetAddr(true, in);
		if (version == 0 || version >= 106) {
			this.addrFrom = new NetAddr(true, in);
			this.nonce = in.readLELong();
			this.userAgent = new VarString(in, MAX_SUBVERSION_LENGTH);
			this.chainHeight = in.readLEInt();
		} else {
			this.addrFrom = NetAddr.NULL_ADDRESS;
			this.nonce = 0;
			this.userAgent = new VarString("");
			this.chainHeight = 0;
		}
		boolean relay = true;
		try {
			relay = in.readBoolean();
		} catch (EOFException e) {
		}
		this.relay = relay;
	}

	@Override
	public void write(int version, EndianDataOutputStream out) throws IOException {
		out.writeLEInt(this.version);
		out.writeLELong(this.services);
		out.writeLELong(this.timestap);
		this.addrTo.write(out);
		if (version == 0 || version >= 106) {
			this.addrFrom.write(out);
			out.writeLELong(nonce);
			this.userAgent.write(out);
			out.writeLEInt(chainHeight);
		}
		if (version == 0 || version >= 70001) {
			out.writeBoolean(this.relay);
		}
	}
	
	public VersionCommon read(int version, EndianDataInputStream in, Object ... extraParams) throws IOException {
		return new VersionCommon(version, in);
	}
	
	public int getVersion() {
		return this.version;
	}
	
	public long getServices() {
		return this.services;
	}
	
	public long getTimestamp() {
		return this.timestap;
	}
	
	public NetAddr getAddrTo() {
		return this.addrTo;
	}
	
	public NetAddr getAddrFrom() {
		return this.getAddrFrom();
	}
	
	public long getNonce() {
		return this.nonce;
	}
	
	public VarString getUserAgent() {
		return this.userAgent;
	}
	
	public String getUserAgentString() {
		return this.userAgent.getValue();
	}
	
	public int getChainHeight() {
		return this.chainHeight;
	}
	
	public boolean isRelay() {
		return relay;
	}

	@Override
	public String getDataString() {
		StringCreator sc = new StringCreator();
		sc.add("version", this.version);
		sc.add("services", String.format("%016x", this.services));
		sc.add("timestamp", this.timestap);
		sc.add("addr_to", this.addrTo);
		sc.add("addr_from", this.addrFrom);
		sc.add("nonce", this.nonce);
		sc.add("agent", this.userAgent);
		sc.add("height", this.chainHeight);
		sc.add("relay", this.relay);
		return sc.toString();
		
	}

	@Override
	public int estimateDataSize() {
		return 4 + 8 + 8 + 30 + 30 + 8 + this.userAgent.estimateSize() + 4 + 1;
	}

}
