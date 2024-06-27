package com.tom.storagemod.inventory.filter;

import net.minecraft.world.item.ItemStack;

import com.tom.storagemod.inventory.StoredItemStack;

public interface ItemPredicate {
	boolean test(StoredItemStack stack);

	default boolean configMatch(ItemStack stack) {
		return true;
	}

	default void updateState() {}

	public static final ItemPredicate TRUE = s -> true;
}
