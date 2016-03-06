package org.tiernolan.pickcluster.net.peers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.tiernolan.pickcluster.net.P2PNode;
import org.tiernolan.pickcluster.net.chainparams.ChainParameters;
import org.tiernolan.pickcluster.types.NetAddr;
import org.tiernolan.pickcluster.types.NetType;
import org.tiernolan.pickcluster.types.SocketAddressType;
import org.tiernolan.pickcluster.types.endian.EndianDataInputStream;
import org.tiernolan.pickcluster.types.endian.EndianDataOutputStream;
import org.tiernolan.pickcluster.util.CatchingThread;
import org.tiernolan.pickcluster.util.FileUtils;
import org.tiernolan.pickcluster.util.StringCreator;
import org.tiernolan.pickcluster.util.ThreadUtils;
import org.tiernolan.pickcluster.util.TimeUtils;

public class AddrTracker {
	
	private final static String PREFIX = "prs";
	
	private final P2PNode node;
	private final ChainParameters params;
	private final FileWriteThread fileWriteThread;
	
	private final Map<InetAddress, Long> addrToTime = new HashMap<InetAddress, Long>();  
	private final TreeMap<Long, NetAddr> timeToAddr = new TreeMap<Long, NetAddr>(Collections.reverseOrder());
	private final Map<InetAddress, Long> banEndTimeMap = new HashMap<InetAddress, Long>();
	private final LinkedBlockingQueue<AddressFileData> fileDataQueue = new LinkedBlockingQueue<AddressFileData>();

	public AddrTracker(P2PNode node, ChainParameters params) {
		this.node = node;
		this.params = params;
		this.fileWriteThread = new FileWriteThread();
		readFromDisk();
		this.fileWriteThread.start();
	}
	
	public void interrupt(boolean wait) {
		this.interrupt();
		if (wait) {
			ThreadUtils.joinUninterruptibly(fileWriteThread);
		}
	}
	
	public void interrupt() {
		this.fileWriteThread.interrupt();
	}
	
	public synchronized NetAddr[] getAddresses() {
		NetAddr[] addresses = new NetAddr[timeToAddr.size()];
		int i = addresses.length - 1;
		for (NetAddr addr : timeToAddr.values()) {
			addresses[i--] = addr;
		}
		Arrays.sort(addresses, new Comparator<NetAddr>() {
			@Override
			public int compare(NetAddr o1, NetAddr o2) {
				return -Long.compare(o1.getTimestamp(), o2.getTimestamp());
			}
		});
		return addresses;
	}

	public synchronized boolean isBanned(InetAddress addr) {
		return banEndTimeMap.containsKey(addr);
	}
	
	public synchronized NetAddr getAddress(InetAddress[] connected) {
		return getAddress(connected, 500, TimeUtils.DAY_IN_SECONDS * 2);
	}
	
	public synchronized NetAddr getAddress(InetAddress[] connected, int minAddresses, long maxAge) {
		if (connected.length == 0) {
			return timeToAddr.isEmpty() ? null : timeToAddr.firstEntry().getValue();
		}
		SocketAddressType[] peers = new SocketAddressType[connected.length];
		for (int i = 0; i < peers.length; i++) {
			try {
				peers[i] = new SocketAddressType(connected[i], 0);
			} catch (UnknownHostException e) {}
		}
		
		int attempts = 0;
		long timestampThreshold = TimeUtils.getNowTimestamp() - maxAge;
		long bestDistance = 0;
		NetAddr bestAddr = null;
		for (NetAddr addr : timeToAddr.values()) {
			if (attempts > minAddresses && addr.getTimestamp() < timestampThreshold) {
				break;
			}
			attempts++;
			long minPeerDistance = Long.MAX_VALUE;
			long addressPrefix = addr.getAddressPrefix();
			for (SocketAddressType peer : peers) {
				if (peer == null) {
					continue;
				}
				long d = peer.getAddressPrefix() - addressPrefix;
				d = (d < 0) ? (~d) : d;
				if (d < minPeerDistance) {
					minPeerDistance = d;
				}
			}
			if (minPeerDistance > bestDistance) {
				bestDistance = minPeerDistance;
				bestAddr = addr;
			}
		}
		return bestAddr;
	}
	
	public synchronized void add(NetAddr addr, boolean directConnect) {
		add(addr, directConnect, false);
	}
	
	private synchronized void add(NetAddr addr, boolean directConnect, boolean useRaw) {
		long timestamp;
		if (useRaw) {
			timestamp = addr.getTimestamp();
		} else if (!directConnect) {
			timestamp = Math.min(addr.getTimestamp(), TimeUtils.getNowTimestamp() - TimeUtils.DAY_IN_SECONDS);
		} else {
			timestamp = TimeUtils.getNowTimestamp();
		}
		SocketAddressType socketAddr = addr.getAddr();
		Long oldTime = addrToTime.get(socketAddr.getAddress());
		if (oldTime == null || timestamp > oldTime + (6 * TimeUtils.HOUR_IN_SECONDS)) {
			updateMap(addr, oldTime, timestamp);
		}
	}
	
