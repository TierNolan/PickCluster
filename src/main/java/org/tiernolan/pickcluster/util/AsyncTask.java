package org.tiernolan.pickcluster.util;

import java.io.IOException;

import org.tiernolan.pickcluster.net.MessageConnection;
import org.tiernolan.pickcluster.net.message.Message;
import org.tiernolan.pickcluster.net.message.MessageHandler;

public class AsyncTask extends Task implements Runnable {
	private final MessageHandler<Message> endHandler;
	private final TaskQueue taskQueue;
	
	public AsyncTask(MessageConnection connection, MessageHandler<Message> asyncHandler, TaskQueue taskQueue, MessageHandler<Message> endHandler) {
		super(connection, asyncHandler, -1);
		this.endHandler = endHandler;
		this.taskQueue = taskQueue;
	}
	
	@Override
	public void run() {
		try {
			super.run();
		} catch (final IOException e) {
			taskQueue.add(new MessageHandler<Message>() {
				@Override
				public void handle(MessageConnection connection, Message message) throws IOException {
					throw new IOException("IOException thrown in async task", e);
				}
			});
			return;
		}
		if (taskQueue != null && endHandler != null) {
			taskQueue.add(endHandler);
		}
	}

}