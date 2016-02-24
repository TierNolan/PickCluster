package org.tiernolan.pickcluster.net.chainparams.bitcoin;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.tiernolan.pickcluster.net.P2PNode;
import org.tiernolan.pickcluster.net.message.MessageMap;
import org.tiernolan.pickcluster.net.message.MessageProtocol;
import org.tiernolan.pickcluster.net.message.common.PingCommon;
import org.tiernolan.pickcluster.net.message.common.PongCommon;
import org.tiernolan.pickcluster.net.message.common.VerAckCommon;
import org.tiernolan.pickcluster.net.message.common.VersionCommon;
import org.tiernolan.pickcluster.net.message.reference.PingMessage;
import org.tiernolan.pickcluster.net.message.reference.PongMessage;
import org.tiernolan.pickcluster.net.message.reference.VerackMessage;
import org.tiernolan.pickcluster.net.message.reference.VersionMessage;
import org.tiernolan.pickcluster.types.NetAddr;
import org.tiernolan.pickcluster.types.SocketAddressType;
import org.tiernolan.pickcluster.types.VarString;

public class BitcoinMessageProtocol implements MessageProtocol {
	
	private final int PROTOCOL_VERSION = 70012;
	
	private final BitcoinMessageMap messageMap = (BitcoinMessageMap) new BitcoinMessageMap().lock();

	@Override
	public MessageMap getMessageMap() {
		return messageMap;
	}

	@Override
	public VersionMessage getVersionMessage(Socket socket, P2PNode node, long connectionNonce) {
		long timestamp = System.currentTimeMillis() / 1000L;
		VarString userAgent = new VarString("/" + node.getServerType() + ":0.0/");
		
		NetAddr netAddrTo;
		try {
			InetSocketAddress to = (InetSocketAddress) socket.getRemoteSocketAddress();
			SocketAddressType addrTo = new SocketAddressType(to.getAddress(), to.getPort());
			netAddrTo = new NetAddr(0L, addrTo);
		} catch (UnknownHostException e) {
			netAddrTo = NetAddr.NULL_ADDRESS;
		}
		
		NetAddr netAddrFrom = node.getNetAddr();
		
		int height = 0;
		
		return new VersionCommon(PROTOCOL_VERSION, node.getServices(), timestamp, netAddrTo, netAddrFrom, connectionNonce, userAgent, height, true);
	}
	
	@Override
	public VerackMessage getVerAckMessage(Socket socket, P2PNode node, long connectionNonce) {
		return new VerAckCommon();
	}
	
	@Override
	public PingMessage getPingMessage(Socket socket, P2PNode node, long pingNonce) {
		return new PingCommon(pingNonce);
	}
	
	@Override
	public PongMessage getPongMessage(Socket socket, P2PNode node, long pingNonce) {
		return new PongCommon(pingNonce);
	}

}
