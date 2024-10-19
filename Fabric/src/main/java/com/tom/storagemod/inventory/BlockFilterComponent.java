package com.tom.storagemod.inventory;

import org.ladysnake.cca.api.v3.component.Component;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class BlockFilterComponent implements Component {
	private BlockFilter filter;
	private BlockEntity be;

	public BlockFilterComponent(BlockEntity be) {
		this.be = be;
	}

	@Override
	public void readFromNbt(CompoundTag tag, Provider registryLookup) {
		if (tag.contains("data")) {
			filter = new BlockFilter(be.getBlockPos());
			filter.deserializeNBT(registryLookup, tag.getCompound("data"));
		}
	}

	@Override
	public void writeToNbt(CompoundTag tag, Provider registryLookup) {
		if(filter != null)
			tag.put("data", filter.serializeNBT(registryLookup));
	}

	public BlockFilter getFilter(boolean make) {
		if (filter == null && make)
			filter = new BlockFilter(be.getBlockPos());
		return filter;
	}

	public void remove(Level level, BlockPos pos) {
		if (filter != null)
			filter.dropContents(level, pos);
		filter = null;
	}
}
