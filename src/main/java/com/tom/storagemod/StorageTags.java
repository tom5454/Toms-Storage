package com.tom.storagemod;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class StorageTags {
	public static final TagKey<Block> REMOTE_ACTIVATE = TagKey.create(Registries.BLOCK, new ResourceLocation("toms_storage:remote_activate"));

	public static void init() {}
}
