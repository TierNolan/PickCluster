package org.tiernolan.pickcluster.net.message.common;

import java.io.IOException;

import org.tiernolan.pickcluster.net.chainparams.bitcoin.types.BitcoinHeader;
import org.tiernolan.pickcluster.net.message.Message;
import org.tiernolan.pickcluster.types.InventoryType;
import org.tiernolan.pickcluster.types.NetTypeArray;
import org.tiernolan.pickcluster.types.endian.EndianDataInputStream;
import org.tiernolan.pickcluster.types.endian.EndianDataOutputStream;
import org.tiernolan.pickcluster.util.StringCreator;

public class InvCommon extends Message {
	
	public final static int MAX_HEADERS_LENGTH = 2000;
	
	private final NetTypeArray<InventoryType> invs;
	
	public InvCommon(InventoryType[] invs) {
		super("inv");
		this.invs = new NetTypeArray<InventoryType>(invs, InventoryType.class);
	}
	
	public InvCommon(int version, EndianDataInputStream in) throws IOException {
		super("inv");
		this.invs = new NetTypeArray<InventoryType>(version, in, MAX_HEADERS_LENGTH, InventoryType.class, InventoryType.EXAMPLE);
	}

	@Override
	public void write(int version, EndianDataOutputStream out) throws IOException {
		this.invs.write(version, out);
	}
	
	@Override
	public InvCommon read(int version, EndianDataInputStream in, Object ... extraParams) throws IOException {
		return new InvCommon(version, in);
	}
	
	public int length() {
		return invs.length();
	}
	
	public InventoryType getInventory(int index) {
		return invs.get(index);
	}

	@Override
	public String getDataString() {
		StringCreator sc = new StringCreator();
		if (invs.length() < 10) {
			for (int i = 0; i < invs.length(); i++) {
				sc.add("invs[" + i + "]", invs.get(i));
			}
		} else {
			sc.add("inv_count", invs.length());
		}
		return sc.toString();
		
	}

	@Override
	public int estimateDataSize() {
		return 9 + 36 * invs.length();
	}

}
