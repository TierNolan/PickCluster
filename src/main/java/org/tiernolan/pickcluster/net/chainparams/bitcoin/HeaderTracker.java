package org.tiernolan.pickcluster.net.chainparams.bitcoin;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.tiernolan.pickcluster.net.MessageConnection;
import org.tiernolan.pickcluster.net.blockchain.HeaderTree;
import org.tiernolan.pickcluster.net.chainparams.bitcoin.message.BitcoinHeaders;
import org.tiernolan.pickcluster.net.chainparams.bitcoin.message.BitcoinSendHeaders;
import org.tiernolan.pickcluster.net.chainparams.bitcoin.types.BitcoinHeader;
import org.tiernolan.pickcluster.net.message.Message;
import org.tiernolan.pickcluster.net.message.MessageHandler;
import org.tiernolan.pickcluster.net.message.common.GetHeadersCommon;
import org.tiernolan.pickcluster.net.message.common.InvCommon;
import org.tiernolan.pickcluster.types.InventoryType;
import org.tiernolan.pickcluster.types.UInt256;
import org.tiernolan.pickcluster.util.Pair;

public class HeaderTracker {
	
	private final HeaderTree<BitcoinHeader> tree;
	private final BitcoinNode node;
	
	public HeaderTracker(BitcoinNode node, BitcoinChainParams params) throws IOException {
		this.node = node;
		File networkDir = new File("data", params.getNetworkName());
		File headersDir = new File(networkDir, "headers");
		
		tree = new HeaderTree<BitcoinHeader>(node, params.getGenesis(), headersDir, params);
	}
	
	private GetHeadersCommon getGetHeaders() {
		UInt256 stop = new UInt256(BigInteger.ZERO);
		UInt256[] locators = tree.getLocators();
		return new GetHeadersCommon(BitcoinMessageProtocol.PROTOCOL_VERSION, locators, stop);
	}
	
	public MessageHandler<Message> getOnConnectMessageHandler() {
		return new OnConnectGetHeadersTask();
	}

	public List<Pair<String, MessageHandler<? extends Message>>> getMessageHandlers() {
		List<Pair<String, MessageHandler<? extends Message>>> handlers = new ArrayList<Pair<String, MessageHandler<? extends Message>>>();
		
		handlers.add(new Pair<String, MessageHandler<? extends Message>>("headers", new HeadersMessageHandler()));
		handlers.add(new Pair<String, MessageHandler<? extends Message>>("inv", new InvMessageHandler()));
		
		return handlers;
	}
	
	public void shutdownSaveThread(boolean wait) {
		tree.interruptSaveThread(wait);
	}
	
	private class OnConnectGetHeadersTask implements MessageHandler<Message> {
		@Override
		public void handle(MessageConnection connection, Message message) throws IOException {
			connection.sendMessage(new BitcoinSendHeaders());
			connection.sendMessage(getGetHeaders());
		}
	}
	
	private class HeadersMessageHandler implements MessageHandler<BitcoinHeaders> {
		@Override
		public void handle(MessageConnection connection, BitcoinHeaders message) {
			int count = 0;
			for (int i = 0; i < message.length(); i++) {
				BitcoinHeader header = message.getHeader(i);
				try {
					if (tree.add(header)) {
						count++;
					}
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
			}
			System.out.println(node.getServerType() + "/HeaderTracker: Received new headers (" + count + "/" + message.length() + "), tree height is " + tree.getChainTipInfo().getHeight());
			if (message.length() == BitcoinHeaders.MAX_HEADERS_LENGTH) {
				GetHeadersCommon getHeaders = getGetHeaders();
				connection.sendMessage(getHeaders);
			}
		}
	}
	
	private class InvMessageHandler implements MessageHandler<InvCommon> {
		@Override
		public void handle(MessageConnection connection, InvCommon message) {
			for (int i = 0; i < message.length(); i++) {
				InventoryType inv = message.getInventory(i);
				if (inv.getType() != InventoryType.MSG_BLOCK) {
					continue;
				}
				if (!tree.contains(inv.getHash())) {
					System.out.println(node.getServerType() + "/HeaderTracker: Inv received of unknown block hash");
					GetHeadersCommon getHeaders = getGetHeaders();
					connection.sendMessage(getHeaders);
					return;
				}
			}
		}
	}
	
	private class BroadcastGetHeadersTask implements Runnable {
		@Override
		public void run() {
			node.broadcast(getGetHeaders());
		}
	}
	
}
