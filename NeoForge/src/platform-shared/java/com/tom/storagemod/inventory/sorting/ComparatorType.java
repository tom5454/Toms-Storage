package com.tom.storagemod.inventory.sorting;

import com.tom.storagemod.inventory.StoredItemStack;

public class ComparatorType extends StoredItemStackComparator {
  public ComparatorType(boolean reversed) {
    super(reversed);
  }

  @Override
  public int compare(StoredItemStack in1, StoredItemStack in2) {
      String descriptionId1 = in1.getStack().getDescriptionId();
      String descriptionId2 = in2.getStack().getDescriptionId();

      String[] parts1 = descriptionId1.split("\\.");
      String[] parts2 = descriptionId2.split("\\.");

      int c = 0;
      for (int i = 0; i < 3; i++) {
          c = parts1[i].compareToIgnoreCase(parts2[i]);
          if (c != 0) {
              break;
          }
      }

      return reversed ? -c : c;
  }

  @Override
  public SortingTypes type() {
    return SortingTypes.TYPE;
  }
}