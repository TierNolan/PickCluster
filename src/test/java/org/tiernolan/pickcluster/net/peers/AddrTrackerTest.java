package org.tiernolan.pickcluster.net.peers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.tiernolan.pickcluster.net.chainparams.ChainParameters;
import org.tiernolan.pickcluster.net.chainparams.bitcoin.BitcoinChainParams;
import org.tiernolan.pickcluster.net.message.MessageProtocol;
import org.tiernolan.pickcluster.types.NetAddr;
import org.tiernolan.pickcluster.types.SocketAddressType;
import org.tiernolan.pickcluster.types.reference.Header;
import org.tiernolan.pickcluster.util.FileUtils;
import org.tiernolan.pickcluster.util.TimeUtils;

public class AddrTrackerTest {

	private final static ChainParameters params = new ChainParameters() {

		@Override
		public String getNetworkName() {
			return "unit_test_network";
		}

		@Override
		public long getSubsidy(int height) {
			return 0;
		}

		@Override
		public int getMaxMessageLength() {
			return 0;
		}

		@Override
		public int getBigEndianMessagePrefix() {
			return 0;
		}

		@Override
		public int getDefaultPort() {
			return 0;
		}

		@Override
		public <T extends Header<T>> T getGenesis() {
			return null;
		}

		@Override
		public MessageProtocol getMessageProtocol() {
			return null;
		}

		@Override
		public List<SocketAddressType> getFixedSeeds() {
			return null;
		}
	};

	@Test
	public void test() throws IOException {

		AddrTracker addrTracker = null;

		try {
			long BASE_TIME = 2000000;

			TimeUtils.forceTimeSeconds(BASE_TIME);

			addrTracker = new AddrTracker(null, params);

			SocketAddressType[] addresses = new SocketAddressType[250];
			for (int i = 0; i < addresses.length; i++) {
				addresses[i] = new SocketAddressType(InetAddress.getByName("192.168.1." + (i + 1)), 1234 + i);
			}

			NetAddr[] netAddresses = new NetAddr[addresses.length];
			for (int i = 0; i < netAddresses.length; i++) {
				netAddresses[i] = new NetAddr((int) TimeUtils.getNowTimestamp() - (i * 3600), 0L, addresses[i]);
			}

			for (int i = 0; i < 250; i++) {
				addrTracker.add(netAddresses[i], (i % 5) == 0);
				if ((i % 7) == 0) {
					addrTracker.ban(netAddresses[i]);
				}
			}

			NetAddr[] oldStored = addrTracker.getAddresses();

			addrTracker.interrupt(true);

			addrTracker = new AddrTracker(null, params);

			NetAddr[] stored = addrTracker.getAddresses();

			addrTracker.interrupt(true);

			for (int i = 0; i < stored.length; i++) {
				assertEquals(oldStored[i], stored[i]);
			}
		} finally {
			if (addrTracker != null) {
				addrTracker.interrupt(true);
			}
			File directory = FileUtils.getDataDirectory(params, "peers");
			for (File f : directory.listFiles()) {
				f.delete();
			}
		}
	}
	
	@Test
	public void testSeparation() throws IOException {

		AddrTracker addrTracker = null;

		try {
			long BASE_TIME = 2000000;

			TimeUtils.forceTimeSeconds(BASE_TIME);

			addrTracker = new AddrTracker(null, params);

			SocketAddressType[] addresses = new SocketAddressType[1024];
			for (int i = 0; i < addresses.length; i += 8) {
				for (int j = 0; j < 8; j++) {
					addresses[i + j] = new SocketAddressType(InetAddress.getByName(((16 * j) + 1)  + ".168.1." + ((i >> 3) + 1)), 1234 + i);
				}
			}

			NetAddr[] netAddresses = new NetAddr[addresses.length];
			for (int i = 0; i < netAddresses.length; i++) {
				netAddresses[i] = new NetAddr((int) TimeUtils.getNowTimestamp() - (i * 60), 0L, addresses[i]);
			}

			for (int i = 0; i < netAddresses.length; i++) {
				addrTracker.add(netAddresses[i], true);
			}

			List<InetAddress> connected = new ArrayList<InetAddress>(8);
			for (int i = 0; i < 8; i++) {
				NetAddr addr = addrTracker.getAddress(connected.toArray(new InetAddress[0]));
				InetAddress inet = addr.getAddress();
				byte[] newAddr = inet.getAddress();
				for (InetAddress peer : connected) {
					byte[] peerAddr = peer.getAddress();
					assertNotSame(newAddr, peerAddr);
				}
				connected.add(inet);
			}
			
		} finally {
			if (addrTracker != null) {
				addrTracker.interrupt(true);
			}
			File directory = FileUtils.getDataDirectory(params, "peers");
			for (File f : directory.listFiles()) {
				f.delete();
			}
		}
	}
	
	@Test
	public void testFixed() {
		AddrTracker addrTracker = new AddrTracker(null, BitcoinChainParams.BITCOIN_MAIN, false);

		List<InetAddress> connected = new ArrayList<InetAddress>(8);
		for (int i = 0; i < 32; i++) {
			NetAddr addr = addrTracker.getAddress(connected.toArray(new InetAddress[0]));
			InetAddress inet = addr.getAddress();
			byte[] newAddr = inet.getAddress();
			for (InetAddress peer : connected) {
				byte[] peerAddr = peer.getAddress();
				assertNotSame(newAddr, peerAddr);
			}
			connected.add(inet);
		}

	}
}
