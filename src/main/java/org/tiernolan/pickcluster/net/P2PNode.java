package org.tiernolan.pickcluster.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.tiernolan.pickcluster.net.chainparams.ChainParameters;
import org.tiernolan.pickcluster.types.NetAddr;
import org.tiernolan.pickcluster.util.ThreadUtils;

public class P2PNode extends Thread {

	private final long services;
	private final String serverType;
	private final ChainParameters params;
	private final ConcurrentHashMap<Integer, MessageConnection> connections = new ConcurrentHashMap<Integer, MessageConnection>();
	private final BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<Runnable>();
	
	public P2PNode(String serverType, ChainParameters params, long services) {
		this.services = services;
		this.serverType = serverType;
		this.params = params;
	}
	
	public NetAddr getNetAddr() {
		return NetAddr.NULL_ADDRESS;
	}
	
	public String getServerType() {
		return serverType;
	}
	
	public long getServices() {
		return services;
	}
	
	public void connect(InetSocketAddress addr) {
		taskQueue.add(new ConnectTask(addr));
	}
	
	public void disconnect(int connectionId) {
		MessageConnection connection = connections.get(connectionId);
		if (connection != null) {
			connection.interrupt();
		}
	}
	
	protected boolean removeConnection(MessageConnection connection) {
		return connections.remove(connection.getConnectionId(), connection);
	}
	
	public void run() {
		try {
			while (!interrupted()) {
				try {
					Runnable task = taskQueue.take();
					task.run();
				} catch (InterruptedException e) {
					break;
				}
			}
		} finally {
			List<MessageConnection> connectionList = new ArrayList<MessageConnection>(connections.size());
			connectionList.addAll(connections.values());
			for (MessageConnection connection : connectionList) {
				connection.interrupt();
			}
			for (MessageConnection connection : connectionList) {
				ThreadUtils.joinUninterruptibly(connection);
				System.out.println(serverType + ": Closed connection " + connection);
			}
		}
	}
	
	private class ConnectTask implements Runnable {
		public final InetSocketAddress addr;
		
		public ConnectTask(InetSocketAddress addr) {
			this.addr = addr;
		}

		@Override
		public void run() {
			Socket socket = null;
			MessageConnection connection = null;
			try {
				socket = new Socket(addr.getAddress(), addr.getPort());
				connection = new MessageConnection(P2PNode.this, socket, true, params);
				connections.put(connection.getConnectionId(), connection);
				connection.start();
			} catch (IOException e) {
				if (socket != null) {
					try {
						socket.close();
					} catch (IOException e2) {}
				}
			}
		}

	}
	
}
