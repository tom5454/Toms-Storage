package com.tom.storagemod.block;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IInventoryCable {
	default boolean canConnectFrom(BlockState state, Direction dir) {
		return true;
	}

	default List<BlockPos> next(World world, BlockState state, BlockPos pos) {
		List<BlockPos> next = new ArrayList<>();
		for (Direction d : Direction.values()) {
			next.add(pos.offset(d));
		}
		return next;
	}

	static boolean canConnect(BlockState block, Direction dir) {
		return block.getBlock() instanceof IInventoryCable && ((IInventoryCable)block.getBlock()).canConnectFrom(block, dir.getOpposite());
	}
}
