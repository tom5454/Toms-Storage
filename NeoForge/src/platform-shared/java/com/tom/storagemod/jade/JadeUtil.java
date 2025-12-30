package com.tom.storagemod.jade;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.JadeUI;

public class JadeUtil {

	public static void addFilterInfo(ITooltip tooltip, BlockAccessor accessor, ItemStack f) {
		if (accessor.showDetails()) {
			ITooltip t = JadeUI.tooltip();

			var icon = JadeUI.item(f, 1f).size(18, 18).offset(0, -1);
			t.add(icon);
			f.getTooltipLines(TooltipContext.of(accessor.getLevel()), accessor.getPlayer(), TooltipFlag.Default.NORMAL).forEach(t::add);

			BoxStyle b = BoxStyle.transparent().clone();
			//b.borderColor = new int[] {0xFFFF0000, 0xFFFF0000, 0xFFFF0000, 0xFFFF0000};
			b.borderWidth = 1;
			tooltip.add(JadeUI.box(t, b));
		} else {
			tooltip.add(Component.translatable("tooltip.toms_storage.block_filter.item", f.getHoverName()));
		}
	}
}
