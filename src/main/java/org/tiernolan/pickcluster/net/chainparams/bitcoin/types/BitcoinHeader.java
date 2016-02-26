package org.tiernolan.pickcluster.net.chainparams.bitcoin.types;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;

import org.tiernolan.pickcluster.crypt.Digest;
import org.tiernolan.pickcluster.net.blockchain.HeaderInfo;
import org.tiernolan.pickcluster.net.chainparams.ChainParameters;
import org.tiernolan.pickcluster.net.chainparams.bitcoin.BitcoinChainParams;
import org.tiernolan.pickcluster.types.NetType;
import org.tiernolan.pickcluster.types.TargetBits;
import org.tiernolan.pickcluster.types.UInt256;
import org.tiernolan.pickcluster.types.VarInt;
import org.tiernolan.pickcluster.types.endian.EndianDataInputStream;
import org.tiernolan.pickcluster.types.endian.EndianDataOutputStream;
import org.tiernolan.pickcluster.types.reference.Header;
import org.tiernolan.pickcluster.util.StringCreator;

public class BitcoinHeader implements NetType, Header<BitcoinHeader> {
	
	private final int version;
	private final UInt256 previous;
	private final UInt256 merkleRoot;
	private final int timestamp;
	private final TargetBits bits;
	private final int nonce;
	private final VarInt transactionCount;
	private final UInt256 hash;
	
	public BitcoinHeader(int version, UInt256 previous, UInt256 merkleRoot, int timestamp, TargetBits bits, int nonce, int transactionCount) {
		this.version = version;
		this.previous = previous;
		this.merkleRoot = merkleRoot;
		this.timestamp = timestamp;
		this.bits = bits;
		this.nonce = nonce;
		this.transactionCount = new VarInt(transactionCount);
		this.hash = computeHash();
	}
	
	public BitcoinHeader(EndianDataInputStream in) throws IOException {
		this.version = in.readLEInt();
		this.previous = new UInt256(in);
		this.merkleRoot = new UInt256(in);
		this.timestamp = in.readLEInt();
		this.bits = new TargetBits(in);
		this.nonce = in.readLEInt();
		this.transactionCount = new VarInt(in);
		this.hash = computeHash();
	}
	
	public void write(int version, EndianDataOutputStream out) throws IOException {
		write(out);
	}

	private void write(EndianDataOutputStream out) throws IOException {
		write(out, false);
	}
	
	private void write(EndianDataOutputStream out, boolean forHash) throws IOException {
		out.writeLEInt(this.version);
		this.previous.write(out);
		this.merkleRoot.write(out);
		out.writeLEInt(this.timestamp);
		this.bits.write(out);
		out.writeLEInt(this.nonce);
		if (!forHash) {
			this.transactionCount.write(out);
		}
	}
	
	public BitcoinHeader read(int version, EndianDataInputStream in, Object ... extraParams) throws IOException {
		return new BitcoinHeader(in);
	}
	
	public int getVersion() {
		return version;
	}
	
	public UInt256 getMerkleRoot() {
		return merkleRoot;
	}
	
	public int getTimestamp() {
		return timestamp;
	}
	
	public TargetBits getBits() {
		return bits;
	}
	
	public int getNonce() {
		return nonce;
	}
	
	public VarInt getTransactionCount() {
		return transactionCount;
	}

	@Override
	public int estimateSize() {
		return 89;
	}

	@Override
	public UInt256 getPreviousHash() {
		return this.previous;
	}

	@Override
	public UInt256 getHash() {
		return this.hash;
	}

	public BigInteger getPOW() {
		return hash.toBigInteger();
	}
	
	public BigInteger getTarget() {
		return bits.getTarget();
	}
	
	@Override
	public boolean verifyPOW() {
		return hash.toBigInteger().compareTo(bits.getTarget()) < 0;
	}
	
	@Override
	public boolean verifyConnect(HeaderInfo<BitcoinHeader> thisInfo, int thisHeight, ChainParameters params) {
		if (thisHeight < 1) {
			return false;
		}
		// Check previous link
		if (!this.previous.equals(thisInfo.getParentInfo().getHeader().getHash())) {
			return false;
		}
		// Check that the target bits match expected
		BitcoinChainParams bitcoinParams = (BitcoinChainParams) params;
		TargetBits expected = bitcoinParams.getTargetBits(thisInfo, thisHeight);
		if (!getBits().equals(expected)) {
			return false;
		}
		return true;
	}
	
	@Override
	public BigInteger getDifficulty() {
		return BigInteger.valueOf(2).pow(256).divide(bits.getTarget());
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof BitcoinHeader) {
			return getHash().equals(((BitcoinHeader) o).getHash());
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return hash.hashCode();
	}

	@Override
	public String toString() {
		return new StringCreator()
			.add("version", version)
			.add("previous", previous)
			.add("merkle", merkleRoot)
			.add("timestamp", timestamp)
			.add("bits", String.format("%08x", bits.getBits()))
			.add("nonce", String.format("%08x", nonce))
			.add("tx_count", transactionCount)
			.toString();
	}
	
	private UInt256 computeHash() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(90);
		EndianDataOutputStream eos = new EndianDataOutputStream(baos);
		try {
			this.write(eos, true);
			eos.flush();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		return Digest.doubleSHA256(baos.toByteArray());				
	}
}
