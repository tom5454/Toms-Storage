package com.tom.storagemod.item;

import java.util.function.Consumer;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

import com.tom.storagemod.block.BlockWithTooltip;

public class BlockItemTSS extends BlockItem {

	public BlockItemTSS(Block p_40565_, Properties p_40566_) {
		super(p_40565_, p_40566_);
	}

	@Override
	public void appendHoverText(ItemStack p_41421_, TooltipContext p_339594_, TooltipDisplay p_399753_,
			Consumer<Component> p_399884_, TooltipFlag p_41424_) {
		((BlockWithTooltip) getBlock()).appendHoverText(p_41421_, p_339594_, p_399884_, p_41424_);
	}
}
