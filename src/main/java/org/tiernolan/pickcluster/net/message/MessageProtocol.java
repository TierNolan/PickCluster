package org.tiernolan.pickcluster.net.message;

import java.net.Socket;

import org.tiernolan.pickcluster.net.P2PNode;
import org.tiernolan.pickcluster.net.message.reference.PingMessage;
import org.tiernolan.pickcluster.net.message.reference.PongMessage;
import org.tiernolan.pickcluster.net.message.reference.VerackMessage;
import org.tiernolan.pickcluster.net.message.reference.VersionMessage;

public interface MessageProtocol {
	
	public MessageMap getMessageMap();
	public VersionMessage getVersionMessage(Socket socket, P2PNode node, long connectionNonce);
	public VerackMessage getVerAckMessage(Socket socket, P2PNode node, long connectionNonce);
	public PingMessage getPingMessage(Socket socket, P2PNode node, long pingNonce);
	public PongMessage getPongMessage(Socket socket, P2PNode node, long pingNonce);

}
