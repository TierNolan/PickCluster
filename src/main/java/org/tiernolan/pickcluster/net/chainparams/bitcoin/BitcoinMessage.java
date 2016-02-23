package org.tiernolan.pickcluster.net.chainparams.bitcoin;

import org.tiernolan.pickcluster.net.message.Message;
import org.tiernolan.pickcluster.types.encode.Convert;

public abstract class BitcoinMessage extends Message {

	protected BitcoinMessage(String command) {
		super(Convert.commandStringToUInt96(command));
	}

}
