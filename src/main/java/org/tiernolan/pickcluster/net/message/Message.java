package org.tiernolan.pickcluster.net.message;

import org.tiernolan.pickcluster.types.NetType;
import org.tiernolan.pickcluster.types.UInt96;
import org.tiernolan.pickcluster.types.encode.Convert;
import org.tiernolan.pickcluster.util.StringCreator;

public abstract class Message implements NetType {
	
	private final UInt96 command;
	
	protected Message(UInt96 command) {
		this.command = command;
	}
	
	public UInt96 getCommand() {
		return this.command;
	}
	
	public String getCommandString() {
		return Convert.UInt96ToCommandString(this.command);
	}
	
	public int estimateSize() {
		return estimateDataSize() + 24;
	}
	
	public abstract int estimateDataSize();

	public abstract String getDataString();
	
	@Override
	public String toString() {
		StringCreator sc = new StringCreator(getClass().getSimpleName());
		sc.add("command", getCommandString());
		sc.add("data", getDataString());
		return sc.toString();
	}

}
