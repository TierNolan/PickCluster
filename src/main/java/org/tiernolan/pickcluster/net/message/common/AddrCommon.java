package org.tiernolan.pickcluster.net.message.common;

import java.io.IOException;

import org.tiernolan.pickcluster.net.message.Message;
import org.tiernolan.pickcluster.types.NetAddr;
import org.tiernolan.pickcluster.types.NetTypeArray;
import org.tiernolan.pickcluster.types.endian.EndianDataInputStream;
import org.tiernolan.pickcluster.types.endian.EndianDataOutputStream;
import org.tiernolan.pickcluster.util.StringCreator;

public class AddrCommon extends Message {
	
	public final static int MAX_ADDR_LENGTH = 2000;
	
	private final NetTypeArray<NetAddr> addrs;
	
	public AddrCommon(NetAddr[] addrs) {
		super("addr");
		this.addrs = new NetTypeArray<NetAddr>(addrs, NetAddr.class);
	}
	
	public AddrCommon(int version, EndianDataInputStream in) throws IOException {
		super("addr");
		this.addrs = new NetTypeArray<NetAddr>(version, in, MAX_ADDR_LENGTH, NetAddr.class, NetAddr.EXAMPLE, false);
	}

	@Override
	public void write(int version, EndianDataOutputStream out) throws IOException {
		this.addrs.write(version, out);
	}
	
	@Override
	public AddrCommon read(int version, EndianDataInputStream in, Object ... extraParams) throws IOException {
		return new AddrCommon(version, in);
	}
	
	public int length() {
		return addrs.length();
	}
	
	public NetAddr getHeader(int index) {
		return addrs.get(index);
	}

	@Override
	public String getDataString() {
		StringCreator sc = new StringCreator();
		if (addrs.length() < 10) {
			for (int i = 0; i < addrs.length(); i++) {
				sc.add("header[" + i + "]", addrs.get(i));
			}
		} else {
			sc.add("header_count", addrs.length());
		}
		return sc.toString();
		
	}

	@Override
	public int estimateDataSize() {
		return 9 + 30 * addrs.length();
	}

}
