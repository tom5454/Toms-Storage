package com.tom.storagemod.jade;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import com.tom.storagemod.block.entity.BasicInventoryHopperBlockEntity;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

public enum InventoryHopperDataProvider implements IServerDataProvider<BlockAccessor> {
	INSTANCE;

	@Override
	public Identifier getUid() {
		return JadePlugin.INVENTORY_HOPPER;
	}

	@Override
	public void appendServerData(CompoundTag data, BlockAccessor accessor) {
		BasicInventoryHopperBlockEntity te = (BasicInventoryHopperBlockEntity) accessor.getBlockEntity();
		var f = te.getFilter();
		if (!f.isEmpty()) {
			data.store("filter", ItemStack.CODEC, f);
		}
		data.putBoolean("enabled", te.isEnabled());
	}
}
