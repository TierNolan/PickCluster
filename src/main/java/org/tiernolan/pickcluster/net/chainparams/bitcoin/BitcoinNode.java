package org.tiernolan.pickcluster.net.chainparams.bitcoin;

import java.io.IOException;

import org.tiernolan.pickcluster.net.P2PNode;

public class BitcoinNode extends P2PNode {

	private HeaderTracker tracker;
	
	public BitcoinNode(BitcoinChainParams params, long services) throws IOException {
		super("BitcoinNode", params, 0L);
	}
	
	protected void addGlobalMessageHandlers() throws IOException {
		this.tracker = new HeaderTracker(this, (BitcoinChainParams) params);
		addGlobalMessageHandler("headers", tracker.getHeadersMessageHandler());
	}
	
	protected void addOnConnectMessageHandlers() throws IOException {
		addConnectMessageHandler(tracker.getOnConnectMessageHandler());
	}
	
	public void interrupt() {
		super.interrupt();
		tracker.shutdownSaveThread(false);
	}

}
