package com.tom.storagemod.item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import com.tom.storagemod.Content;
import com.tom.storagemod.client.ClientUtil;
import com.tom.storagemod.components.SimpleItemFilterComponent;
import com.tom.storagemod.inventory.filter.ItemFilter;
import com.tom.storagemod.inventory.filter.SimpleItemFilter;
import com.tom.storagemod.menu.ItemFilterMenu;
import com.tom.storagemod.util.BlockFaceReference;

public class FilterItem extends Item implements IItemFilter {

	public FilterItem(Item.Properties pr) {
		super(pr);
	}

	@Override
	public void appendHoverText(ItemStack itemStack, TooltipContext tooltipContext, List<Component> tooltip,
			TooltipFlag tooltipFlag) {
		ClientUtil.tooltip("item_filter", tooltip);
		if(Screen.hasControlDown()) {
			tooltip.add(Component.translatable("tooltip.toms_storage.item_filter.contents"));
			SimpleItemFilterComponent c = itemStack.get(Content.simpleItemFilterComponent.get());
			boolean allow = false;
			List<Component> elems = new ArrayList<>();
			if (c != null) {
				for (ItemStack s : c.stacks()) {
					if (!s.isEmpty()) {
						elems.add(Component.translatable("tooltip.toms_storage.item_filter.prefix", s.getHoverName()));
					}
				}
				allow = c.allowList();
			}
			if (elems.isEmpty()) {
				tooltip.add(Component.translatable("tooltip.toms_storage.item_filter.no_items"));
			} else {
				tooltip.addAll(elems);
			}
			tooltip.add(Component.translatable(allow ? "tooltip.toms_storage.allowList" : "tooltip.toms_storage.denyList"));
		} else {
			tooltip.add(Component.translatable("tooltip.toms_storage.hold_control_for_details", Minecraft.ON_OSX ? "CMD" : "CTRL").withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));
		}
	}

	@Override
	public ItemFilter createFilter(BlockFaceReference face, ItemStack stack) {
		return new SimpleItemFilter(stack);
	}

	@Override
	public InteractionResult use(Level world, Player player, InteractionHand hand) {
		ItemStack is = player.getItemInHand(hand);
		openGui(is, player, () -> player.getItemInHand(hand).getItem() == this, null);
		return InteractionResult.SUCCESS_SERVER;
	}

	@Override
	public void openGui(ItemStack is, Player player, BooleanSupplier isValid, Runnable refresh) {
		player.openMenu(new SimpleMenuProvider((id, pi, pl) -> new ItemFilterMenu(id, pi, new SimpleItemFilter(is), isValid, refresh), is.getHoverName()));
	}
}
