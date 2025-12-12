package com.tom.storagemod.jade;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;

import com.tom.storagemod.block.entity.InventoryConnectorBlockEntity;
import com.tom.storagemod.block.entity.InventoryConnectorBlockEntity.UsageInfo;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

public enum InventoryConnectorDataProvider implements IServerDataProvider<BlockAccessor> {
	INSTANCE;

	@Override
	public Identifier getUid() {
		return JadePlugin.INVENTORY_CONNECTOR;
	}

	@Override
	public void appendServerData(CompoundTag data, BlockAccessor accessor) {
		InventoryConnectorBlockEntity te = (InventoryConnectorBlockEntity) accessor.getBlockEntity();
		UsageInfo usage = te.getUsage();
		data.putInt("free", usage.free());
		data.putInt("all", usage.all());
		data.putInt("blocks", usage.blocks());
	}
}
