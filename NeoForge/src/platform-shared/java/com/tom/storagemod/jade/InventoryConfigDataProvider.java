package com.tom.storagemod.jade;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import com.tom.storagemod.inventory.BlockFilter;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

public enum InventoryConfigDataProvider implements IServerDataProvider<BlockAccessor> {
	INSTANCE;

	@Override
	public Identifier getUid() {
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
				data.store("filter", ItemStack.CODEC, f);
			}
		}
	}
}
