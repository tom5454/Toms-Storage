package com.tom.storagemod.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record WorldPos(ResourceKey<Level> dim, BlockPos pos) {

	public static final Codec<WorldPos> CODEC = RecordCodecBuilder.<WorldPos>mapCodec(b -> {
		return b.group(
				ResourceKey.codec(Registries.DIMENSION).fieldOf("bound_dim").forGetter(WorldPos::dim),
				BlockPos.CODEC.fieldOf("bounds_pos").forGetter(WorldPos::pos)
				).apply(b, WorldPos::new);
	}).codec();
}
