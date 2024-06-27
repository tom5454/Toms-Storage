package com.tom.storagemod.item;

import java.util.List;
import java.util.function.BooleanSupplier;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import com.tom.storagemod.client.ClientUtil;
import com.tom.storagemod.inventory.filter.ItemPredicate;
import com.tom.storagemod.inventory.filter.TagFilter;
import com.tom.storagemod.menu.TagItemFilterMenu;
import com.tom.storagemod.util.BlockFaceReference;

public class TagFilterItem extends Item implements IItemFilter {

	public TagFilterItem() {
		super(new Properties().stacksTo(1));
	}

	@Override
	public void appendHoverText(ItemStack itemStack, TooltipContext tooltipContext, List<Component> tooltip,
			TooltipFlag tooltipFlag) {
		ClientUtil.tooltip("tag_item_filter", tooltip);
	}

	@Override
	public ItemPredicate createFilter(BlockFaceReference face, ItemStack stack) {
		return new TagFilter(stack);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
		ItemStack is = player.getItemInHand(hand);
		openGui(is, player, () -> player.getItemInHand(hand).getItem() == this, null);
		return InteractionResultHolder.sidedSuccess(is, world.isClientSide);
	}

	@Override
	public void openGui(ItemStack is, Player player, BooleanSupplier isValid, Runnable refresh) {
		player.openMenu(new SimpleMenuProvider((id, pi, pl) -> new TagItemFilterMenu(id, pi, new TagFilter(is), isValid, refresh), is.getHoverName()));
	}
}
