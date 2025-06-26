package com.tom.storagemod.block.entity;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

import com.tom.storagemod.inventory.BlockFilter;

public class BlockFilterAttachment implements ValueIOSerializable {
	private final BlockFilter filter;

	public BlockFilterAttachment(IAttachmentHolder holder) {
		filter = new BlockFilter(((BlockEntity) holder).getBlockPos());
	}

	public BlockFilter getFilter() {
		return filter;
	}

	@Override
	public void serialize(ValueOutput output) {
		output.store(BlockFilter.STATE_CODEC, filter.storeState());
	}

	@Override
	public void deserialize(ValueInput input) {
		input.read(BlockFilter.STATE_CODEC).ifPresent(filter::loadFromState);
	}
}
