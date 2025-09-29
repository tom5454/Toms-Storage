package com.tom.storagemod.inventory.sorting;

import com.tom.storagemod.inventory.StoredItemStack;

public class ComparatorID extends StoredItemStackComparator {
  public ComparatorID(boolean reversed) {
    super(reversed);
  }

  @Override
  public int compare(StoredItemStack in1, StoredItemStack in2) {
    int c = in1.getStack().getDescriptionId().compareToIgnoreCase(in2.getStack().getDescriptionId());
    return reversed ? -c : c;
  }

  @Override
  public SortingTypes type() {
    return SortingTypes.ID;
  }
}