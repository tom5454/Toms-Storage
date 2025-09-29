package com.tom.storagemod.inventory.sorting;

import net.minecraft.core.registries.BuiltInRegistries;

import java.util.Comparator;

import com.tom.storagemod.inventory.StoredItemStack;

public class ComparatorModName implements Comparator<StoredItemStack> {
	@Override
	public int compare(StoredItemStack in1, StoredItemStack in2) {
		String m1 = BuiltInRegistries.ITEM.getKey(in1.getStack().getItem()).getNamespace();
		String m2 = BuiltInRegistries.ITEM.getKey(in2.getStack().getItem()).getNamespace();

		int c = m1.compareTo(m2);
		if (c == 0) {
			c = in1.getDisplayName().compareTo(in2.getDisplayName());
		}
		
		return c;
	}
}