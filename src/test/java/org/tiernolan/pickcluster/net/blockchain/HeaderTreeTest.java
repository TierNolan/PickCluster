package org.tiernolan.pickcluster.net.blockchain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;

import org.junit.Test;
import org.tiernolan.pickcluster.net.chainparams.BadBehaviourIOException;
import org.tiernolan.pickcluster.net.chainparams.ChainParameters;
import org.tiernolan.pickcluster.types.NetType;
import org.tiernolan.pickcluster.types.UInt256;
import org.tiernolan.pickcluster.types.endian.EndianDataInputStream;
import org.tiernolan.pickcluster.types.endian.EndianDataOutputStream;
import org.tiernolan.pickcluster.types.reference.Header;

public class HeaderTreeTest {

	private final static boolean DELETE_FILES = true;
	
	@Test
	public void testPersist() throws IOException {
		
		File f = new File("test_dir");
		if (f.exists()) {
			if (!f.isDirectory()) {
				throw new IOException(f + " is not a directory");
			}
			if (f.listFiles().length != 0) {
				throw new IOException(f + " is not empty");
			}
		}

		try {

			int LENGTH1 = 12345;
			int LENGTH_FULL = 23456;

			BasicHeader genesis = new BasicHeader(new UInt256(BigInteger.valueOf(0)), null, null);
			BasicHeader first = new BasicHeader(new UInt256(BigInteger.valueOf(1)), genesis.getHash(), null);

			BasicHeader[] initialChain = new BasicHeader[LENGTH_FULL];

			initialChain[0] = genesis;
			initialChain[1] = first;

			int id = 2;

			for (int i = 2; i < LENGTH_FULL; i++) {
				initialChain[i] = new BasicHeader(new UInt256(BigInteger.valueOf(id++)), initialChain[i-1].getHash(), initialChain[i-2].getHash());
			}

			HeaderTree<BasicHeader> tree = new HeaderTree<BasicHeader>(genesis, f, null);

			for (int i = 0; i < LENGTH1; i++) {
				tree.add(initialChain[i]);
			}

			HeaderInfo<BasicHeader> oldTip = tree.getHeaderInfo(tree.getChainTip());

			tree.interruptSaveThread(true);

			tree = new HeaderTree<BasicHeader>(genesis, f, null);

			HeaderInfo<BasicHeader> tip = tree.getHeaderInfo(tree.getChainTip());

			assertEquals(tip.getHeader(), oldTip.getHeader());

			for (int i = 0; i < LENGTH1; i++) {
				BasicHeader ancestor = tip.getAncestorInfo(i).getHeader();
				assertEquals(ancestor, initialChain[i]);
			}
			
			for (int i = LENGTH1; i < LENGTH_FULL; i++) {
				tree.add(initialChain[i]);
			}
			
			oldTip = tree.getHeaderInfo(tree.getChainTip());
			
			tree.interruptSaveThread(true);
			
			tree = new HeaderTree<BasicHeader>(genesis, f, null);

			tip = tree.getHeaderInfo(tree.getChainTip());

			assertEquals(tip.getHeader(), oldTip.getHeader());
			
			for (int i = 0; i < LENGTH_FULL; i++) {
				BasicHeader ancestor = tip.getAncestorInfo(i).getHeader();
				assertEquals(ancestor, initialChain[i]);
			}
			
			tree.interruptSaveThread(true);
		} finally {
			if (DELETE_FILES && f != null && f.isDirectory()) {
				boolean success = true;
				for (File f1 : f.listFiles()) {
					success &= f1.delete();
				}
				assertTrue(success);
			}
		}
	}
	
