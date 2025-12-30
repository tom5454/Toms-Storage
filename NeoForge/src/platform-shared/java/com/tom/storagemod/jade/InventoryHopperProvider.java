package com.tom.storagemod.jade;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import com.tom.storagemod.block.entity.BasicInventoryHopperBlockEntity;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum InventoryHopperProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
	INSTANCE;

	@Override
	public ResourceLocation getUid() {
		return JadePlugin.INVENTORY_HOPPER;
	}

	@Override
	public void appendServerData(CompoundTag data, BlockAccessor accessor) {
		BasicInventoryHopperBlockEntity te = (BasicInventoryHopperBlockEntity) accessor.getBlockEntity();
		var f = te.getFilter();
		if (!f.isEmpty()) {
			data.put("filter", f.save(accessor.getLevel().registryAccess()));
		}
		data.putBoolean("enabled", te.isEnabled());
	}

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		var f = ItemStack.parseOptional(accessor.getLevel().registryAccess(), accessor.getServerData().getCompound("filter"));
		if (!f.isEmpty()) {
			JadeUtil.addFilterInfo(tooltip, accessor, f);
		}
		if (!accessor.getServerData().getBoolean("enabled")) {
			tooltip.add(Component.translatable("tooltip.toms_storage.disabled_by_redstone"));
		}
	}
}
