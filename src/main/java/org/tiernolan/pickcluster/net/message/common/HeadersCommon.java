package org.tiernolan.pickcluster.net.message.common;

import java.io.IOException;

import org.tiernolan.pickcluster.net.message.Message;
import org.tiernolan.pickcluster.types.NetTypeArray;
import org.tiernolan.pickcluster.types.endian.EndianDataInputStream;
import org.tiernolan.pickcluster.types.endian.EndianDataOutputStream;
import org.tiernolan.pickcluster.types.reference.Header;
import org.tiernolan.pickcluster.util.StringCreator;

public class HeadersCommon<T extends Header<T>> extends Message {
	
	public final static int MAX_HEADERS_LENGTH = 2000;
	
	private final NetTypeArray<T> headers;
	
	public HeadersCommon(T[] headers) {
		super("headers");
		@SuppressWarnings("unchecked")
		Class<T> clazz = (Class<T>) headers.getClass().getComponentType();
		this.headers = new NetTypeArray<T>(headers, clazz);
	}
	
	@SuppressWarnings("unchecked")
	public HeadersCommon(int version, EndianDataInputStream in, Header<T> example) throws IOException {
		super("headers");
		this.headers = new NetTypeArray<T>(version, in, MAX_HEADERS_LENGTH, (Class<T>) example.getClass(), (T) example);
	}

	@Override
	public void write(int version, EndianDataOutputStream out) throws IOException {
		this.headers.write(version, out);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public HeadersCommon<T> read(int version, EndianDataInputStream in, Object ... extraParams) throws IOException {
		return new HeadersCommon<T>(version, in, (T) extraParams[0]);
	}
	
	public int length() {
		return headers.length();
	}
	
	public T getHeader(int index) {
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
