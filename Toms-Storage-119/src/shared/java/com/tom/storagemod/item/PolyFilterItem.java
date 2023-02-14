package com.tom.storagemod.item;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import com.tom.storagemod.StorageModClient;
import com.tom.storagemod.platform.Platform;
import com.tom.storagemod.util.BlockFace;
import com.tom.storagemod.util.ItemPredicate;
import com.tom.storagemod.util.PolyFilter;

public class PolyFilterItem extends Item implements IItemFilter {

	public PolyFilterItem() {
		super(Platform.itemProp().stacksTo(1));
	}

	@Override
	public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag tooltipFlag) {
		StorageModClient.tooltip("poly_item_filter", tooltip);
	}

	@Override
	public ItemPredicate createFilter(BlockFace face, ItemStack stack) {
		return new PolyFilter(face);
	}

}
