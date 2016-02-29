package org.tiernolan.pickcluster;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.tiernolan.pickcluster.net.chainparams.bitcoin.BitcoinChainParams;
import org.tiernolan.pickcluster.net.chainparams.bitcoin.BitcoinNode;

public class PickCluster {
	
	public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException {
		
		BitcoinNode node = new BitcoinNode(BitcoinChainParams.BITCOIN_MAIN, 0);
		
		node.start();
		
		node.connect(new InetSocketAddress("192.168.0.100", 8333));

		node.connect(new InetSocketAddress("localhost", 8333));
		//node.connect(new InetSocketAddress("localhost", 8333));
		//node.connect(new InetSocketAddress("localhost", 8333));
		
		System.in.read();
		
		node.interrupt();

		node.join();
		
		System.out.println("Exiting main");
	}

}
