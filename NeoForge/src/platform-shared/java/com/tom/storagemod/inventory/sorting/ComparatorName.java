package com.tom.storagemod.inventory.sorting;

import com.tom.storagemod.inventory.StoredItemStack;

public class ComparatorName extends StoredItemStackComparator {
	public ComparatorName(boolean reversed) {
		super(reversed);
	}

	@Override
	public int compare(StoredItemStack in1, StoredItemStack in2) {
		int c = in1.getDisplayName().compareTo(in2.getDisplayName());
		return this.reversed ? -c : c;
	}

	@Override
	public SortingTypes type() {
		return SortingTypes.NAME;
	}
}