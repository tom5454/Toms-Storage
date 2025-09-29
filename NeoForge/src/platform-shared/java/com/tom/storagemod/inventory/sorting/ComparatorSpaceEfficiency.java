package com.tom.storagemod.inventory.sorting;

import com.tom.storagemod.inventory.StoredItemStack;

/**
 * Space efficiency is defined as the ability to store more items in the same number of slots.
 * 
 * If I'm able to store 256 stone in 4 stacks, but I need 16 stacks for the same number of ender pearls,
 * then stone is more space-efficient.
 */
public class ComparatorSpaceEfficiency extends StoredItemStackComparator {
  public ComparatorSpaceEfficiency(boolean reversed) {
    super(reversed);
  }

  @Override
  public int compare(StoredItemStack in1, StoredItemStack in2) {
    // Fewer stacks = better
    int stackCount = -Long.compare(in1.getStackCount(), in2.getStackCount());
    // More items = better
    int itemCount = Long.compare(in1.getQuantity(), in2.getQuantity());
    // Larger stacks = better
    int stackSize = Long.compare(in1.getMaxStackSize(), in2.getMaxStackSize());
    int c = Integer.compare(Integer.compare(stackCount, itemCount), stackSize);
    return this.reversed ? -c : c;
  }

  @Override
  public SortingTypes type() {
    return SortingTypes.SPACE_EFFICIENCY;
  }
}
