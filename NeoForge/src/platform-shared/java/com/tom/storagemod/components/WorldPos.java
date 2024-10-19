package com.tom.storagemod.components;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record WorldPos(ResourceKey<Level> dim, BlockPos pos) {

	public static final Codec<WorldPos> CODEC = RecordCodecBuilder.<WorldPos>mapCodec(b -> {
		return b.group(
				ResourceKey.codec(Registries.DIMENSION).fieldOf("bound_dim").forGetter(WorldPos::dim),
				BlockPos.CODEC.fieldOf("bounds_pos").forGetter(WorldPos::pos)
				).apply(b, WorldPos::new);
	}).codec();

	public BlockEntity getBlockEntity(ServerLevel world) {
		Level dim = world.getServer().getLevel(this.dim);
		if(!dim.isLoaded(pos))return null;
		return dim.getBlockEntity(pos);
	}
}