	public synchronized void ban(NetAddr addr) {
		ban(addr, TimeUtils.DAY_IN_SECONDS);
	}

	public synchronized void ban(NetAddr addr, long duration) {
		ban(addr.getAddr(), TimeUtils.getNowTimestamp() + duration);
	}
	
	private synchronized void ban(SocketAddressType socketAddr, long newBanTime) {
		Long banTime = banEndTimeMap.get(socketAddr.getAddress());
		if (banTime == null || newBanTime > banTime + (2 * TimeUtils.HOUR_IN_SECONDS)) {
			AddressFileData fileData = new AddressFileData(socketAddr, 0L, newBanTime, true);
			fileDataQueue.add(fileData);
		}
	}
	
	private boolean updateMap(NetAddr netAddr, Long oldTime, long newTime) {
		InetAddress addr = netAddr.getAddress();
		cleanMaps();
		if (oldTime != null) {
			if (!oldTime.equals(addrToTime.remove(addr))) {
				throw new IllegalStateException("Removed element does not equal expected time");
			}
			NetAddr removed = timeToAddr.remove(oldTime);
			if (removed == null || removed.getAddr() == null || !(removed.getAddr().equals(netAddr.getAddr()))) {
				throw new IllegalStateException("Removed element does not equal expected address");
			}
		} else {
			if (addrToTime.containsKey(addr)) {
				throw new IllegalStateException("Unexpected key in address to time map");
			}
		}
		Random r = null;
		while (timeToAddr.get(newTime) != null) {
			r = r == null ? new Random() : r;
			newTime -= r.nextInt(32) + 1;
		}
		
		NetAddr newTimeNetAddr = new NetAddr((int) newTime, netAddr.getServices(), netAddr.getAddr());
		if (addrToTime.put(addr, newTime) != null) {
			throw new IllegalStateException("Unexpected element overwrite");
		}
		if (timeToAddr.put(newTime, newTimeNetAddr) != null) {
			throw new IllegalStateException("Unexpected element overwrite");
		}
		AddressFileData fileData = new AddressFileData(netAddr.getAddr(), netAddr.getServices(), newTime, false);
		fileDataQueue.add(fileData);
		return true;
	}
	
	private void cleanMaps() {
		boolean removed = false;
		
		long weekOld = TimeUtils.getNowTimestamp() - TimeUtils.WEEK_IN_SECONDS;
		while (timeToAddr.firstEntry() != null && timeToAddr.firstEntry().getKey() < weekOld) {
			Long oldest = timeToAddr.firstEntry().getKey();
			NetAddr addr = timeToAddr.firstEntry().getValue();
			Entry<Long, NetAddr> entry = timeToAddr.pollFirstEntry();
			if (!entry.getKey().equals(oldest) || !entry.getValue().equals(addr)) {
				throw new IllegalStateException("Unexpected entry removed");
			}
			Long time = addrToTime.remove(addr.getAddress());
			if (!entry.getKey().equals(time)) {
				throw new IllegalStateException("Unexpected entry removed, " + entry + " " + time);
			}
			removed = true;
		}

		if (removed) {
			Iterator<Entry<InetAddress, Long>> i = banEndTimeMap.entrySet().iterator();
			while (i.hasNext()) {
				Entry<InetAddress, Long> entry = i.next();
				if (entry.getValue() < weekOld) {
					i.remove();
				}
			}
		}
	}
	
