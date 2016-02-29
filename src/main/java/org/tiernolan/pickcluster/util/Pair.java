package org.tiernolan.pickcluster.util;

public class Pair<F, S> {
	
	private final F first;
	private final S second;
	
	public Pair(F first, S second) {
		this.first = first;
		this.second = second;
	}
	
	public F getFirst() {
		return first;
	}
	
	public S getSecond() {
		return second;
	}
	
	@Override
	public int hashCode() {
		int hashFirst = first == null ? 0xAAAAAAAA : first.hashCode();
		int hashSecond = second == null ? 0x55555555 : second.hashCode();
		return hashFirst ^ (hashSecond << 16) ^ (hashSecond >>> 16);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (o instanceof Pair) {
			@SuppressWarnings("rawtypes")
			Pair other = (Pair) o;
			boolean matchFirst = this.first == null ? other.first == null : this.first.equals(other.first);
			boolean matchSecond = this.second == null ? other.second == null : this.second.equals(other.second);
			return matchFirst && matchSecond;
		}
		return false;
	}

}
