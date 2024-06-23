package com.tom.storagemod.item;

import java.util.List;
import java.util.function.BooleanSupplier;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import com.tom.storagemod.client.ClientUtil;
import com.tom.storagemod.inventory.filter.ItemPredicate;
import com.tom.storagemod.inventory.filter.PolyFilter;
import com.tom.storagemod.util.BlockFace;

public class PolyFilterItem extends Item implements IItemFilter {

	public PolyFilterItem() {
		super(new Properties().stacksTo(1));
	}

	@Override
	public void appendHoverText(ItemStack itemStack, TooltipContext tooltipContext, List<Component> tooltip,
			TooltipFlag tooltipFlag) {
		ClientUtil.tooltip("poly_item_filter", tooltip);
	}

	@Override
	public ItemPredicate createFilter(BlockFace face, ItemStack stack) {
		return new PolyFilter(face);
	}

	@Override
	public void openGui(ItemStack is, Player player, BooleanSupplier isValid, Runnable refresh) {
	}
}
