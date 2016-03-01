package org.tiernolan.pickcluster.net.blockchain;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.tiernolan.pickcluster.net.P2PNode;
import org.tiernolan.pickcluster.net.chainparams.BadBehaviourIOException;
import org.tiernolan.pickcluster.net.chainparams.ChainParameters;
import org.tiernolan.pickcluster.types.UInt256;
import org.tiernolan.pickcluster.types.endian.EndianDataInputStream;
import org.tiernolan.pickcluster.types.endian.EndianDataOutputStream;
import org.tiernolan.pickcluster.types.reference.Header;
import org.tiernolan.pickcluster.util.CatchingThread;
import org.tiernolan.pickcluster.util.ThreadUtils;

public class HeaderTree<T extends Header<T>> {
	
	private boolean checkPOW = true;
	
	private final HashMap<UInt256, HeaderInfo<T>> map = new HashMap<UInt256, HeaderInfo<T>>();
	
	private final AtomicBoolean saveQueueAlive = new AtomicBoolean(true);
	private final ArrayBlockingQueue<T> saveQueue;
	private final Thread saveThread;

	private final T genesis;
	private final ChainParameters chainParams;
	private HeaderInfo<T> chainTip;
	@SuppressWarnings("rawtypes")
	private HeaderInfo[] mainChain = new HeaderInfo[1000];
	
	public HeaderTree(T genesis, File directory, ChainParameters chainParams) throws IOException {
		this(null, genesis, directory, chainParams);
	}
	
	public HeaderTree(P2PNode node, T genesis, File directory, ChainParameters chainParams) throws IOException {
		this.genesis = genesis;
		this.chainParams = chainParams;
		this.chainTip = new HeaderInfo<T>(genesis, null, 0, genesis.getDifficulty(), true);
		this.chainTip.setOnMainChain(true);
		this.mainChain[0] = this.chainTip;
		this.map.put(genesis.getHash(), chainTip);
		if (directory != null) {
			if (directory.exists()) {
				if (!directory.isDirectory()) {
					throw new IOException(directory + " is not a directory");
				} else {
					if (node != null) {
						System.out.println(node.getServerType() + "/HeaderTree: Reading headers from disk");
					}
					readFromDisk(directory);
				}
			}
			this.saveQueue = new ArrayBlockingQueue<T>(1024);
			this.saveThread = new SaveThread(directory);
			this.saveThread.start();
		} else {
			this.saveThread = null;
			this.saveQueue = null;
		}
	}

	public synchronized UInt256[] getLocators() {
		List<UInt256> locators = new ArrayList<UInt256>(32);
		T header = getChainTip();
		for (int i = 0; i < 10 && header != null; i++) {
			locators.add(header.getHash());
		}
		int step = 2;
		while (header != null) {
			locators.add(header.getHash());
			HeaderInfo<T> info = getHeaderInfo(header);
			info = info.getAncestorInfo(info.getHeight() - step);
			header = info == null ? null : info.getHeader();
			step *= 2;
		}
		if (!locators.get(locators.size() - 1).equals(genesis)) {
			locators.add(genesis.getHash());
		}
		return locators.toArray(new UInt256[0]);
	}
	
	public synchronized boolean contains(UInt256 hash) {
		return map.containsKey(hash);
	}
	
	public synchronized Boolean isOnMainChain(T header) {
		HeaderInfo<T> info = map.get(header.getHash());
		return info == null ? null : info.isOnMainChain();
	}
	
	public synchronized HeaderInfo<T> getHeaderInfo(T header) {
		return map.get(header.getHash());
	}
	
	public synchronized T getAncestorOf(T header, int height) {
		HeaderInfo<T> info = map.get(header.getHash());
		if (info == null) {
			return null;
		}
		return info.getAncestorInfo(height).getHeader();
	}
	
	@SuppressWarnings("unchecked")
	public synchronized T getMainChainHeader(int height) {
		if (height > chainTip.getHeight()) {
			return null;
		}
		return (T) this.mainChain[height].getHeader();
	}
	
	public synchronized void interruptSaveThread(boolean wait) {
		saveQueueAlive.set(false);
		if (saveThread != null) {
			saveThread.interrupt();
		}
		if (wait) {
			ThreadUtils.joinUninterruptibly(saveThread);
		}
	}
	
	public synchronized void disablePOWCheck() {
		checkPOW = false;
	}

	public synchronized boolean add(T header) throws BadBehaviourIOException {
		return add(header, false);
	}
	
