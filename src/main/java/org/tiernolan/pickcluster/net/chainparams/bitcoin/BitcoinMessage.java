package org.tiernolan.pickcluster.net.chainparams.bitcoin;

import org.tiernolan.pickcluster.net.message.Message;

public abstract class BitcoinMessage extends Message {

	protected BitcoinMessage(String command) {
		super(command);
	}

}
