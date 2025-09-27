package com.tom.storagemod.item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import com.tom.storagemod.Content;
import com.tom.storagemod.client.ClientUtil;
import com.tom.storagemod.components.TagFilterComponent;
import com.tom.storagemod.inventory.filter.ItemFilter;
import com.tom.storagemod.inventory.filter.TagFilter;
import com.tom.storagemod.menu.TagItemFilterMenu;
import com.tom.storagemod.util.BlockFaceReference;
import com.tom.storagemod.util.KeyUtil;

public class TagFilterItem extends Item implements IItemFilter {

	public TagFilterItem(Item.Properties pr) {
		super(pr);
	}

	@Override
	public void appendHoverText(ItemStack itemStack, TooltipContext tooltipContext, TooltipDisplay p_399753_,
			Consumer<Component> tooltip, TooltipFlag tooltipFlag) {
		ClientUtil.tooltip("tag_item_filter", tooltip);
		if(KeyUtil.hasControlDown()) {
			tooltip.accept(Component.translatable("tooltip.toms_storage.item_filter.contents"));
			TagFilterComponent c = itemStack.get(Content.tagFilterComponent.get());
			boolean allow = false;
			List<Component> elems = new ArrayList<>();
			if (c != null) {
				for (TagKey<Item> s : c.tags()) {
					elems.add(Component.translatable("tooltip.toms_storage.item_filter.prefix", s.location().toString()));
				}
				allow = c.allowList();
			}
			if (elems.isEmpty()) {
				tooltip.accept(Component.translatable("tooltip.toms_storage.item_filter.no_items"));
			} else {
				elems.forEach(tooltip);
			}
			tooltip.accept(Component.translatable(allow ? "tooltip.toms_storage.allowList" : "tooltip.toms_storage.denyList"));
		} else {
			tooltip.accept(Component.translatable("tooltip.toms_storage.hold_control_for_details", "CTRL").withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));
		}
	}

	@Override
	public ItemFilter createFilter(BlockFaceReference face, ItemStack stack) {
		return new TagFilter(stack);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack is = player.getItemInHand(hand);
		openGui(is, player, () -> player.getItemInHand(hand).getItem() == this, null);
		return InteractionResult.SUCCESS_SERVER;
	}

	@Override
	public void openGui(ItemStack is, Player player, BooleanSupplier isValid, Runnable refresh) {
		player.openMenu(new SimpleMenuProvider((id, pi, pl) -> new TagItemFilterMenu(id, pi, new TagFilter(is), isValid, refresh), is.getHoverName()));
	}
}
