package org.tiernolan.pickcluster.types;

import java.io.IOException;
import java.lang.reflect.Array;

import org.tiernolan.pickcluster.types.endian.EndianDataInputStream;
import org.tiernolan.pickcluster.types.endian.EndianDataOutputStream;

public class NetTypeArray<T extends NetType> implements NetType {
	
	private final VarInt length;
	private final Object[] array;
	private final Class<T> clazz;
	private final int sizeEstimate;
	
	public NetTypeArray(T[] array, Class<T> clazz) {
		this.clazz = clazz;
		this.array = array;
		this.length = new VarInt(array.length);
		System.arraycopy(array, 0, this.array, 0, array.length);
		int estimate = 0;
		for (int i = 0; i < array.length; i++) {
			estimate += array[i].estimateSize();
		}
		this.sizeEstimate = estimate;
	}
	
	public NetTypeArray(int version, EndianDataInputStream in, int maxLength, Class<T> clazz, T example, Object ... extraParams) throws IOException {
		this.clazz = clazz;
		this.length = new VarInt(in);
		long length = this.length.getValue();
		if (length < 0 || length > maxLength) {
			throw new IOException("Length exceeds maximum value, " + length);
		}
		int estimate = 0;
		this.array = new Object[(int) length];
		for (int i = 0; i < length; i++) {
			NetType element = example.read(version, in, extraParams);
			estimate += element.estimateSize();
			array[i] = element;
		}
		this.sizeEstimate = estimate;
	}
	
	public int length() {
		return array.length;
	}
	
	@Override
	public void write(int version, EndianDataOutputStream out) throws IOException {
		length.write(out);
		for (Object element : array) {
			((NetType) element).write(version, out);
		}
	}
	
	@Override
	public int estimateSize() {
		return sizeEstimate;
	}
	
	@SuppressWarnings("unchecked")
	public T get(int index) {
		return (T) array[index];
	}
	
	public T[] getValue() {
		@SuppressWarnings("unchecked")
		T[] newArray = (T[]) Array.newInstance(clazz, array.length);
		System.arraycopy(this.array, 0, newArray, 0, this.array.length);
		return newArray;
	}

	@SuppressWarnings("unchecked")
	@Override
	public NetTypeArray<T> read(int version, EndianDataInputStream in, Object... extraParams) throws IOException {
		return new NetTypeArray<T>(version, in, (Integer) extraParams[0], (Class<T>) extraParams[1], (T) extraParams[2], (Object[]) extraParams[3]);
	}


}
