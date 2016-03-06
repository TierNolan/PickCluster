package org.tiernolan.pickcluster.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.tiernolan.pickcluster.net.chainparams.ChainParameters;

public class FileUtils {
	
	public static long[] getIndexes(File directory, String prefix) {
		File[] files = directory.listFiles();
		if (files == null) {
			return null;
		}
		List<Long> indexes = new ArrayList<Long>();
		for (File file : files) {
			String filename = file.getName();
			if (filename.startsWith(prefix) && filename.endsWith(".dat") && filename.length() == prefix.length() + 12) {
				String indexString = filename.substring(3, 11);
				try {
					indexes.add(Long.parseLong(indexString));	
				} catch (NumberFormatException e) {
					continue;
				}
			}
		}
		long[] array = new long[indexes.size()];
		int i = 0;
		for (Long index : indexes) {
			array[i++] = index;
		}
		Arrays.sort(array);
		return array;
	}
	
	public static long getFirstFreeIndex(File directory, String prefix) {
		long[] indexes = getIndexes(directory, prefix);
		long maxIndex = 0;
		for (long index : indexes) {
			if (index > maxIndex) {
				maxIndex = index;
			}
		}
		return maxIndex + 1;
	}
	
	public static String getFilename(long index, String prefix) {
		return prefix + String.format("%08d", index) + ".dat";
	}
	
	public static File getDataDirectory(ChainParameters params, String type) {
		File networkDir = new File("data", params.getNetworkName());
		File targetDir = new File(networkDir, type);
		return targetDir;

	}

}
