package com.tom.storagemod.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface ILeftClickListener {
	boolean onLeftClick(ItemStack itemstack, BlockPos pos, Player player);
}
