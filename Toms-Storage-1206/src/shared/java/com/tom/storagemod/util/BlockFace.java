package com.tom.storagemod.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public record BlockFace(Level level, BlockPos pos, Direction from) {

	public BlockEntity getBlockEntity() {
		return level.getBlockEntity(pos);
	}

	public static BlockFace touching(Level level, BlockPos pos, Direction to) {
		return new BlockFace(level, pos.relative(to), to.getOpposite());
	}
}
