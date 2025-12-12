package com.tom.storagemod;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class StorageTags {
	public static final TagKey<Block> REMOTE_ACTIVATE = create("remote_activate");
	public static final TagKey<Block> TRIMS = create("trims");
	public static final TagKey<Block> INV_CONNECTOR_SKIP = create("inventory_connector_skip");
	public static final TagKey<Block> INV_CONFIG_SKIP = create("inventory_configurator_skip");

	private static TagKey<Block> create(String loc) {
		return TagKey.create(Registries.BLOCK, Identifier.tryBuild(StorageMod.modid, loc));
	}
}
