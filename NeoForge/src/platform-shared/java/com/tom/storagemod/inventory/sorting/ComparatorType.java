package com.tom.storagemod.inventory.sorting;

import java.util.Comparator;

import com.tom.storagemod.inventory.StoredItemStack;

public class ComparatorType implements Comparator<StoredItemStack> {
  @Override
  public int compare(StoredItemStack in1, StoredItemStack in2) {
    String descriptionId1 = in1.getStack().getDescriptionId();
    String descriptionId2 = in2.getStack().getDescriptionId();

    String[] parts1 = descriptionId1.split("\\.");
    String[] parts2 = descriptionId2.split("\\.");

    int c = 0;
    for (int i = 0; i < 3; i++) {
      c = parts1[i].compareToIgnoreCase(parts2[i]);
      if (c == 0) {
        continue;
      }

      break;
    }

    return c;
  }
}