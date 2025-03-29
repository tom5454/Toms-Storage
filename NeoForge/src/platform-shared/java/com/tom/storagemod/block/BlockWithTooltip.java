package com.tom.storagemod.block;

import java.util.function.Consumer;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public interface BlockWithTooltip {
	void appendHoverText(ItemStack itemStack, TooltipContext tooltipContext, Consumer<Component> tooltip,
			TooltipFlag tooltipFlag);
}
