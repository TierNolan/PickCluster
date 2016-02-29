package org.tiernolan.pickcluster.net;

import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.tiernolan.pickcluster.crypt.CryptRandom;
import org.tiernolan.pickcluster.net.chainparams.BadBehaviourIOException;
import org.tiernolan.pickcluster.net.chainparams.ChainParameters;
import org.tiernolan.pickcluster.net.message.Message;
import org.tiernolan.pickcluster.net.message.MessageHandler;
import org.tiernolan.pickcluster.net.message.MessageMap;
import org.tiernolan.pickcluster.net.message.common.PingCommon;
import org.tiernolan.pickcluster.net.message.common.PongCommon;
import org.tiernolan.pickcluster.net.message.reference.PingMessage;
import org.tiernolan.pickcluster.net.message.reference.PongMessage;
import org.tiernolan.pickcluster.net.message.reference.VerackMessage;
import org.tiernolan.pickcluster.net.message.reference.VersionMessage;
import org.tiernolan.pickcluster.types.encode.Convert;
import org.tiernolan.pickcluster.util.CatchingThread;
import org.tiernolan.pickcluster.util.StringCreator;
import org.tiernolan.pickcluster.util.Task;
import org.tiernolan.pickcluster.util.TaskQueue;
import org.tiernolan.pickcluster.util.ThreadUtils;
import org.tiernolan.pickcluster.util.TimeUtils;

public class MessageConnection extends CatchingThread {
	
	private static AtomicInteger connectionIdCount = new AtomicInteger();
	
	private final long connectionNonce;
	private final int connectionId;
	private final InetSocketAddress remoteAddress;
	private final boolean upstream;
	private final ChainParameters params;
	private final MessageMap messageMap;
	private final Socket socket;
	private final P2PNode node;
	private final MessageInputStream mis;
	private final MessageOutputStream mos;
	private final MessageWriteThread messageWriteThread;
	
	private final PingPeriodicTask periodicPingHandler;
	private final PongHandler pongHandler;
	private final PingHandler pingHandler;
	
	private final ConcurrentLinkedQueue<Message> fastMessageQueue = new ConcurrentLinkedQueue<Message>();
	private final ConcurrentLinkedQueue<Message> messageQueue = new ConcurrentLinkedQueue<Message>();
	private final TaskQueue taskQueue; 
	private final Object handlerLock = new Object();
	
	private String remoteUserAgent;
	private int version;
	
	public MessageConnection(P2PNode node, Socket socket, boolean upstream, ChainParameters params) throws IOException {
		this.connectionId = connectionIdCount.getAndIncrement();
		if (socket == null) {
			throw new NullPointerException("Socket is null");
		}
		if (!socket.isConnected()) {
			throw new IllegalArgumentException("Socket is not connected");
		}
		super.setName(node.getName() + "/Connection-" + connectionId);
		this.messageWriteThread = new MessageWriteThread();
		this.taskQueue = new TaskQueue(messageWriteThread);
		this.remoteAddress = (InetSocketAddress) socket.getRemoteSocketAddress();
		this.params = params;
		this.messageMap = params.getMessageProtocol().getMessageMap().copyConstructorsOnly();
		this.socket = socket;
		this.node = node;
		this.upstream = upstream;
		this.mis = getInputStream(socket, params);
		this.mos = getOutputStream(socket, params);
		this.connectionNonce = CryptRandom.nextLong();
		this.periodicPingHandler = new PingPeriodicTask();
		this.pongHandler = new PongHandler(periodicPingHandler);
		this.pingHandler = new PingHandler();
		addHandlers();
	}
	
	private void addHandlers() {
		addPeriodicHandler(periodicPingHandler, 30);
		addHandler("pong", pongHandler);
		addHandler("ping", pingHandler);
	}
	
	public <T  extends Message> void addHandler(String command, MessageHandler<T> handler) {
		this.messageMap.add(Convert.commandStringToUInt96(command), handler);
	}
	
