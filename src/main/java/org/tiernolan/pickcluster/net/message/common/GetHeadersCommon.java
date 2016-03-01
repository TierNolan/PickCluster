package org.tiernolan.pickcluster.net.message.common;

import java.io.IOException;

import org.tiernolan.pickcluster.net.message.Message;
import org.tiernolan.pickcluster.net.message.reference.HeadersMessage;
import org.tiernolan.pickcluster.types.NetTypeArray;
import org.tiernolan.pickcluster.types.UInt256;
import org.tiernolan.pickcluster.types.endian.EndianDataInputStream;
import org.tiernolan.pickcluster.types.endian.EndianDataOutputStream;
import org.tiernolan.pickcluster.util.StringCreator;

public class GetHeadersCommon extends Message {
	
	private final int version;
	private final NetTypeArray<UInt256> locators;
	private final UInt256 stop;
	
	public GetHeadersCommon(int version, UInt256[] locators, UInt256 stop) {
		super("getheaders");
		this.version = version;
		this.locators = new NetTypeArray<UInt256>(locators, UInt256.class);
		this.stop = stop;
	}
	
	public GetHeadersCommon(int version, EndianDataInputStream in) throws IOException {
		super("version");
		this.version = in.readLEInt();
		this.locators = new NetTypeArray<UInt256>(version, in, HeadersMessage.MAX_HEADERS_LENGTH, UInt256.class, UInt256.EXAMPLE);
		this.stop = new UInt256(in);
	}

	@Override
	public void write(int version, EndianDataOutputStream out) throws IOException {
		out.writeLEInt(version);
		this.locators.write(version, out);
		this.stop.write(out);
	}
	
	@Override
	public GetHeadersCommon read(int version, EndianDataInputStream in, Object ... extraParams) throws IOException {
		return new GetHeadersCommon(version, in);
	}
	
	public int length() {
		return locators.length();
	}
	
	public UInt256 getHeader(int index) {
		return locators.get(index);
	}

	@Override
	public String getDataString() {
		StringCreator sc = new StringCreator();
		sc.add("version", version);
		if (locators.length() < 10) {
			for (int i = 0; i < locators.length(); i++) {
				sc.add("locator[" + i + "]", locators.get(i));
			}
		} else {
			sc.add("locator_count", locators.length());
		}
		sc.add("stop", stop);
		return sc.toString();
		
	}

	@Override
	public int estimateDataSize() {
		return 45 + 32 * locators.length();
	}

}
