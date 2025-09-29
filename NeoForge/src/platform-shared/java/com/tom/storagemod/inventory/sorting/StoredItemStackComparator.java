package com.tom.storagemod.inventory.sorting;

import java.util.Comparator;

import com.tom.storagemod.inventory.StoredItemStack;

public abstract class StoredItemStackComparator implements Comparator<StoredItemStack> {
	public boolean reversed;

	public StoredItemStackComparator(boolean reversed) {
		this.reversed = reversed;
	}

	public boolean isReversed() {
		return reversed;
	}

	public void setReversed(boolean rev) {
		reversed = rev;
	}

	public abstract SortingTypes type();
}