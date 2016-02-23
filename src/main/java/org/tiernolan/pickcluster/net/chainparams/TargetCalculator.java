package org.tiernolan.pickcluster.net.chainparams;

import org.tiernolan.pickcluster.types.TargetBits;

public interface TargetCalculator {
	
	public TargetBits getTargetBits(TargetBitsContainer[] targets, TimestampContainer[] times, int height);

}
