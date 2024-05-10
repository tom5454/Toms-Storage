package com.tom.storagemod.item;

import net.minecraft.world.item.ItemStack;

import com.tom.storagemod.util.BlockFace;
import com.tom.storagemod.util.ItemPredicate;

public interface IItemFilter {
	ItemPredicate createFilter(BlockFace from, ItemStack stack);
}
