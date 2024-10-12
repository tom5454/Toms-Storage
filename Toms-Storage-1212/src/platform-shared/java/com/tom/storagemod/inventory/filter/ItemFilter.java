package com.tom.storagemod.inventory.filter;

import net.minecraft.world.item.ItemStack;

public interface ItemFilter extends ItemPredicate {

	default boolean configMatch(ItemStack stack) {
		return true;
	}

	public static final ItemFilter TRUE = s -> true;
}
