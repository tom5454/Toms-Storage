package com.tom.storagemod;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class StorageTags {
	public static final TagKey<Block> REMOTE_ACTIVATE = TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation("toms_storage:remote_activate"));

	public static void init() {}
}
