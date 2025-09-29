package com.tom.storagemod.inventory.sorting;

import com.tom.storagemod.inventory.StoredItemStack;

public class ComparatorName implements IStoredItemStackComparator {
	public boolean reversed;

	public ComparatorName(boolean reversed) {
		this.reversed = reversed;
	}

	@Override
	public int compare(StoredItemStack in1, StoredItemStack in2) {
		int c = in1.getDisplayName().compareTo(in2.getDisplayName());
		return this.reversed ? -c : c;
	}

	@Override
	public boolean isReversed() {
		return reversed;
	}

	@Override
	public SortingTypes type() {
		return SortingTypes.NAME;
	}

	@Override
	public void setReversed(boolean rev) {
		reversed = rev;
	}
}