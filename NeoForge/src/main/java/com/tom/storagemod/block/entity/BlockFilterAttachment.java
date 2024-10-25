package com.tom.storagemod.block.entity;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
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
		return (CompoundTag) BlockFilter.STATE_CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), filter.storeState()).getOrThrow();
	}

	@Override
	public void deserializeNBT(Provider provider, CompoundTag nbt) {
		BlockFilter.STATE_CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), nbt).ifSuccess(filter::loadFromState);
	}

	public BlockFilter getFilter() {
		return filter;
	}
}
