package com.tom.storagemod.jade;

import java.util.Locale;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import com.tom.storagemod.inventory.BlockFilter;
import com.tom.storagemod.util.Priority;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

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
		if (accessor.getServerData().getBoolean("bf")) {
			boolean skip = accessor.getServerData().getBoolean("skip");
			var f = ItemStack.parseOptional(accessor.getLevel().registryAccess(), accessor.getServerData().getCompound("filter"));
			tooltip.add(Component.translatable("tooltip.toms_storage.block_filter"));
			Priority pr = Priority.VALUES[Math.abs(accessor.getServerData().getByte("pr")) % Priority.VALUES.length];
			if (skip) {
				tooltip.add(Component.translatable("tooltip.toms_storage.block_filter.skip"));
			} else {
				tooltip.add(Component.translatable("tooltip.toms_storage.priority_" + pr.name().toLowerCase(Locale.ROOT)));
				if (!f.isEmpty()) {
					JadeUtil.addFilterInfo(tooltip, accessor, f);
				}
			}
		}
	}
}
