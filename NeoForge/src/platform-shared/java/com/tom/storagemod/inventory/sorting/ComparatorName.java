package com.tom.storagemod.inventory.sorting;

import java.util.Comparator;

import com.tom.storagemod.inventory.StoredItemStack;

public class ComparatorName implements Comparator<StoredItemStack> {
	@Override
	public int compare(StoredItemStack in1, StoredItemStack in2) {
		return in1.getDisplayName().compareTo(in2.getDisplayName());
	}
}