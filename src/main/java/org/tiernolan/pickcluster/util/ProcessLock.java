package org.tiernolan.pickcluster.util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;

public class ProcessLock {
	
	private final File file;
	private FileLock fileLock;
	
	public ProcessLock(File file) {
		this.file = file;
	}
	
	public synchronized boolean lock() {
		try {
			fileLock = new RandomAccessFile(file, "rw").getChannel().tryLock();
			return fileLock != null;
		} catch (IOException e) {
			return false;
		}
	}
}