	public synchronized boolean add(T header, boolean fromDisk) throws BadBehaviourIOException {
		if (map.containsKey(header.getHash())) {
			return false;
		}
		
		if (checkPOW) {
			if (!header.verifyPOW()) {
				throw new BadBehaviourIOException("Header fails POW requirement", 100);
			}
		}
		
		HeaderInfo<T> parentInfo = map.get(header.getPreviousHash());
		if (parentInfo == null) {
			throw new BadBehaviourIOException("Header is not connected to the tree", 5);
		}
		
		BigInteger totalPOW = parentInfo.getTotalPOW().add(header.getDifficulty());
		int height = parentInfo.getHeight() + 1;

		HeaderInfo<T> headerInfo = new HeaderInfo<T>(header, parentInfo, height, totalPOW, fromDisk);
		
		if (!header.verifyConnect(headerInfo, height, chainParams)) {
			throw new BadBehaviourIOException("Header does not comply with connect rules", 100);
		}
		
		if (map.put(header.getHash(), headerInfo) != null) {
			throw new IllegalStateException("Header key did not exist at start of method");
		}
		
		if (totalPOW.compareTo(chainTip.getTotalPOW()) > 0) {
			while (mainChain.length <= headerInfo.getHeight()) {
				int requiredLength = Math.max(mainChain.length, headerInfo.getHeight() + 1);
				if (mainChain.length < requiredLength) {
					int newTarget = Math.max(mainChain.length, ((headerInfo.getHeight() * 3) / 2) + 1);
					@SuppressWarnings("rawtypes")
					HeaderInfo[] newMainChain = new HeaderInfo[newTarget];
					System.arraycopy(mainChain, 0, newMainChain, 0, mainChain.length);
					mainChain = newMainChain;
				}
			}
			HeaderInfo<T> temp = headerInfo;
			if (chainTip.getHeight() > temp.getHeight()) {
				for (int i = chainTip.getHeight(); i > temp.getHeight(); i--) {
					mainChain[i].setOnMainChain(false);
				}
			}
			while (!temp.isOnMainChain()) {
				@SuppressWarnings("unchecked")
				HeaderInfo<T> removedHeader = (HeaderInfo<T>) mainChain[temp.getHeight()];
				if (removedHeader != null) {
					removedHeader.setOnMainChain(false);
					mainChain[temp.getHeight()] = null;
				}
				
				mainChain[temp.getHeight()] = temp;
				
				if (saveQueue != null && (!temp.isSavedToDisk()) && saveQueueAlive.get()) {
					boolean interrupted = false;;
					while (true) {
						try {
							saveQueue.put(temp.getHeader());
							break;
						} catch (InterruptedException e) {
							interrupted = true;
						};
					}
					if (interrupted) {
						Thread.currentThread().interrupt();
					}
					temp.setSavedToDisk(true);
				}
				
				temp.setOnMainChain(true);
				temp = temp.getParentInfo();
			}
			chainTip = headerInfo;
		}
		
		return true;
		
	}
	
	public synchronized HeaderInfo<T> getChainTipInfo() {
		return chainTip;
	}
	
	public synchronized T getChainTip() {
		return chainTip.getHeader();
	}
	
	public synchronized BigInteger getChainTipPOW() {
		return chainTip.getTotalPOW();
	}
	
	private void readFromDisk(File directory) {
		long[] indexes = getIndexes(directory);
		Arrays.sort(indexes);
		for (long index : indexes) {
			String filename = getFilename(index);
			File file = new File(directory, filename);
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(file);
				EndianDataInputStream eis = new EndianDataInputStream(fis);
				boolean done = eis.readBoolean();
				while (!done) {
					@SuppressWarnings("unchecked")
					T header = (T) genesis.read(0, eis);
					try {
						add(header, true);
					} catch (BadBehaviourIOException e) {}
					done = eis.readBoolean();
				}
			} catch (EOFException e) {
			} catch (IOException e) {
			} finally {
				if (fis != null) {
					try {
						fis.close();
					} catch (IOException e) {}
				}
			}
		}
	}
	
	private String getFilename(long index) {
		return "hdr" + String.format("%08d", index) + ".dat";
	}
	
	private long[] getIndexes(File directory) {
		File[] files = directory.listFiles();
		if (files == null) {
			return null;
		}
		List<Long> indexes = new ArrayList<Long>();
		for (File file : files) {
			String filename = file.getName();
			if (filename.startsWith("hdr") && filename.endsWith(".dat") && filename.length() == 15) {
				String indexString = filename.substring(3, 11);
				try {
					indexes.add(Long.parseLong(indexString));	
				} catch (NumberFormatException e) {
					continue;
				}
			}
		}
		long[] array = new long[indexes.size()];
		int i = 0;
		for (Long index : indexes) {
			array[i++] = index;
		}
		return array;
	}
	
	private long getFirstFreeIndex(File directory) {
		long[] indexes = getIndexes(directory);
		long maxIndex = 0;
		for (long index : indexes) {
			if (index > maxIndex) {
				maxIndex = index;
			}
		}
		return maxIndex + 1;
	}
	
	private class SaveThread extends CatchingThread {
		
		private final File directory;
		
		public SaveThread(File directory) throws IOException {
			this.directory = directory;
			directory.mkdirs();
			if (!directory.isDirectory()) {
				throw new IOException("Failed to create directory");
			}
		}
		
		@Override
		public void secondaryRun() {
			long fileIndex = getFirstFreeIndex(directory);
			
			while (saveQueueAlive.get() || (!saveQueue.isEmpty())) {
				String filename = getFilename(fileIndex++);
				FileOutputStream fos = null;
				EndianDataOutputStream eos = null;
				try {
					File file = new File(directory, filename);
					fos = new FileOutputStream(file);
					eos = new EndianDataOutputStream(fos);
					
					for (int i = 0; i < 1024; i++) {
						Header<T> header;
						try {
							if (saveQueueAlive.get()) {
								header = saveQueue.take();
							} else {
								header = saveQueue.poll();
								if (header == null) {
									break;
								}
							}
						} catch (InterruptedException e) {
							continue;
						}
						eos.writeBoolean(false);
						header.write(0, eos);
					}
					eos.writeBoolean(true);
				} catch (IOException e) {
				} finally {
					try {
						if (eos != null) {
							try {
								eos.flush();
							} catch (IOException e) {}
						}
					} finally {
						if (fos != null) {
							try {
								fos.close();
							} catch (IOException e) {}
						}
					}
				}
			}
		}
	}
}
