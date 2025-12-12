package com.tom.storagemod;

import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class StorageTags {
	public static final TagKey<Block> REMOTE_ACTIVATE = BlockTags.create(Identifier.tryBuild(StorageMod.modid, "remote_activate"));
	public static final TagKey<Block> TRIMS = BlockTags.create(Identifier.tryBuild(StorageMod.modid, "trims"));
	public static final TagKey<Block> INV_CONNECTOR_SKIP = BlockTags.create(Identifier.tryBuild(StorageMod.modid, "inventory_connector_skip"));
	public static final TagKey<Block> INV_CONFIG_SKIP = BlockTags.create(Identifier.tryBuild(StorageMod.modid, "inventory_configurator_skip"));
}
