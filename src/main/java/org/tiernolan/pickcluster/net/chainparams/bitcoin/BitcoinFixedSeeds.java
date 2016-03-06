package org.tiernolan.pickcluster.net.chainparams.bitcoin;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.tiernolan.pickcluster.types.SocketAddressType;
import org.tiernolan.pickcluster.types.encode.Convert;

public class BitcoinFixedSeeds {
	
	public final static List<SocketAddressType> MAIN_SEEDS = convert(FixedSeedsFromCPP.pnSeed6_main);
	public final static List<SocketAddressType> TEST_SEEDS = convert(FixedSeedsFromCPP.pnSeed6_test);
	
	private static List<SocketAddressType> convert(Object[][] seeds) {
		List<SocketAddressType> list = new ArrayList<SocketAddressType>(seeds.length);
		for (Object[] seed : seeds) {
			String hex = (String) seed[0];
			hex = hex.replace("0x", "");
			hex = hex.replace("{", "");
			hex = hex.replace("}", "");
			hex = hex.replace(" ", "");
			hex = hex.replace(",", "");
			byte[] ip = Convert.hexToBytes(hex);
			int port = (Integer) seed[1];
			try {
				InetAddress ipAddr = InetAddress.getByAddress(ip);
				SocketAddressType socketAddr = new SocketAddressType(ipAddr, port);
				list.add(socketAddr);
			} catch (UnknownHostException e) {
				System.out.println("Unable to process address " + Convert.bytesToHex(ip));
			}
		}
		return Collections.unmodifiableList(list);
	}
}
