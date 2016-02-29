package org.tiernolan.pickcluster.net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.tiernolan.pickcluster.crypt.Digest;
import org.tiernolan.pickcluster.net.chainparams.ChainParameters;
import org.tiernolan.pickcluster.net.message.Message;
import org.tiernolan.pickcluster.types.UInt256;
import org.tiernolan.pickcluster.types.endian.Endian;
import org.tiernolan.pickcluster.types.endian.EndianDataOutputStream;

public class MessageOutputStream extends EndianDataOutputStream {

	private final int bigEndianPrefix;
	
	private int version;
	private long totalSent = 0;
	private final int maxMessageLength;
	
	public MessageOutputStream(OutputStream out, ChainParameters params) {
		super(out);
		this.bigEndianPrefix = params.getBigEndianMessagePrefix();
		this.maxMessageLength = params.getMaxMessageLength();
	}
	

	public void writeMessage(Message message) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(message.estimateDataSize());
		EndianDataOutputStream eos = new EndianDataOutputStream(baos);
		message.write(version, eos);
		eos.close();
		
		byte[] data = baos.toByteArray();
		UInt256 digest = Digest.doubleSHA256(data);
		int checksum = Endian.swap(digest.toBigInteger().intValue());
		
		if (data.length + 24 > maxMessageLength) {
			System.out.println("Warning: outgoing message exceeds maximum message length");
		}
		// Header
		this.writeBEInt(this.bigEndianPrefix);
		message.getCommand().write(this);
		writeLEInt(data.length);
		writeBEInt(checksum);
		totalSent += 24;
		// Payload
		write(data);
		totalSent += data.length;
	}
	
	public long getTotalSent() {
		return totalSent;
	}
	
	public void setVersion(int version) {
		this.version = version;
	}

	public int getVersion() {
		return version;
	}

}
