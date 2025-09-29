package com.tom.storagemod.inventory.sorting;

import com.tom.storagemod.inventory.StoredItemStack;

public class ComparatorAmount extends StoredItemStackComparator {
	public ComparatorAmount(boolean reversed) {
		super(reversed);
	}

	@Override
	public int compare(StoredItemStack in1, StoredItemStack in2) {
		int c = in2.getQuantity() > in1.getQuantity() ? 1 : (in1.getQuantity() == in2.getQuantity() ? in1.getDisplayName().compareTo(in2.getDisplayName()) : -1);
		return this.reversed ? -c : c;
	}

	@Override
	public SortingTypes type() {
		return SortingTypes.AMOUNT;
	}
}