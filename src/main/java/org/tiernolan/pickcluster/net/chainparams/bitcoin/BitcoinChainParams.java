package org.tiernolan.pickcluster.net.chainparams.bitcoin;

import java.math.BigInteger;
import java.util.List;

import org.tiernolan.pickcluster.net.blockchain.HeaderInfo;
import org.tiernolan.pickcluster.net.chainparams.ChainParameters;
import org.tiernolan.pickcluster.net.chainparams.bitcoin.types.BitcoinHeader;
import org.tiernolan.pickcluster.net.message.MessageProtocol;
import org.tiernolan.pickcluster.types.SocketAddressType;
import org.tiernolan.pickcluster.types.TargetBits;
import org.tiernolan.pickcluster.types.UInt256;
import org.tiernolan.pickcluster.types.encode.Convert;
import org.tiernolan.pickcluster.types.reference.Header;

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
	private final BitcoinHeader genesis;
	private final MessageProtocol messageProtocol;
	private final List<SocketAddressType> fixedSeeds;
	
	public static final BitcoinHeader BITCOIN_MAIN_GENESIS = 
			new BitcoinHeader(
					1, 
					new UInt256(BigInteger.ZERO), 
					new UInt256(new BigInteger(Convert.hexToBytes("004a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b"))),
					1231006505,
					new TargetBits(TargetBits.bitsToTarget(0x1d00ffff)),
					2083236893,
					0);
	public static final BitcoinChainParams BITCOIN_MAIN = new BitcoinChainParams(
				"main",
				8333,
				2 * 1024 * 1024,
				210000,
				0xf9beb4d9,
				10 * 60,
				14 * 24 * 60 * 60,
				new BigInteger(Convert.hexToBytes("00000000ffffffffffffffffffffffffffffffffffffffffffffffffffffffff")),
				BITCOIN_MAIN_GENESIS,
				new BitcoinMessageProtocol(),
				BitcoinFixedSeeds.MAIN_SEEDS
			);
	
	protected BitcoinChainParams(String name, int defaultPort, int maxMessage, int subsidyHalving, int messagePrefix, int retargetSpacing, int retargetTimespan, BigInteger maxPOWTarget, BitcoinHeader genesis, MessageProtocol messageProtocol, List<SocketAddressType> fixedSeeds) {
		this.name = name;
		this.defaultPort = defaultPort;
		this.maxMessage = maxMessage;
		this.subsidyHalving = subsidyHalving;
		this.messagePrefix = messagePrefix;
		this.retargetSpacing = retargetSpacing;
		this.retargetTimespan = retargetTimespan;
		this.retargetBlockCount = retargetTimespan / retargetSpacing;
		this.maxPOWTarget = maxPOWTarget;
		this.genesis = genesis;
		this.messageProtocol = messageProtocol; 
		this.fixedSeeds = fixedSeeds;
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
	public int getBigEndianMessagePrefix() {
		return this.messagePrefix;
	}
	
	@Override
	public int getDefaultPort() {
		return this.defaultPort;
	}
	
	@Override
	public BitcoinHeader getGenesis() {
		return this.genesis;
	}
	
	@Override
	public MessageProtocol getMessageProtocol() {
		return this.messageProtocol;
	}

	public TargetBits getTargetBits(HeaderInfo<BitcoinHeader> thisInfo, int height) {
		BitcoinHeader last = (BitcoinHeader) thisInfo.getParentInfo().getHeader();
		if ((height % retargetBlockCount) != 0) {
			return last.getBits();
		}
		BitcoinHeader first = thisInfo.getAncestorInfo(height - retargetBlockCount).getHeader();
		
		long end = last.getTimestamp() & 0xFFFFFFFFL;
		long start = first.getTimestamp() & 0xFFFFFFFFL;
		
		long timespan = end - start;
		if (timespan < (retargetTimespan / 4)) {
			timespan = retargetTimespan / 4;
		}
		if (timespan > (retargetTimespan * 4)) {
			timespan = retargetTimespan * 4;
		}
		BigInteger newTarget = last.getBits().getTarget();
		newTarget = newTarget.multiply(BigInteger.valueOf(timespan));
		newTarget = newTarget.divide(BigInteger.valueOf(retargetTimespan));
		
		if (newTarget.compareTo(maxPOWTarget) > 0) {
			newTarget = maxPOWTarget;
		}
		return new TargetBits(newTarget);
	}

	@Override
	public int getMaxMessageLength() {
		return maxMessage;
	}

	@Override
	public List<SocketAddressType> getFixedSeeds() {
		return fixedSeeds;
	}

}
