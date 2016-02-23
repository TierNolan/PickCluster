package org.tiernolan.pickcluster.types;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.tiernolan.pickcluster.types.endian.EndianDataInputStream;
import org.tiernolan.pickcluster.types.endian.EndianDataOutputStream;
import org.tiernolan.pickcluster.util.StringCreator;

public class NetAddr implements NetType {
	
	public static final NetAddr NULL_ADDRESS;
	
	static {
		NetAddr addr = null;
		try {
			addr = new NetAddr(0, new SocketAddressType(InetAddress.getByAddress(new byte[16]), 0));
		} catch (UnknownHostException e) {
			throw new IllegalStateException(e);
		}
		NULL_ADDRESS = addr;
		
	}
	
	private final boolean version;
	private final int timestamp;
	private final long services;
	private final SocketAddressType socket;
	
	public NetAddr(long services, SocketAddressType socket) {
		this(true, 0, services, socket);
	}
	
	public NetAddr(int timestamp, long services, SocketAddressType socket) {
		this(false, timestamp, services, socket);
	}
	
	private NetAddr(boolean version, int timestamp, long services, SocketAddressType socket) {
		this.timestamp = timestamp;
		this.services = services;
		this.socket = socket;
		this.version = version;
	}
	
	public NetAddr(boolean version, EndianDataInputStream in) throws IOException {
		this.version = version;
		if (!version) {
			this.timestamp = in.readLEInt();
		} else {
			this.timestamp = 0;
		}
		this.services = in.readLELong();
		this.socket = new SocketAddressType(in);
	}
	
	public void write(int version, EndianDataOutputStream out) throws IOException {
		write(out);
	}
	
	public void write(EndianDataOutputStream out) throws IOException {
		if (!version) {
			out.writeLEInt(this.timestamp);
		}
		out.writeLELong(this.services);
		this.socket.write(out);
	}
	
	public String toString() {
		StringCreator sc = new StringCreator();
		if (!version) {
			sc.add("timestamp", timestamp);
		}
		sc.add("services", Long.toHexString(services));
		sc.add("socket", socket);
		return sc.toString();
	}

	@Override
	public int estimateSize() {
		return 4 + 8 + this.socket.estimateSize();
	}
	
}
