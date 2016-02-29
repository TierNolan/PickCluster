package org.tiernolan.pickcluster.util;

import java.io.IOException;
import java.util.concurrent.ConcurrentSkipListSet;

import org.tiernolan.pickcluster.net.MessageConnection;
import org.tiernolan.pickcluster.net.message.Message;
import org.tiernolan.pickcluster.net.message.MessageHandler;

public class TaskQueue extends ConcurrentSkipListSet<Task> {

	private static final long serialVersionUID = 1L;
	
	private final Object notifyObject;

	public TaskQueue() {
		this(null);
	}
	
	public TaskQueue(Object notifyObject) {
		this.notifyObject = notifyObject == null ? this : notifyObject;
	}
	
	public boolean add(final Runnable runnable) {
		return add(runnable, -1L);
	}
	
	public boolean add(final Runnable runnable, long seconds) {
		return add(Task.wrapRunnable(runnable), seconds);
	}
	
	public boolean add(final MessageHandler<Message> handler) {
		return add(handler, -1L);
	}
	
	public boolean add(final MessageHandler<Message> handler, long seconds) {
		try {
			Task task = new Task((MessageConnection) null, handler, seconds);
			return add(task);
		} finally {
			synchronized (notifyObject) {
				notifyObject.notify();
			}
		}
	}
	
	public boolean add(final Task task) {
		try {
			return super.add(task);
		} finally {
			synchronized (notifyObject) {
				notifyObject.notify();
			}
		}
	}
	
	public boolean runTask() throws IOException, InterruptedException {
		if (runTaskIfPending()) {
			return true;
		}
		synchronized(notifyObject) {
			if (runTaskIfPending()) {
				return true;
			}
			if (isEmpty()) {
				notifyObject.wait();
			} else {
				notifyObject.wait(getTimeUntilNextTask());
			}
		}
		return false;
	}
	
	public boolean runTaskIfPending() throws IOException {
		Task first = super.pollFirst();
		if (first == null) {
			return false;
		}
		if (first.getNextRun() >= TimeUtils.getCurrentTimeMillis()) {
			add(first);
			return false;
		}
		first.run();
		if (first.isRepeating()) {
			add(first);
		}
		return true;
	}
	
	public long getTimeUntilNextTask() {
		Task first = pollFirst();
		if (first == null) {
			return Integer.MAX_VALUE;
		}
		long delay = first.getNextRun() - TimeUtils.getCurrentTimeMillis();
		return Math.max(delay, 0) + 1;
	}
	
}
