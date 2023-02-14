package com.tom.storagemod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface IPaintable {
	boolean paint(Level world, BlockPos pos, BlockState to);
}
