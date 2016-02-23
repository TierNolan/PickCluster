package org.tiernolan.pickcluster.net.message;

import java.io.IOException;

import org.tiernolan.pickcluster.net.MessageConnection;


public interface MessageHandler<T extends Message> {

	public void handle(MessageConnection connection, T message) throws IOException;
	
}
