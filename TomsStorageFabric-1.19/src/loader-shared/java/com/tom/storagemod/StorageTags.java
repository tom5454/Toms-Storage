package com.tom.storagemod;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import com.tom.storagemod.platform.Platform;

public class StorageTags {
	public static final TagKey<Block> REMOTE_ACTIVATE = Platform.getBlockTag(new ResourceLocation("toms_storage:remote_activate"));

	public static void init() {}
}