	private boolean handshake() throws IOException {
		VersionMessage versionLocal = params.getMessageProtocol().getVersionMessage(socket, node, connectionNonce);
		Message versionRemote;
		
		if (upstream) {
			mos.writeMessage((Message) versionLocal);
		}
		
		versionRemote = mis.getMessage();
		if (!(versionRemote instanceof VersionMessage)) {
			return false;
		}
		
		if (!upstream) {
			mos.writeMessage((Message) versionLocal);
		}
		
		this.version = Math.min(((VersionMessage) versionRemote).getVersion(), versionLocal.getVersion());
		this.mis.setVersion(this.version);
		this.mos.setVersion(this.version);
		this.remoteUserAgent = ((VersionMessage) versionRemote).getUserAgentString();
		
		VerackMessage verackLocal = params.getMessageProtocol().getVerAckMessage(socket, node, connectionNonce);
		
		if (upstream) {
			mos.writeMessage((Message) verackLocal);
		}

		Message verackRemote = mis.getMessage();
		if (!(verackRemote instanceof VerackMessage)) {
			return false;
		}
		
		if (upstream) {
			mos.writeMessage((Message) verackLocal);
		}
		
		System.out.println(node.getServerType() + ": Opened connection " + this);
		
		return true;
	}
	
	private MessageInputStream getInputStream(Socket socket, ChainParameters params) throws IOException {
		return new MessageInputStream(socket.getInputStream(), params);
	}
	
	private MessageOutputStream getOutputStream(Socket socket, ChainParameters params) throws IOException {
		return new MessageOutputStream(socket.getOutputStream(), params);
	}

	public void sendMessage(Message message) {
		sendMessage(message, false);
	}
	
	public void sendMessage(Message message, boolean fast) {
		(fast ? fastMessageQueue : messageQueue).add(message);
		synchronized(messageWriteThread) {
			messageWriteThread.notify();
		}
	}
	
	public int getVersion() {
		return version;
	}
	
	public String getRemoteUserAgent() {
		return remoteUserAgent;
	}
	
	public P2PNode getNode() {
		return node;
	}
	
	public void interrupt() {
		messageWriteThread.interrupt();
	}

	@Override
	public void secondaryRun() {
		int misbehaviour = 0;
		try {
			socket.setSoTimeout(5000);
			if (!handshake()) {
				return;
			}
			socket.setSoTimeout(60000);
			messageWriteThread.start();
			try {
				while (!interrupted()) {
					try {
						Message message = mis.getMessage();
						List<MessageHandler<Message>> handlers = messageMap.getHandler(message.getCommand());
						if (handlers != null) {
							synchronized (handlerLock) {
								for (MessageHandler<Message> handler : handlers) {
									handler.handle(this, message);
								}
							}
						}
					} catch (BadBehaviourIOException e) {
						e.printStackTrace();
						misbehaviour += e.getBanPercent();;
						if (misbehaviour >= 100) {
							//node.ban(this);
							this.interrupt();
						}
					}
				}
			} finally {
				messageWriteThread.interrupt();
				ThreadUtils.joinUninterruptibly(messageWriteThread);
			}
		} catch (SocketException e) {
		} catch (EOFException e) {
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			node.removeConnection(this);
		}
	}

	public long getConnectionNonce() {
		return connectionNonce;
	}

	public int getConnectionId() {
		return connectionId;
	}
	
	public SocketAddress getRemoteAddress() {
		return remoteAddress;
	}
	
	public void addPeriodicHandler(MessageHandler<Message> handler, long seconds) {
		taskQueue.add(new Task(this, handler, seconds));
	}
	
	public void addImmediateHandler(MessageHandler<Message> handler) {
		addPeriodicHandler(handler, -1L);
	}
	
	public int getLatency() {
		return this.pongHandler.getLatency();
	}
	
	@Override
	public boolean equals(Object o) {
		return o == this;
	}
	
