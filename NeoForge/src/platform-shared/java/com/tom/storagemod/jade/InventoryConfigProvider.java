package com.tom.storagemod.jade;

import java.util.Locale;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.phys.Vec2;

import com.tom.storagemod.inventory.BlockFilter;
import com.tom.storagemod.util.Priority;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;

public enum InventoryConfigProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
	INSTANCE;

	@Override
	public ResourceLocation getUid() {
		return JadePlugin.INVENTORY_CONFIG;
	}

	@Override
	public void appendServerData(CompoundTag data, BlockAccessor accessor) {
		BlockFilter bf = BlockFilter.getFilterAt(accessor.getLevel(), accessor.getPosition());
		if (bf != null) {
			data.putBoolean("bf", true);
			data.putBoolean("skip", bf.skip());
			data.putByte("pr", (byte) bf.getPriority().ordinal());
			var f = bf.filter.getItem(0);
			if (!f.isEmpty()) {
				data.put("filter", f.save(accessor.getLevel().registryAccess()));
			}
		}
	}

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		if (accessor.getServerData().getBooleanOr("bf", false)) {
			IElementHelper elements = IElementHelper.get();
			boolean skip = accessor.getServerData().getBooleanOr("skip", false);
			var f = accessor.getServerData().getCompound("filter").flatMap(c -> ItemStack.parse(accessor.getLevel().registryAccess(), c)).orElse(ItemStack.EMPTY);
			tooltip.add(Component.translatable("tooltip.toms_storage.block_filter"));
			Priority pr = Priority.VALUES[Math.abs(accessor.getServerData().getByteOr("pr", (byte) 0)) % Priority.VALUES.length];
			if (skip) {
				tooltip.add(Component.translatable("tooltip.toms_storage.block_filter.skip"));
			} else {
				tooltip.add(Component.translatable("tooltip.toms_storage.priority_" + pr.name().toLowerCase(Locale.ROOT)));
				if (!f.isEmpty()) {
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
		}
	}
}
