package com.tom.storagemod.inventory.sorting;

import java.util.Comparator;

import com.tom.storagemod.inventory.StoredItemStack;

/**
 * Space efficiency is defined as the ability to store more items in fewer slots.
 * 
 * If I'm able to store 256 stone in 4 stacks, but I need 16 stacks for the same number of ender pearls,
 * then stone is more space-efficient.
 */
public class ComparatorSpaceEfficiency implements Comparator<StoredItemStack> {
	@Override
	public int compare(StoredItemStack in1, StoredItemStack in2) {
		// Fewer stacks = better
		int stackCount = -Long.compare(in1.getStackCount(), in2.getStackCount());
		// More items = better
		int itemCount = Long.compare(in1.getQuantity(), in2.getQuantity());
		// Larger stacks = better
		int stackSize = Long.compare(in1.getMaxStackSize(), in2.getMaxStackSize());

		return Integer.compare(Integer.compare(stackCount, itemCount), stackSize);
	}
}
