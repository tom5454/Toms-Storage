package com.tom.storagemod.block.entity;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.common.util.INBTSerializable;

import com.tom.storagemod.inventory.BlockFilter;

public class BlockFilterAttachment implements INBTSerializable<CompoundTag> {
	private final BlockFilter filter;

	public BlockFilterAttachment(IAttachmentHolder holder) {
		filter = new BlockFilter(((BlockEntity) holder).getBlockPos());
	}

	@Override
	public CompoundTag serializeNBT(Provider provider) {
		return filter.serializeNBT(provider);
	}

	@Override
	public void deserializeNBT(Provider provider, CompoundTag nbt) {
		filter.deserializeNBT(provider, nbt);
	}

	public BlockFilter getFilter() {
		return filter;
	}
}
