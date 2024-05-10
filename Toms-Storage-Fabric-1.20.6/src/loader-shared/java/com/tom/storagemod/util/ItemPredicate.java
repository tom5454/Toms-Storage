package com.tom.storagemod.util;

import java.util.function.Predicate;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.world.item.ItemStack;

public interface ItemPredicate extends Predicate<ItemVariant> {
	@Override
	boolean test(ItemVariant stack);

	default boolean configMatch(ItemStack stack) {
		return true;
	}
}
