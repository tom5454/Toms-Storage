package com.tom.storagemod.inventory.sorting;

import java.util.Comparator;

import com.tom.storagemod.inventory.StoredItemStack;

public class ComparatorType implements Comparator<StoredItemStack> {
  @Override
  public int compare(StoredItemStack in1, StoredItemStack in2) {
    String[] parts1 = in1.getDescriptionId().split("\\.");
    String[] parts2 = in2.getDescriptionId().split("\\.");

    for (int i = 0; i < 3; i++) {
      int c = parts1[i].compareToIgnoreCase(parts2[i]);
      if (c != 0) return c;
    }

    return 0;
  }
}