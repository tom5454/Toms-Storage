package com.tom.storagemod.inventory.sorting;

import java.util.Comparator;

import com.tom.storagemod.inventory.StoredItemStack;

public class ComparatorAmount implements Comparator<StoredItemStack> {
	@Override
	public int compare(StoredItemStack in1, StoredItemStack in2) {
		int c = Long.compare(in1.getQuantity(), in2.getQuantity());
		if (c != 0) return c;

		return in1.getDisplayName().compareTo(in2.getDisplayName());
	}
}