package com.tom.storagemod;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class StorageTags {
	public static final TagKey<Block> REMOTE_ACTIVATE = BlockTags.create(ResourceLocation.tryBuild(StorageMod.modid, "remote_activate"));
	public static final TagKey<Block> TRIMS = BlockTags.create(ResourceLocation.tryBuild(StorageMod.modid, "trims"));
	public static final TagKey<Block> INV_CONNECTOR_SKIP = BlockTags.create(ResourceLocation.tryBuild(StorageMod.modid, "inventory_connector_skip"));
}
