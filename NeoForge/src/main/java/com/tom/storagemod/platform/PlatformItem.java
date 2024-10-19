package com.tom.storagemod.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

public class PlatformItem extends Item {

	public PlatformItem(Properties p_41383_) {
		super(p_41383_);
	}

	@Override
	public final InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
		return onRightClick(context.getPlayer(), stack, context.getClickedPos(), context.getHand());
	}

	public InteractionResult onRightClick(Player player, ItemStack stack, BlockPos pos, InteractionHand hand) {
		return InteractionResult.PASS;
	}
}
