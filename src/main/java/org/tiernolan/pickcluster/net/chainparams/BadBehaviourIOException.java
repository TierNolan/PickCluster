package org.tiernolan.pickcluster.net.chainparams;

import java.io.IOException;

public class BadBehaviourIOException extends IOException {
	
	private static final long serialVersionUID = 1L;
	
	private final int banPercent;
	
	public BadBehaviourIOException(String message, int banPercent) {
		super(message);
		this.banPercent = banPercent;
	}
	
	public int getBanPercent() {
		return banPercent;
	}

}
