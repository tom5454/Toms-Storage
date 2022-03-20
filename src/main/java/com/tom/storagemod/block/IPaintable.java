package com.tom.storagemod.block;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IPaintable {
	boolean paint(World world, BlockPos pos, BlockState to);
}
