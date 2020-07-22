package com.tom.storagemod;

import net.minecraft.block.Block;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.Tag;

public class StorageTags {
	public static final Tag<Block> REMOTE_ACTIVATE = BlockTags.ACCESSOR.get("toms_storage:remote_activate");

	public static void init() {}
}
