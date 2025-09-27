package com.tom.storagemod.inventory.sorting;

import java.util.Comparator;

import com.tom.storagemod.inventory.StoredItemStack;

public interface IStoredItemStackComparator extends Comparator<StoredItemStack> {
	boolean isReversed();
	void setReversed(boolean rev);
	SortingTypes type();
}