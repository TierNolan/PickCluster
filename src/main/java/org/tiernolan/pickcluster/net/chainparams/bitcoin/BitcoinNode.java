package org.tiernolan.pickcluster.net.chainparams.bitcoin;

import java.io.IOException;

import org.tiernolan.pickcluster.net.P2PNode;

public class BitcoinNode extends P2PNode {

	public BitcoinNode(BitcoinChainParams params, long services) throws IOException {
		super("BitcoinNode", params, 0L);
	}
	
	protected void addGlobalMessageHandlers() throws IOException {
		super.addGlobalMessageHandlers();
	}
	
	protected void addOnConnectMessageHandlers() throws IOException {
		super.addOnConnectMessageHandlers();
	}
	
	public void interrupt() {
		super.interrupt();
	}

}
