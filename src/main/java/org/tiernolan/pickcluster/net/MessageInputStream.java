package org.tiernolan.pickcluster.net;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import org.tiernolan.pickcluster.crypt.Digest;
import org.tiernolan.pickcluster.net.chainparams.BadBehaviourIOException;
import org.tiernolan.pickcluster.net.chainparams.ChainParameters;
import org.tiernolan.pickcluster.net.message.Message;
import org.tiernolan.pickcluster.net.message.MessageConstructor;
import org.tiernolan.pickcluster.net.message.MessageMap;
import org.tiernolan.pickcluster.net.message.common.UnknownMessage;
import org.tiernolan.pickcluster.types.UInt256;
import org.tiernolan.pickcluster.types.UInt96;
import org.tiernolan.pickcluster.types.encode.Convert;
import org.tiernolan.pickcluster.types.endian.Endian;
import org.tiernolan.pickcluster.types.endian.EndianDataInputStream;

public class MessageInputStream extends EndianDataInputStream {
	
	private final int bigEndianPrefix;
	private final int maxLength;
	
	private final MessageMap messageMap;
	
	private boolean hasHeader = false;
	
	private long totalRead = 0;
	
	private byte[] data = new byte[0];
	
	private UInt96 command;
	private int messageLength;
	private int checksum;
	
	private int version = 0;
	
	public MessageInputStream(InputStream in, ChainParameters params) {
		super(in);
		this.bigEndianPrefix = params.getBigEndianMessagePrefix();
		this.maxLength = params.getMaxMessageLength();
		this.messageMap = params.getMessageProtocol().getMessageMap();
	}
	
	private void readHeader() throws IOException {
		if (!hasHeader) {
			int prefix = readBEInt();
			totalRead += 4;
			while (prefix != bigEndianPrefix) {
				int b = read();
				if (b == -1) {
					throw new EOFException("End of stream reached during seek for message prefix");
				}
				totalRead += 1;
				prefix = (prefix << 8) | (b & 0xFF);
			}
			command = new UInt96(this);
			messageLength = readLEInt();
			checksum = readBEInt();
			totalRead += 20;
			hasHeader = true;
		}
	}
	
	public <T extends Message> T getMessage() throws IOException {
		if (!hasHeader) {
			readHeader();
		}
		
		hasHeader = false;
		
		if (messageLength < 0) {
			throw new BadBehaviourIOException("Negative data lengths are not disallowed", 100);
		} else if (messageLength > maxLength) {
			throw new BadBehaviourIOException("Mesage sizes above " + ((maxLength >> 10) / 1024.0) + "MB are disallowed", 100);
		}

		if (totalRead < messageLength && messageLength > 1024) {
			byte[] temp = new byte[1024];
			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			int read = 0;
			while (read < messageLength) {
				int count = read(temp, 0, Math.min(1024, messageLength - read));
				if (count < 0) {
					throw new EOFException();
				}
				baos.write(temp, 0, count);
				read += count;
			}
			data = baos.toByteArray();
		} else {
			data = new byte[messageLength];

			int read = 0;

			while (read < messageLength) {
				read += read(data, read, data.length - read);
			}
		}

		totalRead += messageLength;
		UInt256 digest = Digest.doubleSHA256(data);
		int messageChecksum = Endian.swap(digest.toBigInteger().intValue());
		if (messageChecksum != checksum) {
			throw new BadBehaviourIOException("Checksum mismatch, " + Integer.toHexString(checksum) + " expected, got " + Integer.toHexString(messageChecksum), 10);
		}
		MessageConstructor<T> constructor = messageMap.getConstructor(command);
		if (constructor == null) {
			@SuppressWarnings("unchecked")
			T message = (T) new UnknownMessage(command, data);
			return message;
		} else {
			EndianDataInputStream in = new EndianDataInputStream(new ByteArrayInputStream(data));
			return constructor.getMessage(version, in);
		}
	}
	
	public void setVersion(int version) {
		this.version = version;
	}

	public int getVersion() {
		return version;
	}
	
	public long getTotalRead() {
		return totalRead;
	}

}
