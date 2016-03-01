package org.tiernolan.pickcluster.net.chainparams.bitcoin.message;

import java.io.IOException;

import org.tiernolan.pickcluster.net.chainparams.bitcoin.types.BitcoinHeader;
import org.tiernolan.pickcluster.net.message.Message;
import org.tiernolan.pickcluster.types.NetTypeArray;
import org.tiernolan.pickcluster.types.endian.EndianDataInputStream;
import org.tiernolan.pickcluster.types.endian.EndianDataOutputStream;
import org.tiernolan.pickcluster.util.StringCreator;

public class BitcoinHeaders extends Message {
	
	public final static int MAX_HEADERS_LENGTH = 2000;
	
	private final NetTypeArray<BitcoinHeader> headers;
	
	public BitcoinHeaders(BitcoinHeader[] headers) {
		super("headers");
		this.headers = new NetTypeArray<BitcoinHeader>(headers, BitcoinHeader.class);
	}
	
	public BitcoinHeaders(int version, EndianDataInputStream in) throws IOException {
		super("headers");
		this.headers = new NetTypeArray<BitcoinHeader>(version, in, MAX_HEADERS_LENGTH, BitcoinHeader.class, BitcoinHeader.EXAMPLE);
	}

	@Override
	public void write(int version, EndianDataOutputStream out) throws IOException {
		this.headers.write(version, out);
	}
	
	@Override
	public BitcoinHeaders read(int version, EndianDataInputStream in, Object ... extraParams) throws IOException {
		return new BitcoinHeaders(version, in);
	}
	
	public int length() {
		return headers.length();
	}
	
	public BitcoinHeader getHeader(int index) {
		return headers.get(index);
	}

	@Override
	public String getDataString() {
		StringCreator sc = new StringCreator();
		if (headers.length() < 10) {
			for (int i = 0; i < headers.length(); i++) {
				sc.add("header[" + i + "]", headers.get(i));
			}
		} else {
			sc.add("header_count", headers.length());
		}
		return sc.toString();
		
	}

	@Override
	public int estimateDataSize() {
		return 9 + 89 * headers.length();
	}

}
