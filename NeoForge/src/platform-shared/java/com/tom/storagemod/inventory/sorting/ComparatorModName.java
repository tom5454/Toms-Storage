package com.tom.storagemod.inventory.sorting;

import net.minecraft.core.registries.BuiltInRegistries;

import com.tom.storagemod.inventory.StoredItemStack;

public class ComparatorModName extends StoredItemStackComparator {
	public ComparatorModName(boolean reversed) {
		super(reversed);
	}

	@Override
	public int compare(StoredItemStack in1, StoredItemStack in2) {
		String m1 = BuiltInRegistries.ITEM.getKey(in1.getStack().getItem()).getNamespace();
		String m2 = BuiltInRegistries.ITEM.getKey(in2.getStack().getItem()).getNamespace();
		int c1 = m1.compareTo(m2);
		int c2 = in1.getDisplayName().compareTo(in2.getDisplayName());
		int c = c1 == 0 ? c2 : c1;
		return this.reversed ? -c : c;
	}

	@Override
	public SortingTypes type() {
		return SortingTypes.BY_MOD;
	}
}