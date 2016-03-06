package org.tiernolan.pickcluster.net.chainparams.bitcoin;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.tiernolan.pickcluster.types.SocketAddressType;
import org.tiernolan.pickcluster.types.encode.Convert;

public class SeedSpec6 {
	
	private final List<SocketAddressType> seeds = new ArrayList<SocketAddressType>();
	
	public SeedSpec6 add(Object ... address) {
		byte[] ip = new byte[16];
		for (int i = 0; i < 16; i++) {
			ip[i] = (Byte) address[i];
		}
		int port = (Integer) address[16];
		try {
			InetAddress ipAddr = InetAddress.getByAddress(ip);
			SocketAddressType socketAddr = new SocketAddressType(ipAddr, port);
			seeds.add(socketAddr);
		} catch (UnknownHostException e) {
			System.out.println("Unable to process address " + Convert.bytesToHex(ip));
		}
		return this;
	}

}
