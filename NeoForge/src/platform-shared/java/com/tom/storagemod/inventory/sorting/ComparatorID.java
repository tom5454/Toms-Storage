package com.tom.storagemod.inventory.sorting;

import java.util.Comparator;

import com.tom.storagemod.inventory.StoredItemStack;

public class ComparatorID implements Comparator<StoredItemStack> {
  @Override
  public int compare(StoredItemStack in1, StoredItemStack in2) {
    return in1.getDescriptionId().compareTo(in2.getDescriptionId());
  }
}