	@Test
	public void testConnect() throws IOException {
		BasicHeader genesis = new BasicHeader(new UInt256(BigInteger.valueOf(1)), null, null);
		BasicHeader first = new BasicHeader(new UInt256(BigInteger.valueOf(2)), genesis.getHash(), null);
		BasicHeader second = new BasicHeader(new UInt256(BigInteger.valueOf(3)), first.getHash(), genesis.getHash());
		
		BasicHeader badSecond = new BasicHeader(new UInt256(BigInteger.valueOf(4)), first.getHash(), first.getHash());
		
		HeaderTree<BasicHeader> tree = new HeaderTree<BasicHeader>(genesis, null, null);
		
		tree.add(first);
		boolean thrown = false;
		try {
			tree.add(badSecond);
		} catch (BadBehaviourIOException e) {
			thrown = true;
		}
		assertTrue(thrown);
		tree.add(second);
	}
	
	@Test
	public void testReorg() throws IOException {
		testReorg(20, 5, 10);
		testReorg(10, 5, 20);
	}
	
	@Test
	public void testSkipList() throws IOException {
		int LENGTH = 12345;
		
		BasicHeader genesis = new BasicHeader(new UInt256(BigInteger.valueOf(0)), null, null);
		BasicHeader first = new BasicHeader(new UInt256(BigInteger.valueOf(1)), genesis.getHash(), null);
		
		BasicHeader[] initialChain = new BasicHeader[LENGTH];
		
		initialChain[0] = genesis;
		initialChain[1] = first;
		
		int id = 2;
		
		for (int i = 2; i < LENGTH; i++) {
			initialChain[i] = new BasicHeader(new UInt256(BigInteger.valueOf(id++)), initialChain[i-1].getHash(), initialChain[i-2].getHash());
		}
		
		HeaderTree<BasicHeader> tree = new HeaderTree<BasicHeader>(genesis, null, null);

		for (int i = 0; i < LENGTH; i++) {
			tree.add(initialChain[i], false);
		}
		
		HeaderInfo<BasicHeader> tip = tree.getHeaderInfo(tree.getChainTip());
		
		for (int i = 0; i < LENGTH; i++) {
			BasicHeader ancestor = tip.getAncestorInfo(i).getHeader();
			assertEquals(ancestor, initialChain[i]);
		}
	}
	
	private void testReorg(int initialHeight, int forkHeight, int forkLength) throws IOException {
		
		BasicHeader genesis = new BasicHeader(new UInt256(BigInteger.valueOf(0)), null, null);
		BasicHeader first = new BasicHeader(new UInt256(BigInteger.valueOf(1)), genesis.getHash(), null);
		
		BasicHeader[] initialChain = new BasicHeader[initialHeight];
		BasicHeader[] forkChain = new BasicHeader[forkLength];
		
		initialChain[0] = genesis;
		initialChain[1] = first;
		
		int id = 2;
		
		for (int i = 2; i < initialHeight; i++) {
			initialChain[i] = new BasicHeader(new UInt256(BigInteger.valueOf(id++)), initialChain[i-1].getHash(), initialChain[i-2].getHash());
		}
		
		forkChain[0] = new BasicHeader(new UInt256(BigInteger.valueOf(id++)), initialChain[forkHeight].getHash(), initialChain[forkHeight-1].getHash());
		forkChain[1] = new BasicHeader(new UInt256(BigInteger.valueOf(id++)), forkChain[0].getHash(), initialChain[forkHeight].getHash());
		
		for (int i = 2; i < forkLength; i++) {
			forkChain[i] = new BasicHeader(new UInt256(BigInteger.valueOf(id++)), forkChain[i-1].getHash(), forkChain[i-2].getHash());
		}
		
		for (int i = 0; i < forkLength; i++) {
			forkChain[i].setDifficulty(BigInteger.ZERO);
		}

		forkChain[forkChain.length - 2].setDifficulty(BigInteger.valueOf(initialHeight - forkHeight - 1));
		
		forkChain[forkChain.length - 1].setDifficulty(BigInteger.valueOf(1));

		
		HeaderTree<BasicHeader> tree = new HeaderTree<BasicHeader>(genesis, null, null);
		
		for (int i = 1; i < initialHeight; i++) {
			tree.add(initialChain[i]);
		}
		
		assertEquals(tree.getChainTipPOW(), BigInteger.valueOf(initialHeight));
		
		for (int i = 0; i < forkLength - 1; i++) {
			tree.add(forkChain[i]);
		}

		assertTrue(tree.getChainTip().getHash().equals(initialChain[initialChain.length - 1].getHash()));
		assertEquals(tree.getChainTipPOW(), BigInteger.valueOf(initialHeight));
		
		for (int i = 0; i < initialHeight; i++) {
			assertTrue(tree.isOnMainChain(initialChain[i]));
		}

		for (int i = 0; i < forkLength - 1; i++) {
			assertFalse(tree.isOnMainChain(forkChain[i]));
		}
		
		tree.add(forkChain[forkLength - 1]);
		
		assertTrue(tree.getChainTip().getHash().equals(forkChain[forkChain.length - 1].getHash()));
		
		assertEquals(tree.getChainTipPOW(), BigInteger.valueOf(initialHeight + 1));
		
		for (int i = 0; i <= forkHeight; i++) {
			assertTrue(tree.isOnMainChain(initialChain[i]));
		}
		
		for (int i = forkHeight + 1; i < initialHeight; i++) {
			assertFalse(tree.isOnMainChain(initialChain[i]));
		}

		for (int i = 0; i < forkLength; i++) {
			assertTrue(tree.isOnMainChain(forkChain[i]));
		}
		
	}
	
