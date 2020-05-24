package com.tom.storagemod;

import net.minecraft.block.Block;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;

public class StorageTags {
	public static final Tag<Block> REMOTE_ACTIVATE = new BlockTags.Wrapper(new ResourceLocation("toms_storage", "remote_activate"));
}
