package com.tom.storagemod.block;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface IInventoryCable {
	default boolean canConnectFrom(BlockState state, Direction dir) {
		return true;
	}

	default List<BlockPos> next(Level world, BlockState state, BlockPos pos) {
		List<BlockPos> next = new ArrayList<>();
		for (Direction d : Direction.values()) {
			next.add(pos.relative(d));
		}
		return next;
	}

	static boolean canConnect(BlockState block, Direction dir) {
		return block.getBlock() instanceof IInventoryCable && ((IInventoryCable)block.getBlock()).canConnectFrom(block, dir.getOpposite());
	}
}
