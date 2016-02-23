package org.tiernolan.pickcluster.net.chainparams.bitcoin;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.tiernolan.pickcluster.net.P2PNode;
import org.tiernolan.pickcluster.net.chainparams.bitcoin.message.Ping;
import org.tiernolan.pickcluster.net.chainparams.bitcoin.message.Pong;
import org.tiernolan.pickcluster.net.chainparams.bitcoin.message.VerAck;
import org.tiernolan.pickcluster.net.chainparams.bitcoin.message.Version;
import org.tiernolan.pickcluster.net.message.MessageMap;
import org.tiernolan.pickcluster.net.message.MessageProtocol;
import org.tiernolan.pickcluster.net.message.PingMessage;
import org.tiernolan.pickcluster.net.message.PongMessage;
import org.tiernolan.pickcluster.net.message.VerackMessage;
import org.tiernolan.pickcluster.net.message.VersionMessage;
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
		
		return new Version(PROTOCOL_VERSION, node.getServices(), timestamp, netAddrTo, netAddrFrom, connectionNonce, userAgent, height, true);
	}
	
	@Override
	public VerackMessage getVerAckMessage(Socket socket, P2PNode node, long connectionNonce) {
		return new VerAck();
	}
	
	@Override
	public PingMessage getPingMessage(Socket socket, P2PNode node, long pingNonce) {
		return new Ping(pingNonce);
	}
	
	@Override
	public PongMessage getPongMessage(Socket socket, P2PNode node, long pingNonce) {
		return new Pong(pingNonce);
	}

}
