package com.tom.storagemod.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.Level;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record WorldPos(ResourceKey<Level> dim, BlockPos pos) {

	public static final Codec<WorldPos> CODEC = ExtraCodecs.validate(RecordCodecBuilder.mapCodec(b -> {
		return b.group(
				ResourceKey.codec(Registries.DIMENSION).fieldOf("bound_dim").forGetter(WorldPos::dim),
				BlockPos.CODEC.fieldOf("bounds_pos").forGetter(WorldPos::pos)
				).apply(b, WorldPos::new);
	}), WorldPos::validate).codec();

	private static DataResult<WorldPos> validate(WorldPos p_286361_) {
		return DataResult.success(p_286361_);
	}
}