	public class BasicHeader implements Header<BasicHeader> {
		
		private final UInt256 hash;
		private final UInt256 parent;
		private final UInt256 grandparent;
		private boolean validPOW = true;
		private BigInteger difficulty = BigInteger.ONE;
		
		public BasicHeader(UInt256 hash, UInt256 parent, UInt256 grandparent) {
			this.hash = hash;
			this.parent = parent;
			this.grandparent = grandparent;
		}
		
		public BasicHeader(EndianDataInputStream in) throws IOException {
			this.hash = new UInt256(in);
			this.parent = in.readByte() == 0 ? null : new UInt256(in);
			this.grandparent = in.readByte() == 0 ? null : new UInt256(in);
		}

		@Override
		public NetType read(int version, EndianDataInputStream in, Object... extraParams) throws IOException {
			return new BasicHeader(in);
		}

		@Override
		public void write(int version, EndianDataOutputStream out) throws IOException {
			this.hash.write(out);
			if (parent == null) {
				out.writeByte(0);
			} else {
				out.writeByte(1);
				this.parent.write(out);
			}
			if (grandparent == null) {
				out.writeByte(0);
			} else {
				out.writeByte(1);
				this.grandparent.write(out);
			}
		}

		@Override
		public int estimateSize() {
			return 96;
		}

		@Override
		public UInt256 getPreviousHash() {
			return parent;
		}
		
		public UInt256 getSecondPreviousHash() {
			return grandparent;
		}
		
		@Override
		public UInt256 getHash() {
			return hash;
		}
		
		public void setValidPOW(boolean validPOW) {
			this.validPOW = validPOW;
		}

		@Override
		public boolean verifyPOW() {
			return validPOW;
		}

		@SuppressWarnings("rawtypes")
		@Override
		public boolean verifyConnect(HeaderInfo thisInfo, int thisHeight, ChainParameters params) {
			UInt256 parent = thisInfo.getParentInfo().getHeader().getHash();
			if (!parent.equals(getPreviousHash())) {
				return false;
			}
			if (thisHeight < 2) {
				return this.getSecondPreviousHash() == null;
			}
			UInt256 grandparent = thisInfo.getAncestorInfo(thisHeight - 2).getHeader().getHash();
			if (!getSecondPreviousHash().equals(grandparent)) {
				return false;
			}
			return true;
		}
		
		public void setDifficulty(BigInteger difficulty) {
			this.difficulty = difficulty;
		}

		@Override
		public BigInteger getDifficulty() {
			return difficulty;
		}
		
		@Override
		public boolean equals(Object o) {
			if (o instanceof BasicHeader) {
				return ((BasicHeader) o).getHash().equals(getHash());
			}
			return false;
		}
		
		@Override
		public int hashCode() {
			return hash.hashCode();
		}
		
	}
	
}
