package com.tom.storagemod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

public interface NeoForgeBlock {
	default void onNeighborChange(BlockState state, LevelReader level, BlockPos pos, BlockPos neighbor) {}
	default boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, Direction side) { return false; }
}
