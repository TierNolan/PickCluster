package org.tiernolan.pickcluster.net.chainparams.bitcoin;

import java.math.BigInteger;

import org.tiernolan.pickcluster.net.chainparams.ChainParameters;
import org.tiernolan.pickcluster.net.chainparams.TargetBitsContainer;
import org.tiernolan.pickcluster.net.chainparams.TargetCalculator;
import org.tiernolan.pickcluster.net.chainparams.TimestampContainer;
import org.tiernolan.pickcluster.net.message.MessageProtocol;
import org.tiernolan.pickcluster.types.TargetBits;
import org.tiernolan.pickcluster.types.UInt256;
import org.tiernolan.pickcluster.types.encode.Convert;

public class BitcoinChainParams implements ChainParameters {
	
	public final static long COIN = 100000000L;
	
	private final String name;
	private final int defaultPort;
	private final int maxMessage;
	private final int subsidyHalving;
	private final int messagePrefix;
	private final int retargetBlockCount;
	private final int retargetSpacing;
	private final int retargetTimespan;
	private final BigInteger maxPOWTarget;
	private final TargetCalculator targetCalculator;
	private final UInt256 genesisHash;
	private final MessageProtocol messageProtocol;
	
	public static final BitcoinChainParams BITCOIN_MAIN = new BitcoinChainParams(
				"main",
				8333,
				4000000,
				210000,
				0xf9beb4d9,
				10 * 60,
				14 * 24 * 60 * 60,
				new BigInteger(Convert.hexToBytes("00000000ffffffffffffffffffffffffffffffffffffffffffffffffffffffff")),
				new UInt256(Convert.hexToBytes("000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f")),
				new BitcoinMessageProtocol()
			);
	
	protected BitcoinChainParams(String name, int defaultPort, int maxMessage, int subsidyHalving, int messagePrefix, int retargetSpacing, int retargetTimespan, BigInteger maxPOWTarget, UInt256 genesisHash, MessageProtocol messageProtocol) {
		this.name = name;
		this.defaultPort = defaultPort;
		this.maxMessage = maxMessage;
		this.subsidyHalving = subsidyHalving;
		this.messagePrefix = messagePrefix;
		this.retargetSpacing = retargetSpacing;
		this.retargetTimespan = retargetTimespan;
		this.retargetBlockCount = retargetTimespan / retargetSpacing;
		this.maxPOWTarget = maxPOWTarget;
		this.genesisHash = genesisHash;
		
		this.targetCalculator = new BitcoinTargetCalculator();
		this.messageProtocol = messageProtocol; 
	}
	
	@Override
	public String getNetworkName() {
		return name;
	}
	
	@Override
	public long getSubsidy(int height) {
		int halvings = height / subsidyHalving;
		if (halvings >= 64) {
			return 0;
		}
		return (50 * COIN) >> halvings;
	}
	
	@Override
	public TargetCalculator getTargetCalculator() {
		return this.targetCalculator;
	}

	@Override
	public int getBigEndianMessagePrefix() {
		return this.messagePrefix;
	}
	
	@Override
	public int getDefaultPort() {
		return this.defaultPort;
	}
	
	@Override
	public UInt256 getGenesisHash() {
		return this.genesisHash;
	}
	
	@Override
	public MessageProtocol getMessageProtocol() {
		return this.messageProtocol;
	}
	
	private class BitcoinTargetCalculator implements TargetCalculator {

		@Override
		public TargetBits getTargetBits(TargetBitsContainer[] targets, TimestampContainer[] times, int height) {
			if ((height % retargetBlockCount) != 0) {
				return targets[height - 1].getTargetBits();
			}
			
			long end = times[height - 1].getTimestamp() & 0xFFFFFFFF;
			long start = times[height - retargetBlockCount].getTimestamp() & 0xFFFFFFFF;
			
			long timespan = end - start;
			if (timespan < (retargetTimespan / 4)) {
				timespan = retargetTimespan / 4;
			}
			if (timespan > (retargetTimespan * 4)) {
				timespan = retargetTimespan * 4;
			}
			BigInteger newTarget = targets[height - 1].getTargetBits().getTarget();
			newTarget = newTarget.multiply(BigInteger.valueOf(timespan));
			newTarget = newTarget.divide(BigInteger.valueOf(retargetTimespan));
			
			if (newTarget.compareTo(maxPOWTarget) > 0) {
				newTarget = maxPOWTarget;
			}
			return new TargetBits(newTarget);
		}
		
	}

	@Override
	public int getMaxMessageLength() {
		return maxMessage;
	}

}
