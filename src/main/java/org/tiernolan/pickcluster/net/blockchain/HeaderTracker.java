package org.tiernolan.pickcluster.net.blockchain;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.tiernolan.pickcluster.net.MessageConnection;
import org.tiernolan.pickcluster.net.P2PNode;
import org.tiernolan.pickcluster.net.chainparams.ChainParameters;
import org.tiernolan.pickcluster.net.chainparams.bitcoin.BitcoinMessageProtocol;
import org.tiernolan.pickcluster.net.chainparams.bitcoin.types.BitcoinHeader;
import org.tiernolan.pickcluster.net.message.Message;
import org.tiernolan.pickcluster.net.message.MessageHandler;
import org.tiernolan.pickcluster.net.message.common.GetHeadersCommon;
import org.tiernolan.pickcluster.net.message.common.HeadersCommon;
import org.tiernolan.pickcluster.net.message.common.InvCommon;
import org.tiernolan.pickcluster.net.message.common.SendHeadersCommon;
import org.tiernolan.pickcluster.types.InventoryType;
import org.tiernolan.pickcluster.types.UInt256;
import org.tiernolan.pickcluster.types.reference.Header;
import org.tiernolan.pickcluster.util.Pair;

public class HeaderTracker<T extends Header<T>> {
	
	private final HeaderTree<T> tree;
	private final P2PNode node;
	
	public HeaderTracker(P2PNode node, ChainParameters params) throws IOException {
		this.node = node;
		File networkDir = new File("data", params.getNetworkName());
		File headersDir = new File(networkDir, "headers");
		
		T genesis = params.getGenesis();
		tree = new HeaderTree<T>(node, genesis, headersDir, params);
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
			connection.sendMessage(new SendHeadersCommon());
			connection.sendMessage(getGetHeaders());
		}
	}
	
	private class HeadersMessageHandler implements MessageHandler<HeadersCommon<T>> {
		@Override
		public void handle(MessageConnection connection, HeadersCommon<T> message) {
			int count = 0;
			for (int i = 0; i < message.length(); i++) {
				T header = message.getHeader(i);
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
			if (count > 0 && message.length() == HeadersCommon.MAX_HEADERS_LENGTH) {
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
