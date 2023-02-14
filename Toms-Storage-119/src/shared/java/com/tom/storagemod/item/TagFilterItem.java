package com.tom.storagemod.item;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import com.tom.storagemod.StorageModClient;
import com.tom.storagemod.gui.TagItemFilterMenu;
import com.tom.storagemod.platform.Platform;
import com.tom.storagemod.util.BlockFace;
import com.tom.storagemod.util.ItemPredicate;
import com.tom.storagemod.util.TagFilter;

public class TagFilterItem extends Item implements IItemFilter {

	public TagFilterItem() {
		super(Platform.itemProp().stacksTo(1));
	}

	@Override
	public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag tooltipFlag) {
		StorageModClient.tooltip("tag_item_filter", tooltip);
	}

	@Override
	public ItemPredicate createFilter(BlockFace face, ItemStack stack) {
		return new TagFilter(stack);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
		ItemStack is = player.getItemInHand(hand);
		player.openMenu(new SimpleMenuProvider((id, pi, pl) -> new TagItemFilterMenu(id, pi, new TagFilter(is)), is.getHoverName()));
		return InteractionResultHolder.sidedSuccess(is, world.isClientSide);
	}
}