	private void readFromDisk() {
		File directory = FileUtils.getDataDirectory(params, "peers");
		if (!directory.isDirectory()) {
			return;
		}
		long nowDay = getNowDayIndex();
		long[] indexes = FileUtils.getIndexes(directory, PREFIX);
		for (long index : indexes) {
			if (index >= nowDay - TimeUtils.WEEK_IN_SECONDS) {
				FileInputStream fis = null;
				try {
					File f = new File(directory, FileUtils.getFilename(index, PREFIX));
					fis = new FileInputStream(f);
					EndianDataInputStream eis = new EndianDataInputStream(fis);
					while (true) {
						AddressFileData data = new AddressFileData(eis);
						if (data.isBan()) {
							ban(data.getAddr(), data.getLastSeen());
						} else {
							NetAddr netAddr = new NetAddr((int) data.getLastSeen(), data.getServices(), data.getAddr());
							add(netAddr, false, true);
						}
					}
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
	}
	
	private class AddressFileData implements NetType {
		private final boolean ban;
		private final long lastSeen;
		private final SocketAddressType address;
		private final long services;
		
		public AddressFileData(SocketAddressType address, long services, long lastSeen, boolean ban) {
			this.address = address;
			this.lastSeen = lastSeen;
			this.services = services;
			this.ban = ban;
		}
		
		public AddressFileData(EndianDataInputStream in) throws IOException {
			this.address = new SocketAddressType(in);
			this.services = in.readLELong();
			this.lastSeen = in.readLELong();
			this.ban = in.readBoolean();
		}

		public long getLastSeen() {
			return lastSeen;
		}
		
		public boolean isBan() {
			return ban;
		}
		
		public SocketAddressType getAddr() {
			return address;
		}
		
		public long getServices() {
			return services;
		}

		public NetType read(EndianDataInputStream in, Object... extraParams) throws IOException {
			return new AddressFileData(in);
		}
		
		@Override
		public NetType read(int version, EndianDataInputStream in, Object... extraParams) throws IOException {
			return read(in);
		}

		@Override
		public void write(int version, EndianDataOutputStream out) throws IOException {
			write(out);
		}
		
		public void write(EndianDataOutputStream out) throws IOException {
			address.write(out);
			out.writeLELong(services);
			out.writeLELong(lastSeen);
			out.writeBoolean(ban);
		}

		@Override
		public int estimateSize() {
			return address.estimateSize() + 9;
		}
		
		public String toString() {
			StringCreator sc = new StringCreator()
						.add("address", address)
						.add("services", Long.toHexString(services))
						.add("last_seen", lastSeen)
						.add("ban", ban);
			return sc.toString();
		}
		
	}
	
	private long getNowDayIndex() {
		return TimeUtils.getNowTimestamp() / TimeUtils.DAY_IN_SECONDS;
	}
	
	private EndianDataOutputStream getEndianDataOutputStream(File directory, long index) throws IOException {
		long dayIndex = getNowDayIndex() - 7;
		if (index < dayIndex) {
			return null;
		}
		File f = new File(directory, FileUtils.getFilename(index, PREFIX));
		try {
			FileOutputStream fos = new FileOutputStream(f, true);
			return new EndianDataOutputStream(fos);
		} catch (FileNotFoundException e) {
			throw new IOException("Unable to create file to store addresses", e);
		}
	}
	
	private void closeOldFiles(Map<Long, EndianDataOutputStream> eosMap, File directory) {
		long dayIndex = getNowDayIndex() - 7;
		Iterator<Entry<Long, EndianDataOutputStream>> i = eosMap.entrySet().iterator();
		while (i.hasNext()) {
			Entry<Long, EndianDataOutputStream> entry = i.next();
			if (entry.getKey() < dayIndex) {
				Long index = entry.getKey();
				EndianDataOutputStream eos = entry.getValue();
				i.remove();
				try {
					eos.close();
				} catch (IOException e) {}
				File f = new File(directory, FileUtils.getFilename(index, PREFIX));
				f.delete();
			}
		}
	}
	
	private class FileWriteThread extends CatchingThread {
		@Override
		protected void secondaryRun() throws IOException {
			File directory = FileUtils.getDataDirectory(params, "peers");
			directory.mkdirs();
			if (!directory.isDirectory()) {
				throw new IOException("Failed to create directory");
			}
			long nowDay = getNowDayIndex();
			long[] indexes = FileUtils.getIndexes(directory, PREFIX);
			for (long index : indexes) {
				if (index < nowDay - 7) {
					File f = new File(directory, FileUtils.getFilename(index, PREFIX));
					f.delete();
				}
			}
			indexes = FileUtils.getIndexes(directory, PREFIX);

			Map<Long, EndianDataOutputStream> eosMap = new HashMap<Long, EndianDataOutputStream>();
			EndianDataOutputStream eos = null;
			try {
				boolean shutdownInProgress = false;
				while (true) {
					closeOldFiles(eosMap, directory);
					AddressFileData data;
					try {
						if (shutdownInProgress) {
							data = fileDataQueue.poll();
							if (data == null) {
								break;
							}
						} else {
							data = fileDataQueue.poll(TimeUtils.HOUR_IN_SECONDS, TimeUnit.SECONDS);
						}
					} catch (InterruptedException e) {
						shutdownInProgress = true;
						continue;
					}
					if (data == null) {
						continue;
					}
					long dayIndex = data.getLastSeen() / TimeUtils.DAY_IN_SECONDS;
					eos = eosMap.get(dayIndex);
					if (eos == null) {
						eos = getEndianDataOutputStream(directory, dayIndex);
						if (eos == null) { 
							continue;
						}
						eosMap.put(dayIndex, eos);
					}
					data.write(eos);
				}
			} finally {
				if (eos != null) {
					try {
						eos.close();
					} catch (IOException e) {
					} catch (Throwable t) {
						t.printStackTrace();
					}
				}
				for (EndianDataOutputStream value : eosMap.values()) {
					try {
						if (value != null) {
							value.close();
						}
					} catch (IOException e) {
					} catch (Throwable t) {
						t.printStackTrace();
					}
				}
			}
		}
	}

}
