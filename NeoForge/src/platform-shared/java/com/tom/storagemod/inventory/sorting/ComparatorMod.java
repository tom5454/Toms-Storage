package com.tom.storagemod.inventory.sorting;

import java.util.Comparator;

import com.tom.storagemod.inventory.StoredItemStack;

public class ComparatorMod implements Comparator<StoredItemStack> {
	@Override
	public int compare(StoredItemStack in1, StoredItemStack in2) {
		int c = in1.getNamespace().compareTo(in2.getNamespace());
		if (c != 0) return c;
		
		return in1.getDisplayName().compareTo(in2.getDisplayName());
	}
}