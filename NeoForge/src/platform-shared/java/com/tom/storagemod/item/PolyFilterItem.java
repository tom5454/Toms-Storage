package com.tom.storagemod.item;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import com.tom.storagemod.client.ClientUtil;
import com.tom.storagemod.inventory.filter.ItemFilter;
import com.tom.storagemod.inventory.filter.PolyFilter;
import com.tom.storagemod.util.BlockFaceReference;

public class PolyFilterItem extends Item implements IItemFilter {

	public PolyFilterItem(Item.Properties pr) {
		super(pr);
	}

	@Override
	public void appendHoverText(ItemStack itemStack, TooltipContext tooltipContext, TooltipDisplay p_399753_,
			Consumer<Component> tooltip, TooltipFlag tooltipFlag) {
		ClientUtil.tooltip("poly_item_filter", tooltip);
	}

	@Override
	public ItemFilter createFilter(BlockFaceReference face, ItemStack stack) {
		return new PolyFilter(face);
	}

	@Override
	public void openGui(ItemStack is, Player player, BooleanSupplier isValid, Runnable refresh) {
	}
}
