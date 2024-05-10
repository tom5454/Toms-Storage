package com.tom.storagemod.util;

import net.minecraft.world.item.ItemStack;

public interface ItemPredicate {
	boolean test(ItemStack stack);

	default boolean configMatch(ItemStack stack) {
		return true;
	}
}
