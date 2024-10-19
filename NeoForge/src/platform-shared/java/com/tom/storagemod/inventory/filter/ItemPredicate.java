package com.tom.storagemod.inventory.filter;

import com.tom.storagemod.inventory.StoredItemStack;

public interface ItemPredicate {
	boolean test(StoredItemStack stack);

	default void updateState() {}
}
