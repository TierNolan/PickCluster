package org.tiernolan.pickcluster.types;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.tiernolan.pickcluster.types.endian.EndianDataInputStream;
import org.tiernolan.pickcluster.types.endian.EndianDataOutputStream;
import org.tiernolan.pickcluster.util.ByteArray;
import org.tiernolan.pickcluster.util.StringCreator;

public class SocketAddressType implements NetType {
	
	private static final byte[] nullAddress = new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xFF, (byte) 0xFF, 0, 0, 0, 0};
	
	private final UInt128 ip;
	private final InetAddress addr;
	private final int port;
	
	public SocketAddressType(SocketAddressType socketAddr) throws UnknownHostException {
		this(socketAddr.getAddress(), socketAddr.getPort());
	}
	
	public SocketAddressType(InetAddress addr, int port) throws UnknownHostException {
		this(addrToBytes(addr), port);
	}
	
	public SocketAddressType(EndianDataInputStream in) throws IOException {
		this(ByteArray.readBuf(in, 16), in.readBEShort());
	}
	
	private SocketAddressType(byte[] ip6Raw, int port) throws UnknownHostException {
		this.ip = new UInt128(ip6Raw);
		this.addr = getInetAddress(ip6Raw);
		this.port = port & 0xFFFF;
	}
	
	public void write(int version, EndianDataOutputStream out) throws IOException {
		write(out);
	}
	
	public void write(EndianDataOutputStream out) throws IOException {
		this.ip.write(out);
		out.writeBEShort((short) port);
	}
	
	public SocketAddressType read(int version, EndianDataInputStream in, Object ... extraParams) throws IOException {
		return new SocketAddressType(in);
	}
	
	public InetAddress getAddress() {
		return addr;
	}
	
	public int getPort() {
		return port;
	}
	
	private static InetAddress getInetAddress(byte[] addr) throws UnknownHostException {
		return InetAddress.getByAddress(addr);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		} else if (!(o instanceof SocketAddressType)) {
			return false;
		} else {
			SocketAddressType other = (SocketAddressType) o;
			
			if (other.port != port) {
				return false;
			}
			
			return ip.equals(other.ip);
		}
	}
	
	@Override
	public int hashCode() {
		return port + ip.hashCode();
	}
	
	@Override
	public String toString() {
		StringCreator sc = new StringCreator();
		return sc
			.add("ip", ip.toHexString())
			.add("port", port)
			.toString();
	}
	
	private static byte[] addrToBytes(InetAddress addr) {
		if (addr == null) {
			return nullAddress;
		} else {
			byte[] bytes = addr.getAddress();
			if (bytes.length == 4) {
				bytes = ByteArray.rightJustify(bytes, 16);
				bytes[10] = -1;
				bytes[11] = -1;
			} else if (bytes.length != 16) {
				throw new IllegalStateException("Network address (" + bytes.length + ") was not length 4 or 16");
			}
			return bytes;
		}
	}

	@Override
	public int estimateSize() {
		return ip.estimateSize() + 2;
	}

}
