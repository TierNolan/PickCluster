package org.tiernolan.pickcluster.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.tiernolan.pickcluster.net.chainparams.ChainParameters;
import org.tiernolan.pickcluster.net.message.Message;
import org.tiernolan.pickcluster.net.message.MessageHandler;
import org.tiernolan.pickcluster.types.NetAddr;
import org.tiernolan.pickcluster.util.AsyncTask;
import org.tiernolan.pickcluster.util.CatchingThread;
import org.tiernolan.pickcluster.util.Pair;
import org.tiernolan.pickcluster.util.Task;
import org.tiernolan.pickcluster.util.TaskQueue;
import org.tiernolan.pickcluster.util.ThreadUtils;

public class P2PNode extends CatchingThread {

	private static final int CONNECT_TIMEOUT = 1000;
	public final String SERVER_TYPE = "PickCluster";
	
	private final long services;
	private final String serverType;
	protected final ChainParameters params;
	protected boolean shuttingDown = false;
	protected final ConcurrentHashMap<Integer, MessageConnection> connections = new ConcurrentHashMap<Integer, MessageConnection>();
	private final TaskQueue taskQueue = new TaskQueue(); 
	private final List<Pair<String, MessageHandler<? extends Message>>> globalMessageHandlers = new ArrayList<Pair<String, MessageHandler<? extends Message>>>();
	private final List<MessageHandler<Message>> connectMessageHandlers = new ArrayList<MessageHandler<Message>>();
	private boolean handlerListsMutable;
	private final ExecutorService executor = new ThreadPoolExecutor(4, 8, 10, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(100));
	
	public P2PNode(String serverType, ChainParameters params, long services) throws IOException {
		super(serverType + "/P2PNode");
		this.services = services;
		this.serverType = SERVER_TYPE + ":" + serverType;
		this.params = params;
		this.handlerListsMutable = true;
		addGlobalMessageHandlers();
		addOnConnectMessageHandlers();
		this.handlerListsMutable = false;
		
	}
	
	protected final void addGlobalMessageHandler(String command, MessageHandler<? extends Message> handler) {
		if (!handlerListsMutable) {
			throw new IllegalStateException("Attempt made to modify global message handlers outside constructor");
		}
		globalMessageHandlers.add(new Pair<String, MessageHandler<? extends Message>>(command, handler));
	}
	
	protected final void addConnectMessageHandler(MessageHandler<Message> handler) {
		if (!handlerListsMutable) {
			throw new IllegalStateException("Attempt made to connect global message handlers outside constructor");
		}
		connectMessageHandlers.add(handler);
	}
	
	protected void addGlobalMessageHandlers() throws IOException {
	}
	
	protected void addOnConnectMessageHandlers() throws IOException {
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
	
	public void broadcast(Message message) {
		broadcast(message, false);
	}
	
	public void broadcast(Message message, boolean fast) {
		for (Entry<Integer, MessageConnection> entry : getConnections()) {
			entry.getValue().sendMessage(message, fast);
		}
	}
	
	public Set<Entry<Integer, MessageConnection>> getConnections() {
		return Collections.unmodifiableSet(connections.entrySet());
	}
	
	public void connect(InetSocketAddress addr) {
		submitAsyncTask(new ConnectTask(addr));
	}
	
	protected void submitTask(Runnable task) {
		taskQueue.add(task);
	}
	
	protected void submitPeriodicTask(final Runnable task, final long seconds) {
		taskQueue.add(task, seconds);
	}

	protected void submitAsyncTask(Runnable asyncTask) {
		submitAsyncTask(asyncTask, null);
	}
	
	protected void submitAsyncTask(Runnable asyncTask, Runnable endTask) {
		AsyncTask task = new AsyncTask(null, Task.wrapRunnable(asyncTask), taskQueue, Task.wrapRunnable(endTask));		
		executor.execute(task);
	}
	
	public void disconnect(int connectionId) {
		MessageConnection connection = connections.get(connectionId);
		if (connection != null) {
			connection.interrupt();
		}
	}
	
	protected boolean addConnection(MessageConnection connection) {
		synchronized (connections) {
			if (shuttingDown) {
				return false;
			}
		}
		connections.put(connection.getConnectionId(), connection);
		return true;	
	}
	
	protected boolean removeConnection(MessageConnection connection) {
		return connections.remove(connection.getConnectionId(), connection);
	}
	
	public void interrupt() {
		super.interrupt();
		synchronized(taskQueue) {
			taskQueue.notify();
		}
	}
	
	@Override
	public void secondaryRun() {
		try {
			try {
				while (!interrupted()) {
					try {
						taskQueue.runTask();
					} catch (InterruptedException e) {
						break;
					} catch (IOException e) {
						System.out.println("Exception thrown when executing task, " + e);
						Thread.dumpStack();
					} catch (Throwable t) {
						t.printStackTrace();
					}
				}
			} finally {
				executor.shutdown();
				synchronized (connections) {
					shuttingDown = true;
				}
				List<MessageConnection> connectionList = new ArrayList<MessageConnection>(connections.size());
				connectionList.addAll(connections.values());
				for (MessageConnection connection : connectionList) {
					connection.interrupt();
				}
				for (MessageConnection connection : connectionList) {
					ThreadUtils.joinUninterruptibly(connection);
					System.out.println(getServerType() + ": Closed connection " + connection);
				}
				boolean shutdown = false;
				while (!shutdown) {
					try {
						shutdown = executor.awaitTermination(1, TimeUnit.SECONDS);
					} catch (InterruptedException e) {}
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
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
				socket = new Socket();
				try {
					socket.connect(addr, CONNECT_TIMEOUT);
				} catch (SocketTimeoutException e) {
					throw new IOException("Timeout when connecting to " + addr, e);
				}
				connection = new MessageConnection(P2PNode.this, socket, true, params);
				for (Pair<String, MessageHandler<? extends Message>> handler : globalMessageHandlers) {
					connection.addHandler(handler.getFirst(), handler.getSecond());
				}
				if (!addConnection(connection)) {
					throw new IOException("Unable to start connection to " + addr + " due to shutdown in progress");
				}
				for (MessageHandler<Message> handler : connectMessageHandlers) {
					connection.addImmediateHandler(handler);
				}
				connection.start();
			} catch (IOException e) {
				System.out.println(getServerType() + ": " + e.getMessage());
				if (socket != null) {
					try {
						socket.close();
					} catch (IOException e2) {}
				}
			}
		}

	}
	
}
