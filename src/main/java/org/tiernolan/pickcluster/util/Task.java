package org.tiernolan.pickcluster.util;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.tiernolan.pickcluster.net.MessageConnection;
import org.tiernolan.pickcluster.net.message.Message;
import org.tiernolan.pickcluster.net.message.MessageHandler;

public class Task implements Comparable<Task> {
	private static AtomicInteger idCounter = new AtomicInteger();
	
	protected final MessageHandler<Message> task;
	protected final MessageConnection connection;
	protected final int id;
	private long nextRun;
	private long period;
	
	public Task(MessageConnection connection, MessageHandler<Message> task, long seconds) {
		this.task = task;
		this.connection = connection;
		this.nextRun = TimeUtils.getCurrentTimeMillis() - 1000;
		this.period = seconds * 1000;
		this.id = idCounter.incrementAndGet();
		if (seconds == 0) {
			throw new IllegalArgumentException("Period set to zero seconds");
		}
	}

	@Override
	public int compareTo(Task o) {
		if (o == this) {
			return 0;
		}
		int c = Long.compare(nextRun, o.nextRun);
		if (c != 0) {
			return c;
		}
		c = Integer.compare(id, o.id);
		return c;
	}
	
	public boolean isRepeating() {
		return period > 0;
	}
	
	public long getNextRun() {
		return nextRun;
	}
	
	public void run() throws IOException {
		task.handle(connection, null);
		nextRun += period;
	}
	
	public static MessageHandler<Message> wrapRunnable(final Runnable runnable) {
		if (runnable == null) {
			return null;
		}
		return new MessageHandler<Message>() {
			@Override
			public void handle(MessageConnection connection, Message message) throws IOException {
				runnable.run();
			}		
		};
	}
}