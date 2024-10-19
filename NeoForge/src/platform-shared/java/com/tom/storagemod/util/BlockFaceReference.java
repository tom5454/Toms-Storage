package com.tom.storagemod.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public record BlockFaceReference(Level level, BlockPos pos, Direction from) {

	public BlockEntity getBlockEntity() {
		return level.getBlockEntity(pos);
	}

	public static BlockFaceReference touching(Level level, BlockPos pos, Direction to) {
		return new BlockFaceReference(level, pos.relative(to), to.getOpposite());
	}

	public BlockState getState() {
		return level.getBlockState(pos);
	}
}
