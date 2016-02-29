package org.tiernolan.pickcluster.net.blockchain;

import java.math.BigInteger;

import org.tiernolan.pickcluster.types.reference.Header;
import org.tiernolan.pickcluster.util.TimeUtils;

public class HeaderInfo<T extends Header<T>> {
	
	private final BigInteger totalPOW;
	private final T header;
	private final HeaderInfo<T> parent;
	private final HeaderInfo<T> skipTarget;
	private final int height;
	private final long timeCreated;
	private boolean onMainChain;
	private boolean savedToDisk;
	
	public HeaderInfo(T header, HeaderInfo<T> parent, int height, BigInteger totalPOW, boolean savedToDisk) {
		this.header = header;
		this.parent = parent;
		this.height = height;
		this.totalPOW = totalPOW;
		this.onMainChain = false;
		this.timeCreated = TimeUtils.getCurrentTimeMillis();
		this.savedToDisk = savedToDisk;
		this.skipTarget = parent == null ? null : parent.getAncestorInfo(getSkipTarget(height));
	}
	
	public T getHeader() {
		return header;
	}
	
	public HeaderInfo<T> getParentInfo() {
		return parent;
	}
	
	public HeaderInfo<T> getAncestorInfo(int height) {
		if (height > getHeight()) {
			throw new IllegalArgumentException("Height is to large to be an ancestor height");
		}
		if (height < 0) {
			return null;
		}
		HeaderInfo<T> temp = this;
		while (temp.getHeight() != height) {
			if (temp.skipTarget.getHeight() >= height) {
				temp = temp.skipTarget;
			} else {
				temp = temp.getParentInfo();
			}
		}
		return temp;
	}
	
	public int getHeight() {
		return height;
	}
	
	public BigInteger getTotalPOW() {
		return totalPOW;
	}
	
	public long getTimeCreated() {
		return timeCreated;
	}
	
	public void setOnMainChain(boolean onMainChain) {
		this.onMainChain = onMainChain;
	}
	
	public boolean isOnMainChain() {
		return this.onMainChain;
	}
	
	public void setSavedToDisk(boolean savedToDisk) {
		this.savedToDisk = savedToDisk;
	}
	
	public boolean isSavedToDisk() {
		return savedToDisk;
	}

	public static int getSkipTarget(int height) {
		if (height < 2)
			return 0;

		int index = height & 0x1F;

		if (index > 25)
			index = 25;

		int step = (1 << (index + 5)) + 1;

		int skip_height = height - step;

		if (skip_height < 2) {
			return (height > 33) ? (height - 33) : (height >> 1);
		} else {
			return skip_height;
		}
	}
}
