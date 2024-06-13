package com.tom.storagemod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

public interface IForgeBlock {
	boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, Direction side);
}
