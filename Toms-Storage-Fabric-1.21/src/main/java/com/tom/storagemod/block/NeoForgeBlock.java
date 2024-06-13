package com.tom.storagemod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

public interface NeoForgeBlock {
	void onNeighborChange(BlockState state, LevelReader level, BlockPos pos, BlockPos neighbor);

}
