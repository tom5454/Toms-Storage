package com.tom.storagemod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public interface IPaintable {
	boolean paint(Level world, BlockPos pos, BlockState to);

	public static BlockState readBlockState(Level level, CompoundTag tag) {
		HolderGetter<Block> holdergetter = level != null ? level.holderLookup(Registries.BLOCK) : BuiltInRegistries.BLOCK.asLookup();
		return NbtUtils.readBlockState(holdergetter, tag);
	}
}
