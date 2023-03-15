package com.tom.storagemod;

import net.minecraft.block.Block;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class StorageTags {
	public static final TagKey<Block> REMOTE_ACTIVATE = TagKey.of(Registry.BLOCK_KEY, new Identifier("toms_storage:remote_activate"));

	public static void init() {}
}