	@Override
	public int hashCode() {
		return connectionId;
	}
	
	@Override
	public String toString() {
		StringCreator sc = new StringCreator();
		sc.add("id", getConnectionId());
		sc.add("remote_addr", getRemoteAddress());
		sc.add("remote_agent", getRemoteUserAgent());
		return sc.toString();
	}
	
	private class MessageWriteThread extends CatchingThread {
		
		public MessageWriteThread() {
			setName(MessageConnection.this.getName() + "/WriteThread");
		}
		
		@Override
		public void secondaryRun() {
			try {
				while (!interrupted()) {
					Message fastMessage = fastMessageQueue.poll();
					if (fastMessage != null) {
						mos.writeMessage(fastMessage);
						continue;
					}
					synchronized (handlerLock) {
						if (taskQueue.runTaskIfPending()) {
							continue;
						}
					}
					Message message = messageQueue.poll();
					if (message != null) {
						mos.writeMessage(message);
						continue;
					}
					synchronized(this) {
						if (!fastMessageQueue.isEmpty() || !messageQueue.isEmpty()) {
							continue;
						}
						try {
							taskQueue.runTask();
						} catch (InterruptedException e) {
							break;
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					socket.shutdownOutput();
				} catch (IOException e) {}
				try {
					mos.close();
				} catch (IOException e) {}
				MessageConnection.super.interrupt();
			}
		}
	}
	
	private class PingHandler implements MessageHandler<PingCommon> {
		
		@Override
		public void handle(MessageConnection connection, PingCommon message) throws IOException {
			PongMessage pong = params.getMessageProtocol().getPongMessage(socket, node, message.getPingNonce());
			sendMessage((Message) pong, true);
		}
	}
	
	private class PongHandler implements MessageHandler<PongCommon> {
		private final PingPeriodicTask pingHandler;
		private final long[] latencyArray;
		private int index = 0;
		private volatile int latency;
		
		public PongHandler(PingPeriodicTask pingHandler) {
			this.pingHandler = pingHandler;
			this.latencyArray = new long[10];
			Arrays.fill(this.latencyArray, 65536);
			this.latency = 0;
		}
		
		@Override
		public void handle(MessageConnection connection, PongCommon message) throws IOException {
			long now = TimeUtils.getCurrentTimeMillis();
			Long pingTime = pingHandler.getTime(message.getNonce());
			if (pingTime != null) {
				latencyArray[index] = now - pingTime;
				index++;
				if (index >= latencyArray.length) {
					index = 0;
				}
				
				long minLatency = Long.MAX_VALUE;
				for (long l : latencyArray) {
					if (l < minLatency) {
						minLatency = l;
					}
				}
				
				latency = (int) (minLatency > Integer.MAX_VALUE ? Integer.MAX_VALUE : (minLatency < 0 ? 0 : minLatency));
			} else {
				latency = 1000;
			}
		}
		
		public int getLatency() {
			return latency;
		}
	}
	
	private class PingPeriodicTask implements MessageHandler<Message> {
		private final Map<Long, Long> nonceMap = new HashMap<Long, Long>();

		@Override
		public void handle(MessageConnection connection, Message message) throws IOException {
			Long oldest = Long.MAX_VALUE;
			Long key = null;
			for (Map.Entry<Long, Long> entry : nonceMap.entrySet()) {
				if (entry.getValue() <= oldest) {
					oldest = entry.getValue();
					key = entry.getKey();
				}
			}
			if (nonceMap.size() >= 10) {
				nonceMap.remove(key);
			}
			long nonce = CryptRandom.nextLong();
			long now = TimeUtils.getCurrentTimeMillis();
			nonceMap.put(nonce, now);
			PingMessage ping = params.getMessageProtocol().getPingMessage(socket, node, nonce);
			sendMessage((Message) ping, true);
		}
		
		public Long getTime(long nonce) {
			return nonceMap.remove(nonce);
		}
		
	}

}
