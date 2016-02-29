package org.tiernolan.pickcluster.net.chainparams.bitcoin;

import java.io.IOException;
import java.math.BigInteger;

import org.tiernolan.pickcluster.net.MessageConnection;
import org.tiernolan.pickcluster.net.blockchain.HeaderTree;
import org.tiernolan.pickcluster.net.chainparams.bitcoin.message.BitcoinGetHeaders;
import org.tiernolan.pickcluster.net.chainparams.bitcoin.message.BitcoinHeaders;
import org.tiernolan.pickcluster.net.chainparams.bitcoin.message.BitcoinSendHeaders;
import org.tiernolan.pickcluster.net.chainparams.bitcoin.types.BitcoinHeader;
import org.tiernolan.pickcluster.net.message.Message;
import org.tiernolan.pickcluster.net.message.MessageHandler;
import org.tiernolan.pickcluster.types.UInt256;

public class HeaderTracker {
	
	private final HeaderTree<BitcoinHeader> tree;
	private final BitcoinNode node;
	
	public HeaderTracker(BitcoinNode node, BitcoinChainParams params) throws IOException {
		this.node = node;
		tree = new HeaderTree<BitcoinHeader>(params.getGenesis(), null, params);
	}
	
	private BitcoinGetHeaders getGetHeaders() {
		UInt256 stop = new UInt256(BigInteger.ZERO);
		UInt256[] locators = tree.getLocators();
		return new BitcoinGetHeaders(BitcoinMessageProtocol.PROTOCOL_VERSION, locators, stop);
	}
	
	public MessageHandler<Message> getOnConnectMessageHandler() {
		return new OnConnectGetHeadersTask();
	}

	public MessageHandler<BitcoinHeaders> getHeadersMessageHandler() {
		return new HeadersMessageHandler();
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
			System.out.println("Received new headers (" + count + "/" + message.length() + "), tree height is " + tree.getChainTipInfo().getHeight());
			if (message.length() == BitcoinHeaders.MAX_HEADERS_LENGTH) {
				BitcoinGetHeaders getHeaders = getGetHeaders();
				connection.sendMessage(getHeaders);
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
