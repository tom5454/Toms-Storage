package com.tom.storagemod.jade;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.phys.Vec2;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;

public class JadeUtil {

	public static void addFilterInfo(ITooltip tooltip, BlockAccessor accessor, ItemStack f) {
		IElementHelper elements = IElementHelper.get();
		if (accessor.showDetails()) {
			ITooltip t = elements.tooltip();

			IElement icon = elements.item(f, 1f).size(new Vec2(18, 18)).translate(new Vec2(0, -1));
			icon.message(null);
			t.add(icon);
			f.getTooltipLines(TooltipContext.of(accessor.getLevel()), accessor.getPlayer(), TooltipFlag.Default.NORMAL).forEach(t::add);

			BoxStyle.GradientBorder b = BoxStyle.getTransparent().clone();
			b.borderColor = new int[] {0xFFFF0000, 0xFFFF0000, 0xFFFF0000, 0xFFFF0000};
			b.borderWidth = 1;
			tooltip.add(elements.box(t, b));
		} else {
			tooltip.add(Component.translatable("tooltip.toms_storage.block_filter.item", f.getHoverName()));
		}
	}
}
